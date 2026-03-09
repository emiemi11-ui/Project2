package com.vitanova.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vitanova.app.data.local.entity.Habit
import com.vitanova.app.data.local.entity.HabitCompletion
import kotlinx.coroutines.flow.Flow

data class HabitWithStatus(
    val id: Long,
    val name: String,
    val description: String?,
    val icon: String?,
    val color: String?,
    val frequency: String,
    val targetDaysPerWeek: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val isActive: Boolean,
    val createdAt: Long,
    val completedToday: Boolean
)

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion): Long

    @Query("SELECT * FROM habits WHERE is_active = 1 ORDER BY created_at ASC")
    fun getActiveHabits(): Flow<List<Habit>>

    @Query(
        """
        SELECT * FROM habit_completions
        WHERE habit_id = :habitId
          AND date BETWEEN :monthStart AND :monthEnd
        ORDER BY date ASC
        """
    )
    fun getCompletionsByHabitAndMonth(
        habitId: Long,
        monthStart: String,
        monthEnd: String
    ): Flow<List<HabitCompletion>>

    @Query(
        """
        UPDATE habits
        SET current_streak = :streak,
            best_streak = CASE WHEN :streak > best_streak THEN :streak ELSE best_streak END
        WHERE id = :habitId
        """
    )
    suspend fun updateStreak(habitId: Long, streak: Int)

    @Query(
        """
        SELECT h.id, h.name, h.description, h.icon, h.color, h.frequency,
               h.target_days_per_week AS targetDaysPerWeek,
               h.current_streak AS currentStreak,
               h.best_streak AS bestStreak,
               h.is_active AS isActive,
               h.created_at AS createdAt,
               CASE WHEN hc.id IS NOT NULL THEN 1 ELSE 0 END AS completedToday
        FROM habits h
        LEFT JOIN habit_completions hc ON h.id = hc.habit_id AND hc.date = :today
        WHERE h.is_active = 1
        ORDER BY h.created_at ASC
        """
    )
    fun getAllHabitsWithTodayStatus(today: String): Flow<List<HabitWithStatus>>
}
