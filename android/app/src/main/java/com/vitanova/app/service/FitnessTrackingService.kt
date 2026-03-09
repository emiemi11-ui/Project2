package com.vitanova.app.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.vitanova.app.MainActivity
import com.vitanova.app.R
import com.vitanova.app.data.local.VitaNovaDatabase
import com.vitanova.app.data.local.entity.FitnessActivity
import com.vitanova.app.data.local.entity.GpsPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

class FitnessTrackingService : Service() {

    companion object {
        const val CHANNEL_ID = "fitness_tracking"
        const val NOTIFICATION_ID = 2001
        const val ACTION_STOP = "com.vitanova.app.action.STOP_FITNESS_TRACKING"
        const val ACTION_PAUSE = "com.vitanova.app.action.PAUSE_FITNESS_TRACKING"
        const val ACTION_RESUME = "com.vitanova.app.action.RESUME_FITNESS_TRACKING"
        const val EXTRA_ACTIVITY_TYPE = "activity_type"

        private const val UPDATE_INTERVAL_MS = 5_000L
        private const val LOCATION_INTERVAL_MS = 3_000L
        private const val LOCATION_MIN_DISPLACEMENT_M = 2f

        fun startIntent(context: Context, activityType: String = "running"): Intent {
            return Intent(context, FitnessTrackingService::class.java).apply {
                putExtra(EXTRA_ACTIVITY_TYPE, activityType)
            }
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, FitnessTrackingService::class.java).apply {
                action = ACTION_STOP
            }
        }

        fun pauseIntent(context: Context): Intent {
            return Intent(context, FitnessTrackingService::class.java).apply {
                action = ACTION_PAUSE
            }
        }

        fun resumeIntent(context: Context): Intent {
            return Intent(context, FitnessTrackingService::class.java).apply {
                action = ACTION_RESUME
            }
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var notificationUpdateJob: Job? = null

    private val gpsPoints = CopyOnWriteArrayList<GpsPoint>()
    private var activityType: String = "running"
    private var startTimeMillis: Long = 0L
    private var pauseTimeMillis: Long = 0L
    private var totalPausedDurationMs: Long = 0L
    private var isPaused: Boolean = false
    private var totalDistanceMeters: Float = 0f
    private var lastLocation: Location? = null

    private val db by lazy { VitaNovaDatabase.getInstance(applicationContext) }
    private val fitnessDao by lazy { db.fitnessDao() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopFitnessTracking()
                return START_NOT_STICKY
            }
            ACTION_PAUSE -> {
                pauseTracking()
                return START_STICKY
            }
            ACTION_RESUME -> {
                resumeTracking()
                return START_STICKY
            }
        }

        activityType = intent?.getStringExtra(EXTRA_ACTIVITY_TYPE) ?: "running"

        val notification = buildNotification(
            distance = "0.00 km",
            pace = "--:--",
            duration = "00:00"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startFitnessTracking()
        return START_STICKY
    }

