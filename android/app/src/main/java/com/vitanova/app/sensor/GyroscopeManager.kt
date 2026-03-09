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

data class RotationData(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val magnitude: Float = 0f,
    val timestamp: Long = 0L
)

sealed class GyroscopeState {
    object Unavailable : GyroscopeState()
    object Stopped : GyroscopeState()
    data class Active(val data: RotationData) : GyroscopeState()
    data class Error(val message: String) : GyroscopeState()
}

class GyroscopeManager(context: Context) {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val gyroscope: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _state = MutableStateFlow<GyroscopeState>(
        if (gyroscope == null) GyroscopeState.Unavailable
        else GyroscopeState.Stopped
    )
    val state: StateFlow<GyroscopeState> = _state.asStateFlow()

    val isAvailable: Boolean get() = gyroscope != null

    private var listener: SensorEventListener? = null

    /**
     * Returns a cold Flow of [RotationData] containing rotation rates around x, y, z axes
     * in radians per second. The flow automatically manages sensor registration.
     */
    fun rotationFlow(samplingPeriod: Int = SensorManager.SENSOR_DELAY_UI): Flow<RotationData> =
        callbackFlow {
            if (gyroscope == null) {
                close(IllegalStateException("Gyroscope sensor not available on this device"))
                return@callbackFlow
            }

            val callbackListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = sqrt(x * x + y * y + z * z)

                    trySend(
                        RotationData(
                            x = x,
                            y = y,
                            z = z,
                            magnitude = magnitude,
                            timestamp = event.timestamp
                        )
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            val registered = sensorManager.registerListener(
                callbackListener,
                gyroscope,
                samplingPeriod
            )

            if (!registered) {
                close(IllegalStateException("Failed to register gyroscope listener"))
                return@callbackFlow
            }

            awaitClose {
                sensorManager.unregisterListener(callbackListener)
            }
        }

    /**
     * Starts continuous gyroscope monitoring, updating [state] with live rotation data.
     */
    fun start(samplingPeriod: Int = SensorManager.SENSOR_DELAY_UI) {
        if (gyroscope == null) {
            _state.value = GyroscopeState.Unavailable
            return
        }

        stop()

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt(x * x + y * y + z * z)

                _state.value = GyroscopeState.Active(
                    RotationData(
                        x = x,
                        y = y,
                        z = z,
                        magnitude = magnitude,
                        timestamp = event.timestamp
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val registered = sensorManager.registerListener(
            listener,
            gyroscope,
            samplingPeriod
        )

        if (!registered) {
            _state.value = GyroscopeState.Error("Failed to register gyroscope listener")
            listener = null
        }
    }

    /**
     * Stops gyroscope monitoring and releases sensor resources.
     */
    fun stop() {
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
        _state.value = if (gyroscope != null) GyroscopeState.Stopped
        else GyroscopeState.Unavailable
    }
}
