package com.vitanova.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Calendar

data class StepCounterData(
    val todaySteps: Int = 0,
    val stepsPerHour: Map<Int, Int> = emptyMap(),
    val lastUpdated: Long = 0L
)

sealed class StepCounterState {
    object Unavailable : StepCounterState()
    object Stopped : StepCounterState()
    data class Active(val data: StepCounterData) : StepCounterState()
    data class Error(val message: String) : StepCounterState()
}

class StepCounterManager(context: Context) {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepCounterSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _state = MutableStateFlow<StepCounterState>(
        if (stepCounterSensor == null) StepCounterState.Unavailable
        else StepCounterState.Stopped
    )
    val state: StateFlow<StepCounterState> = _state.asStateFlow()

    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps.asStateFlow()

    val isAvailable: Boolean get() = stepCounterSensor != null

    private var listener: SensorEventListener? = null
    private var initialStepCount: Float = -1f
    private var dayOfYear: Int = -1
    private val hourlySteps = mutableMapOf<Int, Int>()
    private var lastHourStepBase: Int = 0
    private var lastHour: Int = -1

    /**
     * Returns a cold Flow that emits today's step count.
     * TYPE_STEP_COUNTER reports cumulative steps since last device reboot.
     * This flow calculates the delta from when monitoring started for the current day.
     */
    fun stepsFlow(samplingPeriod: Int = SensorManager.SENSOR_DELAY_NORMAL): Flow<Int> =
        callbackFlow {
            if (stepCounterSensor == null) {
                close(IllegalStateException("Step counter sensor not available on this device"))
                return@callbackFlow
            }

            var flowInitialCount = -1f
            var flowDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

            val callbackListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val cumulativeSteps = event.values[0]
                    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

                    if (currentDay != flowDayOfYear) {
                        flowInitialCount = cumulativeSteps
                        flowDayOfYear = currentDay
                    }

                    if (flowInitialCount < 0f) {
                        flowInitialCount = cumulativeSteps
                    }

                    val steps = (cumulativeSteps - flowInitialCount).toInt()
                    trySend(steps)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            val registered = sensorManager.registerListener(
                callbackListener,
                stepCounterSensor,
                samplingPeriod
            )

            if (!registered) {
                close(IllegalStateException("Failed to register step counter listener"))
                return@callbackFlow
            }

            awaitClose {
                sensorManager.unregisterListener(callbackListener)
            }
        }

    /**
     * Starts continuous step counting. Updates [state] with today's step count
     * and per-hour breakdown. Handles day rollovers by resetting the baseline.
     */
    fun start(samplingPeriod: Int = SensorManager.SENSOR_DELAY_NORMAL) {
        if (stepCounterSensor == null) {
            _state.value = StepCounterState.Unavailable
            return
        }

        stop()

        val calendar = Calendar.getInstance()
        dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        lastHour = calendar.get(Calendar.HOUR_OF_DAY)
        hourlySteps.clear()

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val cumulativeSteps = event.values[0]
                val now = Calendar.getInstance()
                val currentDay = now.get(Calendar.DAY_OF_YEAR)
                val currentHour = now.get(Calendar.HOUR_OF_DAY)

                // Day rollover: reset baseline
                if (currentDay != dayOfYear) {
                    initialStepCount = cumulativeSteps
                    dayOfYear = currentDay
                    hourlySteps.clear()
                    lastHourStepBase = 0
                    lastHour = currentHour
                }

                // First reading: set baseline
                if (initialStepCount < 0f) {
                    initialStepCount = cumulativeSteps
                }

                val todayTotal = (cumulativeSteps - initialStepCount).toInt()

                // Track per-hour buckets
                if (currentHour != lastHour) {
                    hourlySteps[lastHour] = todayTotal - lastHourStepBase
                    lastHourStepBase = todayTotal
                    lastHour = currentHour
                }
                // Current hour's running count
                hourlySteps[currentHour] = todayTotal - lastHourStepBase

                _todaySteps.value = todayTotal
                _state.value = StepCounterState.Active(
                    StepCounterData(
                        todaySteps = todayTotal,
                        stepsPerHour = hourlySteps.toMap(),
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val registered = sensorManager.registerListener(
            listener,
            stepCounterSensor,
            samplingPeriod
        )

        if (!registered) {
            _state.value = StepCounterState.Error("Failed to register step counter listener")
            listener = null
        }
    }

    /**
     * Stops step counting and unregisters the sensor listener.
     */
    fun stop() {
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
        initialStepCount = -1f
        _state.value = if (stepCounterSensor != null) StepCounterState.Stopped
        else StepCounterState.Unavailable
    }
}