    override fun onDestroy() {
        stopLocationUpdates()
        notificationUpdateJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startFitnessTracking() {
        startTimeMillis = System.currentTimeMillis()
        totalPausedDurationMs = 0L
        isPaused = false
        totalDistanceMeters = 0f
        lastLocation = null
        gpsPoints.clear()

        startLocationUpdates()
        startNotificationUpdater()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_INTERVAL_MS
        )
            .setMinUpdateDistanceMeters(LOCATION_MIN_DISPLACEMENT_M)
            .setWaitForAccurateLocation(false)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (isPaused) return
                for (location in result.locations) {
                    processNewLocation(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    private fun processNewLocation(location: Location) {
        val previous = lastLocation
        if (previous != null) {
            val delta = previous.distanceTo(location)
            if (delta < 100f) {
                totalDistanceMeters += delta
            }
        }
        lastLocation = location

        val point = GpsPoint(
            activityId = 0,
            timestamp = location.time,
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = if (location.hasAltitude()) location.altitude else null,
            speed = if (location.hasSpeed()) location.speed else null,
            accuracy = if (location.hasAccuracy()) location.accuracy else null
        )
        gpsPoints.add(point)
    }

    private fun startNotificationUpdater() {
        notificationUpdateJob = serviceScope.launch {
            while (true) {
                delay(UPDATE_INTERVAL_MS)
                if (!isPaused) {
                    updateNotification()
                }
            }
        }
    }

    private fun updateNotification() {
        val elapsedMs = getActiveElapsedTimeMs()
        val distanceKm = totalDistanceMeters / 1000f
        val durationStr = formatDuration(elapsedMs)
        val paceStr = calculatePace(totalDistanceMeters, elapsedMs)
        val distanceStr = String.format(Locale.getDefault(), "%.2f km", distanceKm)

        val notification = buildNotification(
            distance = distanceStr,
            pace = paceStr,
            duration = durationStr
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getActiveElapsedTimeMs(): Long {
        val now = if (isPaused) pauseTimeMillis else System.currentTimeMillis()
        return now - startTimeMillis - totalPausedDurationMs
    }

    private fun pauseTracking() {
        if (isPaused) return
        isPaused = true
        pauseTimeMillis = System.currentTimeMillis()

        val notification = buildNotification(
            distance = String.format(Locale.getDefault(), "%.2f km", totalDistanceMeters / 1000f),
            pace = "Pauza",
            duration = formatDuration(getActiveElapsedTimeMs())
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun resumeTracking() {
        if (!isPaused) return
        totalPausedDurationMs += System.currentTimeMillis() - pauseTimeMillis
        isPaused = false
    }

    private fun stopFitnessTracking() {
        stopLocationUpdates()
        notificationUpdateJob?.cancel()

        serviceScope.launch {
            val endTime = System.currentTimeMillis()
            val elapsedMs = getActiveElapsedTimeMs()
            val durationMinutes = (elapsedMs / 60_000).toInt()
            val distanceKm = totalDistanceMeters / 1000f

            val caloriesBurned = estimateCalories(activityType, durationMinutes, totalDistanceMeters)
            val avgPace = if (totalDistanceMeters > 0) {
                (elapsedMs / 60_000f) / (totalDistanceMeters / 1000f)
            } else null

            val activityId = fitnessDao.insertActivity(
                FitnessActivity(
                    type = activityType,
                    startTime = startTimeMillis,
                    endTime = endTime,
                    durationMinutes = durationMinutes,
                    caloriesBurned = caloriesBurned,
                    distanceMeters = totalDistanceMeters,
                    avgPace = avgPace,
                    notes = null
                )
            )

            for (point in gpsPoints) {
                fitnessDao.insertGpsPoint(
                    point.copy(activityId = activityId)
                )
            }

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }

    private fun estimateCalories(type: String, durationMinutes: Int, distanceMeters: Float): Int {
        val metValue = when (type) {
            "running" -> 9.8f
            "cycling" -> 7.5f
            "walking" -> 3.8f
            "swimming" -> 8.0f
            "gym" -> 6.0f
            else -> 5.0f
        }
        val weightKg = 70f
        return ((metValue * weightKg * 3.5f / 200f) * durationMinutes).toInt()
    }

    private fun calculatePace(distanceMeters: Float, elapsedMs: Long): String {
        if (distanceMeters < 10f || elapsedMs < 1000L) return "--:--"
        val minutesPerKm = (elapsedMs / 60_000f) / (distanceMeters / 1000f)
        val wholeMinutes = minutesPerKm.toInt()
        val seconds = ((minutesPerKm - wholeMinutes) * 60).toInt()
        return String.format(Locale.getDefault(), "%d:%02d /km", wholeMinutes, seconds)
    }

    private fun formatDuration(elapsedMs: Long): String {
        val totalSeconds = elapsedMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Monitorizare fitness",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificari pentru monitorizarea activitatilor fitness"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(distance: String, pace: String, duration: String): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopPendingIntent = PendingIntent.getService(
            this, 1,
            stopIntent(this),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumePendingIntent = if (isPaused) {
            PendingIntent.getService(
                this, 2,
                resumeIntent(this),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                this, 2,
                pauseIntent(this),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val pauseResumeLabel = if (isPaused) "Continua" else "Pauza"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VitaNova - ${activityType.replaceFirstChar { it.uppercase() }}")
            .setContentText("$distance | $pace | $duration")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_launcher_foreground, pauseResumeLabel, pauseResumePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .build()
    }
}
