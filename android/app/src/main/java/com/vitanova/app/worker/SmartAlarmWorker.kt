package com.vitanova.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vitanova.app.MainActivity
import com.vitanova.app.R
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

class SmartAlarmWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "smart_alarm"
        const val NOTIFICATION_ID = 3001
        private const val KEY_ALARM_START = "alarm_start_millis"
        private const val KEY_ALARM_END = "alarm_end_millis"

        private const val LIGHT_SLEEP_THRESHOLD = 1.5f
        private const val SAMPLE_DURATION_MS = 30_000L
        private const val CHECK_INTERVAL_MS = 60_000L

        fun schedule(context: Context, alarmStartMillis: Long, alarmEndMillis: Long) {
            val now = System.currentTimeMillis()
            val delayMs = (alarmStartMillis - now).coerceAtLeast(0L)

            val inputData = androidx.work.Data.Builder()
                .putLong(KEY_ALARM_START, alarmStartMillis)
                .putLong(KEY_ALARM_END, alarmEndMillis)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<SmartAlarmWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    override suspend fun doWork(): Result {
        val alarmStart = inputData.getLong(KEY_ALARM_START, 0L)
        val alarmEnd = inputData.getLong(KEY_ALARM_END, 0L)

        if (alarmStart == 0L || alarmEnd == 0L) return Result.failure()

        createNotificationChannel()

        while (System.currentTimeMillis() < alarmEnd) {
            val movement = sampleMovement()

            if (movement != null && isLightSleep(movement)) {
                triggerAlarm()
                return Result.success()
            }

            if (System.currentTimeMillis() + CHECK_INTERVAL_MS >= alarmEnd) {
                triggerAlarm()
                return Result.success()
            }

            delay(CHECK_INTERVAL_MS)
        }

        triggerAlarm()
        return Result.success()
    }

    private suspend fun sampleMovement(): Float? {
        val sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: return null

        val readings = mutableListOf<Float>()
        val done = CompletableDeferred<Unit>()

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt(x * x + y * y + z * z)
                synchronized(readings) {
                    readings.add(magnitude)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        withTimeoutOrNull(SAMPLE_DURATION_MS) {
            delay(SAMPLE_DURATION_MS)
        }

        sensorManager.unregisterListener(listener)

        synchronized(readings) {
            if (readings.isEmpty()) return null
            return readings.average().toFloat()
        }
    }

    private fun isLightSleep(avgMovement: Float): Boolean {
        return avgMovement >= LIGHT_SLEEP_THRESHOLD
    }

    private fun triggerAlarm() {
        triggerVibration()
        showAlarmNotification()
    }

    private fun triggerVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                    as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 500, 200, 500, 200, 500, 200, 800),
                    -1
                )
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 200, 500, 200, 500, 200, 800),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500, 200, 800), -1)
            }
        }
    }

    private fun showAlarmNotification() {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("smart_alarm_triggered", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Alarma inteligenta")
            .setContentText("Este momentul perfect sa te trezesti! Esti in faza de somn usor.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarma inteligenta",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificari pentru alarma inteligenta de trezire"
                enableVibration(true)
            }
            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
