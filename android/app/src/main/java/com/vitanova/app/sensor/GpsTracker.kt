package com.vitanova.app.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

/**
 * A single GPS track point with all relevant location data.
 */
data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val accuracy: Float,
    val bearing: Float,
    val timestamp: Long
)

/**
 * Aggregate GPS tracking state emitted via [GpsTracker.trackingState].
 *
 * @property currentLocation The most recent [GpsPoint], or null if none yet.
 * @property totalDistanceMeters Cumulative distance covered in meters.
 * @property currentPaceMinPerKm Current pace in minutes per kilometer (0 if stationary).
 * @property averageSpeedKmh Average speed over the session in km/h.
 * @property elevationGainMeters Cumulative positive elevation gain in meters.
 * @property durationMs Active tracking duration in milliseconds (excludes paused time).
 * @property points All recorded track points.
 * @property isTracking Whether tracking is currently active.
 * @property isPaused Whether tracking is paused.
 */
data class GpsTrackingState(
    val currentLocation: GpsPoint? = null,
    val totalDistanceMeters: Double = 0.0,
    val currentPaceMinPerKm: Double = 0.0,
    val averageSpeedKmh: Double = 0.0,
    val elevationGainMeters: Double = 0.0,
    val durationMs: Long = 0L,
    val points: List<GpsPoint> = emptyList(),
    val isTracking: Boolean = false,
    val isPaused: Boolean = false
)

sealed class GpsTrackerState {
    object Idle : GpsTrackerState()
    data class Tracking(val data: GpsTrackingState) : GpsTrackerState()
    data class Paused(val data: GpsTrackingState) : GpsTrackerState()
    data class Error(val message: String) : GpsTrackerState()
}

/**
 * Tracks GPS location during exercise sessions using [FusedLocationProviderClient].
 *
 * Requests high-accuracy location updates every 3 seconds and computes running
 * metrics including total distance, current pace, average speed, and elevation gain.
 * Supports pause/resume for rest intervals.
 */
class GpsTracker(private val context: Context) {

