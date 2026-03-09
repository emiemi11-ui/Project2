package com.vitanova.app.ui.nutrition

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vitanova.app.VitaNovaApp
import com.vitanova.app.data.local.dao.DailyCalories
import com.vitanova.app.data.local.dao.MacroSummary
import com.vitanova.app.data.local.entity.Meal
import com.vitanova.app.data.repository.NutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class NutritionUiState(
    val todayMeals: List<Meal> = emptyList(),
    val todayCalories: Int = 0,
    val calorieGoal: Int = 2000,
    val todayMacros: MacroSummary = MacroSummary(0f, 0f, 0f, 0f),
    val waterGlasses: Int = 0,
    val weeklyCalories: List<DailyCalories> = emptyList(),
    val isLoading: Boolean = false
)

class NutritionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = VitaNovaApp.getInstance().database
    private val nutritionRepository = NutritionRepository(database.nutritionDao())
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    init {
        loadTodayData()
    }

    fun loadTodayData() {
        viewModelScope.launch {
            nutritionRepository.getTodayMeals().collect { meals ->
                _uiState.update { it.copy(todayMeals = meals) }
            }
        }
        viewModelScope.launch {
            nutritionRepository.getTodayCalories().collect { calories ->
                _uiState.update { it.copy(todayCalories = calories) }
            }
        }
        viewModelScope.launch {
            nutritionRepository.getTodayMacros().collect { macros ->
                _uiState.update { it.copy(todayMacros = macros) }
            }
        }
        viewModelScope.launch {
            val sevenDaysAgo = LocalDate.now().minusDays(7).format(dateFormatter)
            database.nutritionDao().getLast7DaysCalories(sevenDaysAgo).collect { weekly ->
                _uiState.update { it.copy(weeklyCalories = weekly) }
            }
        }
    }

    fun addMeal(meal: Meal) {
        viewModelScope.launch {
            nutritionRepository.saveMeal(meal)
        }
    }

    fun addWater() {
        _uiState.update { state ->
            if (state.waterGlasses < 8) {
                state.copy(waterGlasses = state.waterGlasses + 1)
            } else {
                state
            }
        }
    }

    fun removeWater() {
        _uiState.update { state ->
            if (state.waterGlasses > 0) {
                state.copy(waterGlasses = state.waterGlasses - 1)
            } else {
                state
            }
        }
    }
}
