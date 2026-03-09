package com.vitanova.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vitanova.app.MainActivity
import com.vitanova.app.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class ReminderType(
    val channelId: String,
    val channelName: String,
    val title: String,
    val message: String,
    val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
) {
    HRV_MORNING(
        channelId = "reminder_hrv",
        channelName = "Reminder HRV dimineata",
        title = "Masurare HRV",
        message = "Buna dimineata! Este momentul sa iti masori HRV-ul. O masurare constanta te ajuta sa iti intelegi mai bine recuperarea.",
        importance = NotificationManager.IMPORTANCE_DEFAULT
    ),
    HYDRATION(
        channelId = "reminder_hydration",
        channelName = "Reminder hidratare",
        title = "Hidratare",
        message = "Nu uita sa bei apa! Hidratarea corecta imbunatateste energia si concentrarea.",
        importance = NotificationManager.IMPORTANCE_LOW
    ),
    SCREEN_TIME_ALERT(
        channelId = "reminder_screen_time",
        channelName = "Alerta timp ecran",
        title = "Timp de ecran ridicat",
        message = "Ai petrecut mult timp pe telefon astazi. Ia o pauza si misca-te putin!",
        importance = NotificationManager.IMPORTANCE_DEFAULT
    ),
    BEDTIME(
        channelId = "reminder_bedtime",
        channelName = "Reminder somn",
        title = "Ora de culcare",
        message = "Se apropie ora de culcare. Incepe rutina de somn: reduceti lumina, evitati ecranele si relaxati-va.",
        importance = NotificationManager.IMPORTANCE_DEFAULT
    )
}

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val KEY_REMINDER_TYPE = "reminder_type"
        private const val NOTIFICATION_ID_BASE = 4000

        fun schedule(
            context: Context,
            reminderType: ReminderType,
            hourOfDay: Int,
            minute: Int = 0,
            intervalHours: Long = 24
        ) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val initialDelayMs = target.timeInMillis - now.timeInMillis

            val inputData = androidx.work.Data.Builder()
                .putString(KEY_REMINDER_TYPE, reminderType.name)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                intervalHours, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            val workName = "reminder_${reminderType.name.lowercase()}"
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                workName,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun scheduleAllDefaults(context: Context) {
            schedule(context, ReminderType.HRV_MORNING, hourOfDay = 7, minute = 0)
            schedule(context, ReminderType.HYDRATION, hourOfDay = 9, minute = 0, intervalHours = 2)
            schedule(context, ReminderType.SCREEN_TIME_ALERT, hourOfDay = 20, minute = 0)
            schedule(context, ReminderType.BEDTIME, hourOfDay = 22, minute = 0)
        }

        fun cancelReminder(context: Context, reminderType: ReminderType) {
            val workName = "reminder_${reminderType.name.lowercase()}"
            WorkManager.getInstance(context).cancelUniqueWork(workName)
        }

        fun cancelAll(context: Context) {
            ReminderType.entries.forEach { cancelReminder(context, it) }
        }
    }

    override suspend fun doWork(): Result {
        val typeName = inputData.getString(KEY_REMINDER_TYPE) ?: return Result.failure()
        val reminderType = try {
            ReminderType.valueOf(typeName)
        } catch (e: IllegalArgumentException) {
            return Result.failure()
        }

        createNotificationChannel(reminderType)
        showNotification(reminderType)

        return Result.success()
    }

    private fun createNotificationChannel(reminderType: ReminderType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                reminderType.channelId,
                reminderType.channelName,
                reminderType.importance
            ).apply {
                description = "Notificari de tip ${reminderType.channelName}"
            }
            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(reminderType: ReminderType) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("reminder_type", reminderType.name)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, reminderType.ordinal, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, reminderType.channelId)
            .setContentTitle(reminderType.title)
            .setContentText(reminderType.message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(mapImportanceToPriority(reminderType.importance))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminderType.message))
            .build()

        val notificationId = NOTIFICATION_ID_BASE + reminderType.ordinal
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun mapImportanceToPriority(importance: Int): Int {
        return when (importance) {
            NotificationManager.IMPORTANCE_HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationManager.IMPORTANCE_LOW -> NotificationCompat.PRIORITY_LOW
            NotificationManager.IMPORTANCE_MIN -> NotificationCompat.PRIORITY_MIN
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }
}