    companion object {
        private const val TAG = "GpsTracker"

        /** Location update interval in milliseconds. */
        private const val UPDATE_INTERVAL_MS = 3_000L

        /** Fastest allowed location update interval. */
        private const val FASTEST_INTERVAL_MS = 1_000L

        /** Minimum distance change to record a new point (meters). */
        private const val MIN_DISPLACEMENT_METERS = 2f

        /** Minimum accuracy in meters for a location fix to be considered useful. */
        private const val MAX_ACCEPTABLE_ACCURACY = 30f

        /**
         * Minimum altitude change between consecutive points to count as
         * elevation gain (prevents GPS altitude noise from inflating the total).
         */
        private const val ELEVATION_NOISE_THRESHOLD = 2.0

        /** Speed threshold below which pace is considered "stationary" (m/s). */
        private const val STATIONARY_SPEED_THRESHOLD = 0.3f
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _state = MutableStateFlow<GpsTrackerState>(GpsTrackerState.Idle)
    val state: StateFlow<GpsTrackerState> = _state.asStateFlow()

    private val _trackingState = MutableStateFlow(GpsTrackingState())
    val trackingState: StateFlow<GpsTrackingState> = _trackingState.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private val trackPoints = mutableListOf<GpsPoint>()
    private var totalDistance = 0.0
    private var elevationGain = 0.0
    private var previousLocation: Location? = null
    private var previousAltitude: Double? = null

    private var trackingStartTime = 0L
    private var pausedDuration = 0L
    private var pauseStartTime = 0L
    private var isActive = false
    private var isPaused = false

    /**
     * Starts GPS tracking. Requires ACCESS_FINE_LOCATION permission.
     *
     * @param ctx Context used for permission checking (uses constructor context if same).
     */
    fun startTracking(ctx: Context = context) {
        if (isActive) {
            Log.w(TAG, "Tracking already active")
            return
        }

        if (!hasLocationPermission(ctx)) {
            _state.value = GpsTrackerState.Error(
                "Location permission not granted. ACCESS_FINE_LOCATION is required."
            )
            return
        }

        resetState()
        isActive = true
        isPaused = false
        trackingStartTime = SystemClock.elapsedRealtime()

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
            .setMinUpdateDistanceMeters(MIN_DISPLACEMENT_METERS)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (!isActive || isPaused) return

                val location = result.lastLocation ?: return
                processLocation(location)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )

            _trackingState.value = _trackingState.value.copy(isTracking = true, isPaused = false)
            _state.value = GpsTrackerState.Tracking(_trackingState.value)
        } catch (e: SecurityException) {
            _state.value = GpsTrackerState.Error("Location permission denied: ${e.message}")
            isActive = false
        }
    }

    /**
     * Completely stops GPS tracking and releases location resources.
     */
    fun stopTracking() {
        if (!isActive) return

        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
        isActive = false
        isPaused = false

        _trackingState.value = _trackingState.value.copy(isTracking = false, isPaused = false)
        _state.value = GpsTrackerState.Idle
    }

    /**
     * Pauses tracking (e.g. during a rest stop). Location updates continue
     * to be received but are not processed. Duration clock is frozen.
     */
    fun pauseTracking() {
        if (!isActive || isPaused) return

        isPaused = true
        pauseStartTime = SystemClock.elapsedRealtime()

        _trackingState.value = _trackingState.value.copy(isPaused = true)
        _state.value = GpsTrackerState.Paused(_trackingState.value)
    }

    /**
     * Resumes tracking after a pause. The paused interval is excluded
     * from the total duration.
     */
    fun resumeTracking() {
        if (!isActive || !isPaused) return

        pausedDuration += SystemClock.elapsedRealtime() - pauseStartTime
        isPaused = false
        previousLocation = null // prevent large distance jump after pause

        _trackingState.value = _trackingState.value.copy(isPaused = false)
        _state.value = GpsTrackerState.Tracking(_trackingState.value)
    }

    private fun processLocation(location: Location) {
        // Discard inaccurate fixes
        if (location.accuracy > MAX_ACCEPTABLE_ACCURACY) return

        val point = GpsPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            speed = location.speed,
            accuracy = location.accuracy,
            bearing = location.bearing,
            timestamp = System.currentTimeMillis()
        )

        // Distance accumulation
        previousLocation?.let { prev ->
            val distanceTo = prev.distanceTo(location)
            // Only add distance if it exceeds GPS noise threshold
            if (distanceTo > MIN_DISPLACEMENT_METERS && location.accuracy < MAX_ACCEPTABLE_ACCURACY) {
                totalDistance += distanceTo
            }
        }

        // Elevation gain accumulation (only positive, with noise filter)
        if (location.hasAltitude()) {
            previousAltitude?.let { prevAlt ->
                val altDelta = location.altitude - prevAlt
                if (altDelta > ELEVATION_NOISE_THRESHOLD) {
                    elevationGain += altDelta
                }
            }
            previousAltitude = location.altitude
        }

        previousLocation = location
        trackPoints.add(point)

        // Compute metrics
        val activeDurationMs = SystemClock.elapsedRealtime() - trackingStartTime - pausedDuration
        val activeDurationHours = activeDurationMs / 3_600_000.0

        val avgSpeedKmh = if (activeDurationHours > 0) {
            (totalDistance / 1000.0) / activeDurationHours
        } else {
            0.0
        }

        // Current pace: minutes per kilometer from current speed
        val currentPace = if (location.speed > STATIONARY_SPEED_THRESHOLD) {
            // speed is m/s, convert to min/km: (1000 / speed) / 60 = 1000 / (speed * 60)
            1000.0 / (location.speed.toDouble() * 60.0)
        } else {
            0.0
        }

        _trackingState.value = GpsTrackingState(
            currentLocation = point,
            totalDistanceMeters = totalDistance,
            currentPaceMinPerKm = currentPace,
            averageSpeedKmh = avgSpeedKmh,
            elevationGainMeters = elevationGain,
            durationMs = max(0, activeDurationMs),
            points = trackPoints.toList(),
            isTracking = true,
            isPaused = false
        )

        _state.value = GpsTrackerState.Tracking(_trackingState.value)
    }

    private fun hasLocationPermission(ctx: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun resetState() {
        trackPoints.clear()
        totalDistance = 0.0
        elevationGain = 0.0
        previousLocation = null
        previousAltitude = null
        pausedDuration = 0L
        pauseStartTime = 0L
        _trackingState.value = GpsTrackingState()
    }
}
