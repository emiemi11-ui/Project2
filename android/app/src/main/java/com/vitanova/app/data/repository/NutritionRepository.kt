package com.vitanova.app.data.repository

import com.vitanova.app.data.local.dao.MacroSummary
import com.vitanova.app.data.local.dao.NutritionDao
import com.vitanova.app.data.local.entity.Meal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NutritionRepository(private val nutritionDao: NutritionDao) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun saveMeal(meal: Meal): Long {
        return nutritionDao.insertMeal(meal)
    }

    fun getTodayMeals(): Flow<List<Meal>> {
        val today = LocalDate.now().format(dateFormatter)
        return nutritionDao.getMealsByDate(today)
    }

    fun getTodayCalories(): Flow<Int> {
        val today = LocalDate.now().format(dateFormatter)
        return nutritionDao.getTodayCalories(today)
    }

    fun getTodayMacros(): Flow<MacroSummary> {
        val today = LocalDate.now().format(dateFormatter)
        return nutritionDao.getTodayMacros(today)
    }
}
