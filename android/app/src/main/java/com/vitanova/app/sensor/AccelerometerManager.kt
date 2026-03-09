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
import kotlin.math.sqrt

data class AccelerometerData(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val magnitude: Float = 0f,
    val timestamp: Long = 0L
)

sealed class AccelerometerState {
    object Unavailable : AccelerometerState()
    object Stopped : AccelerometerState()
    data class Active(val data: AccelerometerData) : AccelerometerState()
    data class Error(val message: String) : AccelerometerState()
}

class AccelerometerManager(context: Context) {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _state = MutableStateFlow<AccelerometerState>(
        if (accelerometer == null) AccelerometerState.Unavailable
        else AccelerometerState.Stopped
    )
    val state: StateFlow<AccelerometerState> = _state.asStateFlow()

    private val _movementIntensity = MutableStateFlow(0f)
    val movementIntensity: StateFlow<Float> = _movementIntensity.asStateFlow()

    val isAvailable: Boolean get() = accelerometer != null

    private var listener: SensorEventListener? = null

    /**
     * Returns a cold Flow of movement intensity (magnitude of acceleration vector).
     * The flow automatically registers and unregisters the sensor listener
     * based on collector lifecycle.
     */
    fun movementFlow(samplingPeriod: Int = SensorManager.SENSOR_DELAY_UI): Flow<Float> =
        callbackFlow {
            if (accelerometer == null) {
                close(IllegalStateException("Accelerometer sensor not available on this device"))
                return@callbackFlow
            }

            val callbackListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = sqrt(x * x + y * y + z * z)
                    trySend(magnitude)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            val registered = sensorManager.registerListener(
                callbackListener,
                accelerometer,
                samplingPeriod
            )

            if (!registered) {
                close(IllegalStateException("Failed to register accelerometer listener"))
                return@callbackFlow
            }

            awaitClose {
                sensorManager.unregisterListener(callbackListener)
            }
        }

    /**
     * Starts continuous accelerometer monitoring, updating [state] and [movementIntensity].
     * Call [stop] to unregister the listener and free resources.
     */
    fun start(samplingPeriod: Int = SensorManager.SENSOR_DELAY_UI) {
        if (accelerometer == null) {
            _state.value = AccelerometerState.Unavailable
            return
        }

        stop()

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt(x * x + y * y + z * z)

                val data = AccelerometerData(
                    x = x,
                    y = y,
                    z = z,
                    magnitude = magnitude,
                    timestamp = event.timestamp
                )
                _state.value = AccelerometerState.Active(data)
                _movementIntensity.value = magnitude
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val registered = sensorManager.registerListener(
            listener,
            accelerometer,
            samplingPeriod
        )

        if (!registered) {
            _state.value = AccelerometerState.Error("Failed to register accelerometer listener")
            listener = null
        }
    }

    /**
     * Stops accelerometer monitoring and unregisters the sensor listener.
     */
    fun stop() {
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
        _state.value = if (accelerometer != null) AccelerometerState.Stopped
        else AccelerometerState.Unavailable
        _movementIntensity.value = 0f
    }
}
