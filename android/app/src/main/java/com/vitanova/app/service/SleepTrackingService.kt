package com.vitanova.app.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.vitanova.app.R
import com.vitanova.app.MainActivity
import com.vitanova.app.data.local.VitaNovaDatabase
import com.vitanova.app.data.local.entity.SleepSample
import com.vitanova.app.data.local.entity.SleepSession
import com.vitanova.app.sensor.AccelerometerManager
import com.vitanova.app.util.SleepAnalyzer
import com.vitanova.app.util.SleepSampleInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SleepTrackingService : Service() {

    companion object {
        const val CHANNEL_ID = "sleep_tracking"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.vitanova.app.action.STOP_SLEEP_TRACKING"
        const val EXTRA_ALARM_START_MILLIS = "alarm_start_millis"
        const val EXTRA_ALARM_END_MILLIS = "alarm_end_millis"
        const val EXTRA_SESSION_ID = "session_id"

        private const val SAMPLE_INTERVAL_MS = 5_000L
        private const val WINDOW_DURATION_MS = 5L * 60 * 1000
        private const val SAMPLES_PER_WINDOW = (WINDOW_DURATION_MS / SAMPLE_INTERVAL_MS).toInt()

        private const val LIGHT_SLEEP_MOVEMENT_THRESHOLD = 1.5f

        fun startIntent(context: Context, alarmStartMillis: Long = 0L, alarmEndMillis: Long = 0L): Intent {
            return Intent(context, SleepTrackingService::class.java).apply {
                putExtra(EXTRA_ALARM_START_MILLIS, alarmStartMillis)
                putExtra(EXTRA_ALARM_END_MILLIS, alarmEndMillis)
            }
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, SleepTrackingService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var accelerometerManager: AccelerometerManager
    private var samplingJob: Job? = null

    private val movementBuffer = mutableListOf<Float>()
    private val sleepSampleInputs = mutableListOf<SleepSampleInput>()

    private var sessionId: Long = 0L
    private var sessionStartTime: Long = 0L
    private var alarmStartMillis: Long = 0L
    private var alarmEndMillis: Long = 0L
    private var alarmTriggered = false

    private val db by lazy { VitaNovaDatabase.getInstance(applicationContext) }
    private val sleepDao by lazy { db.sleepDao() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        accelerometerManager = AccelerometerManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSleepTracking()
            return START_NOT_STICKY
        }

        alarmStartMillis = intent?.getLongExtra(EXTRA_ALARM_START_MILLIS, 0L) ?: 0L
        alarmEndMillis = intent?.getLongExtra(EXTRA_ALARM_END_MILLIS, 0L) ?: 0L

        val notification = buildNotification("VitaNova monitorizeaz\u0103 somnul")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startSleepTracking()
        return START_STICKY
    }

    override fun onDestroy() {
        stopSampling()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startSleepTracking() {
        sessionStartTime = System.currentTimeMillis()
        alarmTriggered = false
        movementBuffer.clear()
        sleepSampleInputs.clear()

        serviceScope.launch {
            val session = SleepSession(
                startTime = sessionStartTime,
                endTime = 0L,
                totalDurationMinutes = 0,
                efficiencyPercent = 0f,
                deepMinutes = 0,
                lightMinutes = 0,
                remMinutes = 0,
                awakeMinutes = 0,
                cyclesCount = 0,
                sleepScore = 0
            )
            sessionId = sleepDao.insert(session)
        }

        accelerometerManager.start()
        startSampling()
    }

    private fun startSampling() {
        samplingJob = serviceScope.launch {
            var sampleCount = 0

            while (true) {
                delay(SAMPLE_INTERVAL_MS)

                val currentMovement = accelerometerManager.movementIntensity.value
                movementBuffer.add(currentMovement)
                sampleCount++

                if (sampleCount >= SAMPLES_PER_WINDOW) {
                    processWindow()
                    sampleCount = 0
                }
            }
        }
    }

    private suspend fun processWindow() {
        if (movementBuffer.isEmpty()) return

        val avgMovement = movementBuffer.average().toFloat()
        val timestamp = System.currentTimeMillis()

        val stage = classifyMovementToStage(avgMovement)

        val sleepSample = SleepSample(
            sessionId = sessionId,
            timestamp = timestamp,
            stage = stage,
            movement = avgMovement
        )
        sleepDao.insert(sleepSample)

        sleepSampleInputs.add(SleepSampleInput(timestamp, avgMovement))

        checkSmartAlarm(avgMovement, stage)

        movementBuffer.clear()
    }

    private fun classifyMovementToStage(avgMovement: Float): String {
        return when {
            avgMovement < 0.3f -> "deep"
            avgMovement < 1.0f -> "light"
            avgMovement < 2.0f -> "rem"
            else -> "awake"
        }
    }

    private fun checkSmartAlarm(avgMovement: Float, stage: String) {
        if (alarmTriggered) return
        if (alarmStartMillis == 0L || alarmEndMillis == 0L) return

        val now = System.currentTimeMillis()
        if (now !in alarmStartMillis..alarmEndMillis) return

        val isLightSleep = stage == "light" || avgMovement >= LIGHT_SLEEP_MOVEMENT_THRESHOLD
        if (isLightSleep) {
            alarmTriggered = true
            triggerSmartAlarm()
        }
    }

    private fun triggerSmartAlarm() {
        triggerVibration()

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("smart_alarm_triggered", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            pendingIntent
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm\u0103 inteligent\u0103")
            .setContentText("Este momentul perfect s\u0103 te treze\u0219ti! E\u0219ti \u00een faza de somn u\u0219or.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(NOTIFICATION_ID + 1, alarmNotification)
    }

    private fun triggerVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 500, 200, 500, 200, 500),
                    -1
                )
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 200, 500, 200, 500),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
            }
        }
    }

    private fun stopSleepTracking() {
        stopSampling()
        accelerometerManager.stop()

        serviceScope.launch {
            val endTime = System.currentTimeMillis()
            val analysis = SleepAnalyzer.analyze(sleepSampleInputs)

            if (analysis != null) {
                val updatedSession = SleepSession(
                    id = sessionId,
                    startTime = sessionStartTime,
                    endTime = endTime,
                    totalDurationMinutes = analysis.totalDurationMinutes,
                    efficiencyPercent = analysis.efficiencyPercent,
                    deepMinutes = analysis.deepMinutes,
                    lightMinutes = analysis.lightMinutes,
                    remMinutes = analysis.remMinutes,
                    awakeMinutes = analysis.awakeMinutes,
                    cyclesCount = analysis.cyclesCount,
                    sleepScore = analysis.sleepScore
                )
                sleepDao.insert(updatedSession)
            } else {
                val durationMinutes = ((endTime - sessionStartTime) / 60000).toInt()
                val updatedSession = SleepSession(
                    id = sessionId,
                    startTime = sessionStartTime,
                    endTime = endTime,
                    totalDurationMinutes = durationMinutes,
                    efficiencyPercent = 0f,
                    deepMinutes = 0,
                    lightMinutes = 0,
                    remMinutes = 0,
                    awakeMinutes = 0,
                    cyclesCount = 0,
                    sleepScore = 0
                )
                sleepDao.insert(updatedSession)
            }

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopSampling() {
        samplingJob?.cancel()
        samplingJob = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Monitorizare somn",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notific\u0103ri pentru monitorizarea somnului"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopPendingIntent = PendingIntent.getService(
            this, 1,
            stopIntent(this),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VitaNova")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Opre\u0219te", stopPendingIntent)
            .build()
    }
}
