package com.vitanova.app.worker

import android.content.Context
import android.content.SharedPreferences
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vitanova.app.data.local.VitaNovaDatabase
import com.vitanova.app.util.ReadinessCalculator
import com.vitanova.app.util.ReadinessInput
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class DailyScoreWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "daily_score_worker"
        private const val PREFS_NAME = "vitanova_readiness"
        private const val KEY_PREFIX_SCORE = "readiness_score_"
        private const val KEY_PREFIX_LEVEL = "readiness_level_"

        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 7)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val initialDelayMs = target.timeInMillis - now.timeInMillis

            val workRequest = PeriodicWorkRequestBuilder<DailyScoreWorker>(
                1, TimeUnit.DAYS
            )
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val db = VitaNovaDatabase.getInstance(applicationContext)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(System.currentTimeMillis())

            val sleepScore = fetchSleepScore(db)
            val hrvScore = fetchHrvScore(db)
            val stressScore = fetchStressScore(db, today)
            val fitnessScore = fetchFitnessScore(db)
            val habitMomentumScore = fetchHabitMomentumScore(db, today)

            val readinessResult = ReadinessCalculator.calculate(
                ReadinessInput(
                    sleepScore = sleepScore,
                    hrvScore = hrvScore,
                    stressScore = stressScore,
                    fitnessScore = fitnessScore,
                    habitMomentumScore = habitMomentumScore
                )
            )

            val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt("${KEY_PREFIX_SCORE}$today", readinessResult.score)
                .putString("${KEY_PREFIX_LEVEL}$today", readinessResult.level.name)
                .apply()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun fetchSleepScore(db: VitaNovaDatabase): Int {
        val sessions = db.sleepDao().getLast7Sessions().firstOrNull() ?: emptyList()
        if (sessions.isEmpty()) return 50
        return sessions.first().sleepScore
    }

    private suspend fun fetchHrvScore(db: VitaNovaDatabase): Int {
        val latestHrv = db.hrvDao().getLatest().firstOrNull() ?: return 50
        return latestHrv.recoveryScore.coerceIn(0, 100)
    }

    private suspend fun fetchStressScore(db: VitaNovaDatabase, today: String): Int {
        val moods = db.moodDao().getByDate(today).firstOrNull() ?: emptyList()
        if (moods.isEmpty()) return 50
        val avgStress = moods.map { it.stressLevel }.average()
        return (avgStress * 10).toInt().coerceIn(0, 100)
    }

    private suspend fun fetchFitnessScore(db: VitaNovaDatabase): Int {
        val activities = db.fitnessDao().getRecentActivities(7).firstOrNull() ?: emptyList()
        if (activities.isEmpty()) return 30

        val totalMinutes = activities.sumOf { it.durationMinutes }
        val targetMinutes = 150
        val score = ((totalMinutes.toFloat() / targetMinutes) * 100).toInt()
        return score.coerceIn(0, 100)
    }

    private suspend fun fetchHabitMomentumScore(db: VitaNovaDatabase, today: String): Int {
        val habitsWithStatus = db.habitDao().getAllHabitsWithTodayStatus(today)
            .firstOrNull() ?: emptyList()

        if (habitsWithStatus.isEmpty()) return 50

        val avgStreak = habitsWithStatus.map { it.currentStreak }.average()
        val completionRate = habitsWithStatus.count { it.completedToday }.toFloat() /
                habitsWithStatus.size

        val streakScore = (avgStreak * 10).toInt().coerceIn(0, 100)
        val completionScore = (completionRate * 100).toInt()

        return ((streakScore * 0.6f) + (completionScore * 0.4f)).toInt().coerceIn(0, 100)
    }
}
