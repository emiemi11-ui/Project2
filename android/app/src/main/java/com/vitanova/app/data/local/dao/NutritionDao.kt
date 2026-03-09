package com.vitanova.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vitanova.app.data.local.entity.Meal
import kotlinx.coroutines.flow.Flow

data class MacroSummary(
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val totalFiber: Float
)

data class DailyCalories(
    val date: String,
    val totalCalories: Int
)

@Dao
interface NutritionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal): Long

    @Query("SELECT * FROM meals WHERE date = :date ORDER BY timestamp ASC")
    fun getMealsByDate(date: String): Flow<List<Meal>>

    @Query("SELECT COALESCE(SUM(calories), 0) FROM meals WHERE date = :today")
    fun getTodayCalories(today: String): Flow<Int>

    @Query(
        """
        SELECT COALESCE(SUM(protein_grams), 0.0) AS totalProtein,
               COALESCE(SUM(carbs_grams), 0.0) AS totalCarbs,
               COALESCE(SUM(fat_grams), 0.0) AS totalFat,
               COALESCE(SUM(fiber_grams), 0.0) AS totalFiber
        FROM meals
        WHERE date = :today
        """
    )
    fun getTodayMacros(today: String): Flow<MacroSummary>

    @Query(
        """
        SELECT date, SUM(calories) AS totalCalories
        FROM meals
        WHERE date >= :sevenDaysAgo
        GROUP BY date
        ORDER BY date DESC
        """
    )
    fun getLast7DaysCalories(sevenDaysAgo: String): Flow<List<DailyCalories>>
}
