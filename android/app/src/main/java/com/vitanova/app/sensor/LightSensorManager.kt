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

data class LightSensorData(
    val lux: Float = 0f,
    val isNightUsage: Boolean = false,
    val timestamp: Long = 0L
)

sealed class LightSensorState {
    object Unavailable : LightSensorState()
    object Stopped : LightSensorState()
    data class Active(val data: LightSensorData) : LightSensorState()
    data class Error(val message: String) : LightSensorState()
}

class LightSensorManager(context: Context) {

    companion object {
        /** Lux threshold below which ambient light is considered very low. */
        private const val NIGHT_LUX_THRESHOLD = 10f

        /** Hour of day (24h) at or after which we consider it "late". */
        private const val NIGHT_HOUR_START = 22

        /** Hour of day (24h) before which we consider it "early morning". */
        private const val NIGHT_HOUR_END = 6
    }

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val lightSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _state = MutableStateFlow<LightSensorState>(
        if (lightSensor == null) LightSensorState.Unavailable
        else LightSensorState.Stopped
    )
    val state: StateFlow<LightSensorState> = _state.asStateFlow()

    private val _currentLux = MutableStateFlow(0f)
    val currentLux: StateFlow<Float> = _currentLux.asStateFlow()

    val isAvailable: Boolean get() = lightSensor != null

    private var listener: SensorEventListener? = null

    /**
     * Returns a cold Flow of ambient light level in lux.
     * The flow automatically manages sensor registration/unregistration.
     */
    fun luxFlow(samplingPeriod: Int = SensorManager.SENSOR_DELAY_NORMAL): Flow<Float> =
        callbackFlow {
            if (lightSensor == null) {
                close(IllegalStateException("Light sensor not available on this device"))
                return@callbackFlow
            }

            val callbackListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    trySend(event.values[0])
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            val registered = sensorManager.registerListener(
                callbackListener,
                lightSensor,
                samplingPeriod
            )

            if (!registered) {
                close(IllegalStateException("Failed to register light sensor listener"))
                return@callbackFlow
            }

            awaitClose {
                sensorManager.unregisterListener(callbackListener)
            }
        }

    /**
     * Starts continuous light level monitoring with night-usage detection.
     * Night usage is flagged when light is below [NIGHT_LUX_THRESHOLD] lux
     * and the current hour is between [NIGHT_HOUR_START] and [NIGHT_HOUR_END].
     */
    fun start(samplingPeriod: Int = SensorManager.SENSOR_DELAY_NORMAL) {
        if (lightSensor == null) {
            _state.value = LightSensorState.Unavailable
            return
        }

        stop()

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val lux = event.values[0]
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val isLateHour = hour >= NIGHT_HOUR_START || hour < NIGHT_HOUR_END
                val isNightUsage = lux < NIGHT_LUX_THRESHOLD && isLateHour

                _currentLux.value = lux
                _state.value = LightSensorState.Active(
                    LightSensorData(
                        lux = lux,
                        isNightUsage = isNightUsage,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val registered = sensorManager.registerListener(
            listener,
            lightSensor,
            samplingPeriod
        )

        if (!registered) {
            _state.value = LightSensorState.Error("Failed to register light sensor listener")
            listener = null
        }
    }

    /**
     * Stops light sensor monitoring and releases resources.
     */
    fun stop() {
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
        _currentLux.value = 0f
        _state.value = if (lightSensor != null) LightSensorState.Stopped
        else LightSensorState.Unavailable
    }
}
