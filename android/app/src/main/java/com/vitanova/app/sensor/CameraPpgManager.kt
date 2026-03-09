package com.vitanova.app.sensor

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * PPG measurement result emitted via [measurementState].
 *
 * @property currentBpm Current estimated heart rate in beats per minute.
 * @property rrIntervals List of recent R-R intervals in milliseconds.
 * @property isFingerDetected Whether a finger is currently covering the camera lens.
 * @property progress Measurement progress from 0f (just started) to 1f (complete).
 * @property signalQuality Estimated signal quality from 0f (poor) to 1f (excellent).
 */
data class PpgMeasurementState(
    val currentBpm: Int = 0,
    val rrIntervals: List<Long> = emptyList(),
    val isFingerDetected: Boolean = false,
    val progress: Float = 0f,
    val signalQuality: Float = 0f
)

sealed class PpgState {
    object Idle : PpgState()
    object Preparing : PpgState()
    data class Measuring(val measurement: PpgMeasurementState) : PpgState()
    data class Completed(val measurement: PpgMeasurementState) : PpgState()
    data class Error(val message: String) : PpgState()
}

/**
 * Measures heart rate via camera-based photoplethysmography (PPG).
 *
 * The user places a finger over the rear camera while the flash illuminates it.
 * Red channel intensity variations correspond to blood volume changes with each
 * heartbeat. A band-pass filter isolates the cardiac frequency band (0.5–4 Hz,
 * i.e. 30–240 BPM) and peak detection identifies individual beats to compute
 * BPM and R-R intervals.
 *
 * A measurement session runs for [MEASUREMENT_DURATION_MS] (60 seconds).
 */
class CameraPpgManager(private val context: Context) {

    companion object {
        private const val TAG = "CameraPpgManager"

        /** Total measurement duration in milliseconds. */
        const val MEASUREMENT_DURATION_MS = 60_000L

        /** Target analysis frame rate. */
        private const val TARGET_FPS = 30

        /** Sampling frequency matching the target FPS. */
        private const val SAMPLING_FREQ = 30.0

        /** Band-pass filter lower cutoff in Hz (30 BPM). */
        private const val FILTER_LOW_HZ = 0.5

        /** Band-pass filter upper cutoff in Hz (240 BPM). */
        private const val FILTER_HIGH_HZ = 4.0

        /** Minimum average red channel value to consider finger present. */
        private const val FINGER_DETECT_THRESHOLD = 50.0

        /** Number of samples to buffer before starting BPM calculation. */
        private const val MIN_SAMPLES_FOR_BPM = 60 // ~2 seconds at 30 fps

        /** Filter order for the Butterworth band-pass filter (2nd order). */
        private const val FILTER_ORDER = 2
    }

    private val _state = MutableStateFlow<PpgState>(PpgState.Idle)
    val state: StateFlow<PpgState> = _state.asStateFlow()

    private val _measurementState = MutableStateFlow(PpgMeasurementState())
    val measurementState: StateFlow<PpgMeasurementState> = _measurementState.asStateFlow()

    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var scope: CoroutineScope? = null
    private var timerJob: Job? = null

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    // Signal processing buffers
    private val rawRedSamples = mutableListOf<Double>()
    private val timestamps = mutableListOf<Long>()
    private val detectedPeaks = mutableListOf<Long>() // timestamps of detected peaks
    private var measurementStartTime = 0L

    // Butterworth band-pass filter state (2nd order, two cascaded biquad sections)
    private val filterStateX = DoubleArray(5) // input history
    private val filterStateY = DoubleArray(5) // output history

