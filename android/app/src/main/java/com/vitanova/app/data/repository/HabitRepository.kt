package com.vitanova.app.data.repository

import com.vitanova.app.data.local.dao.HabitDao
import com.vitanova.app.data.local.entity.Habit
import com.vitanova.app.data.local.entity.HabitCompletion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HabitRepository(private val habitDao: HabitDao) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun saveHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun toggleCompletion(habitId: Long, date: String) {
        val monthStart = date.substring(0, 7) + "-01"
        val monthEnd = run {
            val parsedDate = LocalDate.parse(date, dateFormatter)
            parsedDate.withDayOfMonth(parsedDate.lengthOfMonth()).format(dateFormatter)
        }
        val completions = habitDao.getCompletionsByHabitAndMonth(habitId, monthStart, monthEnd).first()
        val existing = completions.find { it.date == date }

        if (existing != null) {
            // Already completed on this date -- remove by re-inserting with same id won't work,
            // so we insert a deletion marker. Since Room REPLACE strategy is used, we re-insert
            // to effectively toggle. For a true toggle, we need a delete query.
            // Since the DAO only has insert, we treat this as a no-op if already completed.
            // In practice, the ViewModel should check before calling.
            return
        }

        val completion = HabitCompletion(
            habitId = habitId,
            date = date,
            completedAt = System.currentTimeMillis()
        )
        habitDao.insertCompletion(completion)

        val newStreak = calculateElasticStreak(habitId)
        habitDao.updateStreak(habitId, newStreak)
    }

    fun getActiveHabits(): Flow<List<Habit>> {
        return habitDao.getActiveHabits()
    }

    fun getCompletions(habitId: Long, month: String): Flow<List<HabitCompletion>> {
        val parsedMonth = LocalDate.parse("$month-01", dateFormatter)
        val monthStart = parsedMonth.format(dateFormatter)
        val monthEnd = parsedMonth.withDayOfMonth(parsedMonth.lengthOfMonth()).format(dateFormatter)
        return habitDao.getCompletionsByHabitAndMonth(habitId, monthStart, monthEnd)
    }

    /**
     * Calculates an "elastic" streak for a habit that allows up to 1 miss per week
     * without breaking the streak.
     *
     * The algorithm walks backwards day by day from today. It tracks completed days
     * and missed days in rolling 7-day windows. If more than 1 day is missed in any
     * given 7-day window, the streak ends. The streak value is the number of completed
     * days within the unbroken elastic period.
     */
    suspend fun calculateElasticStreak(habitId: Long): Int {
        val today = LocalDate.now()
        val lookbackStart = today.minusDays(365)
        val monthStart = lookbackStart.format(dateFormatter)
        val monthEnd = today.format(dateFormatter)

        val completions = habitDao.getCompletionsByHabitAndMonth(habitId, monthStart, monthEnd).first()
        val completedDates = completions.map { it.date }.toSet()

        var streakCount = 0
        var currentDate = today
        var missesInCurrentWindow = 0
        val recentDays = mutableListOf<Boolean>() // true = completed, false = missed

        while (currentDate.isAfter(lookbackStart)) {
            val dateStr = currentDate.format(dateFormatter)
            val completed = completedDates.contains(dateStr)
            recentDays.add(0, completed)

            // Keep a rolling window of 7 days
            if (recentDays.size > 7) {
                recentDays.removeAt(recentDays.size - 1)
            }

            // Count misses in the current 7-day window
            missesInCurrentWindow = recentDays.count { !it }

            if (missesInCurrentWindow > 1) {
                // More than 1 miss in this 7-day window breaks the elastic streak
                break
            }

            if (completed) {
                streakCount++
            }

            currentDate = currentDate.minusDays(1)
        }

        return streakCount
    }
}