    /**
     * Starts a 60-second PPG measurement session.
     *
     * @param lifecycleOwner The lifecycle owner to bind the camera to (typically an Activity or Fragment).
     */
    fun startMeasurement(lifecycleOwner: LifecycleOwner) {
        if (_state.value is PpgState.Measuring) {
            Log.w(TAG, "Measurement already in progress")
            return
        }

        resetState()
        _state.value = PpgState.Preparing

        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider
                startCamera(provider, lifecycleOwner)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize camera", e)
                _state.value = PpgState.Error("Camera initialization failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Stops the current measurement session and releases camera resources.
     */
    fun stopMeasurement() {
        timerJob?.cancel()
        timerJob = null

        camera?.cameraControl?.enableTorch(false)
        camera = null

        cameraProvider?.unbindAll()
        cameraProvider = null

        scope?.cancel()
        scope = null

        if (_state.value is PpgState.Measuring) {
            val finalState = _measurementState.value
            if (finalState.currentBpm > 0) {
                _state.value = PpgState.Completed(finalState)
            } else {
                _state.value = PpgState.Idle
            }
        } else {
            _state.value = PpgState.Idle
        }
    }

    private fun startCamera(provider: ProcessCameraProvider, lifecycleOwner: LifecycleOwner) {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetFrameRate(androidx.camera.core.CameraSelector.QUALITY_SELECTOR_AUTO_PREFERENCE)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processFrame(imageProxy)
            imageProxy.close()
        }

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                imageAnalysis
            )

            // Turn on flash/torch to illuminate the finger
            camera?.cameraControl?.enableTorch(true)

            measurementStartTime = System.currentTimeMillis()
            _state.value = PpgState.Measuring(_measurementState.value)

            // Timer for measurement progress and completion
            timerJob = scope?.launch {
                while (true) {
                    val elapsed = System.currentTimeMillis() - measurementStartTime
                    val progress = (elapsed.toFloat() / MEASUREMENT_DURATION_MS).coerceIn(0f, 1f)

                    _measurementState.value = _measurementState.value.copy(progress = progress)
                    _state.value = PpgState.Measuring(_measurementState.value)

                    if (elapsed >= MEASUREMENT_DURATION_MS) {
                        val finalState = _measurementState.value.copy(progress = 1f)
                        _measurementState.value = finalState
                        _state.value = PpgState.Completed(finalState)
                        stopMeasurement()
                        break
                    }

                    delay(200)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind camera use cases", e)
            _state.value = PpgState.Error("Camera binding failed: ${e.message}")
        }
    }

    /**
     * Extracts average red channel intensity from a camera frame and processes
     * it through the PPG signal pipeline.
     */
    private fun processFrame(imageProxy: ImageProxy) {
        val avgRed = extractRedChannelAverage(imageProxy)
        val now = System.currentTimeMillis()

        val fingerDetected = avgRed > FINGER_DETECT_THRESHOLD

        synchronized(this) {
            rawRedSamples.add(avgRed)
            timestamps.add(now)

            // Keep a rolling window of the last 10 seconds of data
            val maxSamples = (SAMPLING_FREQ * 10).toInt()
            if (rawRedSamples.size > maxSamples) {
                rawRedSamples.removeAt(0)
                timestamps.removeAt(0)
            }
        }

        if (!fingerDetected) {
            _measurementState.value = _measurementState.value.copy(
                isFingerDetected = false,
                signalQuality = 0f
            )
            _state.value = PpgState.Measuring(_measurementState.value)
            return
        }

        if (rawRedSamples.size < MIN_SAMPLES_FOR_BPM) {
            _measurementState.value = _measurementState.value.copy(
                isFingerDetected = true,
                signalQuality = 0.1f
            )
            _state.value = PpgState.Measuring(_measurementState.value)
            return
        }

        // Process signal
        val filtered = applyBandPassFilter(rawRedSamples.toDoubleArray())
        val peaks = detectPeaks(filtered, timestamps.toLongArray())

        val bpm: Int
        val rrIntervals: List<Long>

        if (peaks.size >= 2) {
            rrIntervals = mutableListOf<Long>()
            for (i in 1 until peaks.size) {
                rrIntervals.add(peaks[i] - peaks[i - 1])
            }

            // Calculate BPM from average R-R interval
            val validRr = rrIntervals.filter { it in 250L..2000L } // 30-240 BPM range
            bpm = if (validRr.isNotEmpty()) {
                val avgRr = validRr.average()
                (60_000.0 / avgRr).roundToInt().coerceIn(30, 240)
            } else {
                0
            }

            // Signal quality based on R-R interval consistency
            val quality = if (validRr.size >= 2) {
                val mean = validRr.average()
                val variance = validRr.map { (it - mean) * (it - mean) }.average()
                val cv = sqrt(variance) / mean // coefficient of variation
                (1.0 - cv.coerceIn(0.0, 1.0)).toFloat()
            } else {
                0.3f
            }

            synchronized(detectedPeaks) {
                detectedPeaks.clear()
                detectedPeaks.addAll(peaks)
            }

            _measurementState.value = _measurementState.value.copy(
                currentBpm = bpm,
                rrIntervals = validRr,
                isFingerDetected = true,
                signalQuality = quality
            )
        } else {
            _measurementState.value = _measurementState.value.copy(
                isFingerDetected = true,
                signalQuality = 0.2f
            )
        }

        _state.value = PpgState.Measuring(_measurementState.value)
    }

    /**
     * Extracts the average red channel intensity from a YUV_420_888 image.
     * In YUV, the red component can be derived from Y (luminance) and the
     * V (Cr) chrominance plane. For PPG with flash on a finger, the Y plane
     * alone correlates strongly with red intensity since the scene is
     * predominantly red.
     */
    private fun extractRedChannelAverage(imageProxy: ImageProxy): Double {
        val yPlane = imageProxy.planes[0]
        val uPlane = imageProxy.planes[1]
        val vPlane = imageProxy.planes[2]

        val yBuffer = yPlane.buffer
        val vBuffer = vPlane.buffer

        val width = imageProxy.width
        val height = imageProxy.height

        // Sample a center region (1/4 of the frame) for more stable readings
        val startX = width / 4
        val endX = width * 3 / 4
        val startY = height / 4
        val endY = height * 3 / 4

        var redSum = 0.0
        var count = 0

        val yRowStride = yPlane.rowStride
        val yPixelStride = yPlane.pixelStride
        val vRowStride = vPlane.rowStride
        val vPixelStride = vPlane.pixelStride

        // Sample every 4th pixel for performance
        for (row in startY until endY step 4) {
            for (col in startX until endX step 4) {
                val yIndex = row * yRowStride + col * yPixelStride
                val uvRow = row / 2
                val uvCol = col / 2
                val vIndex = uvRow * vRowStride + uvCol * vPixelStride

                if (yIndex < yBuffer.capacity() && vIndex < vBuffer.capacity()) {
                    val y = (yBuffer.get(yIndex).toInt() and 0xFF).toDouble()
                    val v = (vBuffer.get(vIndex).toInt() and 0xFF).toDouble() - 128.0

                    // Convert YUV to Red: R = Y + 1.402 * V
                    val red = (y + 1.402 * v).coerceIn(0.0, 255.0)
                    redSum += red
                    count++
                }
            }
        }

        return if (count > 0) redSum / count else 0.0
    }

    /**
     * Applies a 2nd-order Butterworth band-pass filter between [FILTER_LOW_HZ]
     * and [FILTER_HIGH_HZ] at [SAMPLING_FREQ] Hz sampling rate.
     *
     * The filter is implemented as a direct-form II transposed biquad cascade.
     */
    private fun applyBandPassFilter(signal: DoubleArray): DoubleArray {
        if (signal.isEmpty()) return signal

        // Compute Butterworth band-pass coefficients
        val coefficients = computeBandPassCoefficients(
            FILTER_LOW_HZ,
            FILTER_HIGH_HZ,
            SAMPLING_FREQ
        )

        val output = DoubleArray(signal.size)

        // Forward pass (zero-phase filtering requires forward + reverse)
        val state = DoubleArray(4) // Two delay elements per biquad section
        for (i in signal.indices) {
            output[i] = applyBiquad(signal[i], coefficients, state)
        }

        // Reverse pass for zero-phase filtering
        val reverseState = DoubleArray(4)
        for (i in output.indices.reversed()) {
            output[i] = applyBiquad(output[i], coefficients, reverseState)
        }

        return output
    }

    /**
     * Computes biquad coefficients for a 2nd-order Butterworth band-pass filter.
     * Returns array: [b0, b1, b2, a1, a2] (a0 is normalized to 1).
     */
    private fun computeBandPassCoefficients(
        lowFreq: Double,
        highFreq: Double,
        sampleRate: Double
    ): DoubleArray {
        val w1 = 2.0 * Math.PI * lowFreq / sampleRate
        val w2 = 2.0 * Math.PI * highFreq / sampleRate

        val centerFreq = (w1 + w2) / 2.0
        val bandwidth = w2 - w1

        val cosW0 = cos(centerFreq)
        val sinW0 = sin(centerFreq)
        val alpha = sinW0 * sinh(Math.log(2.0) / 2.0 * bandwidth / sinW0)

        val b0 = alpha
        val b1 = 0.0
        val b2 = -alpha
        val a0 = 1.0 + alpha
        val a1 = -2.0 * cosW0
        val a2 = 1.0 - alpha

        // Normalize
        return doubleArrayOf(
            b0 / a0,
            b1 / a0,
            b2 / a0,
            a1 / a0,
            a2 / a0
        )
    }

    private fun sinh(x: Double): Double = (Math.exp(x) - Math.exp(-x)) / 2.0

    /**
     * Applies a single biquad filter section.
     * State array: [w1, w2] (direct-form II transposed).
     */
    private fun applyBiquad(
        input: Double,
        coefficients: DoubleArray,
        state: DoubleArray
    ): Double {
        val b0 = coefficients[0]
        val b1 = coefficients[1]
        val b2 = coefficients[2]
        val a1 = coefficients[3]
        val a2 = coefficients[4]

        val output = b0 * input + state[0]
        state[0] = b1 * input - a1 * output + state[1]
        state[1] = b2 * input - a2 * output

        return output
    }

    /**
     * Detects peaks in the filtered PPG signal using a simple threshold-based
     * approach with minimum inter-peak distance enforcement.
     *
     * @return List of timestamps at which peaks (heartbeats) were detected.
     */
    private fun detectPeaks(filtered: DoubleArray, timestampsArray: LongArray): List<Long> {
        if (filtered.size < 3) return emptyList()

        val peaks = mutableListOf<Long>()

        // Compute adaptive threshold as a fraction of signal amplitude
        val maxVal = filtered.max()
        val minVal = filtered.min()
        val range = maxVal - minVal
        if (range < 1e-6) return emptyList() // flat signal, no peaks

        val threshold = minVal + range * 0.4

        // Minimum inter-peak distance: 250ms (240 BPM max)
        val minPeakDistanceMs = 250L

        for (i in 1 until filtered.size - 1) {
            val isPeak = filtered[i] > filtered[i - 1] &&
                    filtered[i] > filtered[i + 1] &&
                    filtered[i] > threshold

            if (isPeak) {
                val peakTime = timestampsArray[i]
                if (peaks.isEmpty() || (peakTime - peaks.last()) >= minPeakDistanceMs) {
                    peaks.add(peakTime)
                }
            }
        }

        return peaks
    }

    private fun resetState() {
        synchronized(this) {
            rawRedSamples.clear()
            timestamps.clear()
            detectedPeaks.clear()
        }
        filterStateX.fill(0.0)
        filterStateY.fill(0.0)
        _measurementState.value = PpgMeasurementState()
        measurementStartTime = 0L
    }

    /**
     * Releases all resources. Call when the manager is no longer needed.
     */
    fun destroy() {
        stopMeasurement()
        cameraExecutor.shutdown()
    }
}
