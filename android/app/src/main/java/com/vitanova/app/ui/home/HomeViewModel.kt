package com.vitanova.app.ui.home

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vitanova.app.data.local.VitaNovaDatabase
import com.vitanova.app.data.local.dao.HabitWithStatus
import com.vitanova.app.data.repository.FitnessRepository
import com.vitanova.app.data.repository.FocusRepository
import com.vitanova.app.data.repository.HabitRepository
import com.vitanova.app.data.repository.HrvRepository
import com.vitanova.app.data.repository.SleepRepository
import com.vitanova.app.util.CircadianEngine
import com.vitanova.app.util.CircadianPhase
import com.vitanova.app.util.ReadinessCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

data class QuickAction(
    val label: String,
    val icon: String,
    val route: String
)

data class HabitSummary(
    val name: String,
    val completedToday: Boolean,
    val currentStreak: Int,
    val color: String?
)

data class HomeUiState(
    val readinessScore: Int = 0,
    val sleepScore: Int = 0,
    val hrvScore: Int = 0,
    val focusScore: Int = 0,
    val fitnessScore: Int = 0,
    val todaySteps: Int = 0,
    val currentPhase: CircadianPhase = CircadianPhase.MORNING,
    val greeting: String = "",
    val gradientColorStart: Long = 0xFFFFA502L,
    val gradientColorEnd: Long = 0xFFFF7979L,
    val quickActions: List<QuickAction> = emptyList(),
    val habitsSummary: List<HabitSummary> = emptyList(),
    val userName: String = "",
    val currentHour: Int = 12,
    val sleepHistory: List<Float> = emptyList(),
    val hrvHistory: List<Float> = emptyList(),
    val focusHistory: List<Float> = emptyList(),
    val fitnessHistory: List<Float> = emptyList(),
    val summaryText: String = ""
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = VitaNovaDatabase.getInstance(application)
    private val sleepRepository = SleepRepository(db.sleepDao())
    private val hrvRepository = HrvRepository(db.hrvDao())
    private val fitnessRepository = FitnessRepository(db.fitnessDao())
    private val focusRepository = FocusRepository(db.focusDao())
    private val habitRepository = HabitRepository(db.habitDao())

    private val prefs: SharedPreferences =
        application.getSharedPreferences("vitanova_prefs", android.content.Context.MODE_PRIVATE)

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCircadianState()
        loadQuickActions()
        loadUserName()
        observeData()
    }

    private fun loadCircadianState() {
        val state = CircadianEngine.getCurrentState()
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        _uiState.value = _uiState.value.copy(
            currentPhase = state.currentPhase,
            greeting = state.greeting,
            gradientColorStart = state.gradientColors.first,
            gradientColorEnd = state.gradientColors.second,
            currentHour = hour
        )
    }

    private fun loadQuickActions() {
        _uiState.value = _uiState.value.copy(
            quickActions = listOf(
                QuickAction(
                    label = "Măsoară HRV",
                    icon = "favorite",
                    route = "hrv_measure"
                ),
                QuickAction(
                    label = "Start Focus",
                    icon = "center_focus_strong",
                    route = "focus_timer"
                ),
                QuickAction(
                    label = "Start Workout",
                    icon = "fitness_center",
                    route = "fitness_start"
                )
            )
        )
    }

    private fun loadUserName() {
        val name = prefs.getString("user_name", "") ?: ""
        _uiState.value = _uiState.value.copy(userName = name)
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                sleepRepository.getLast7Sessions(),
                hrvRepository.getLast7Days(),
                fitnessRepository.getTodaySteps(),
                focusRepository.getTodayFocusSessions(),
                habitRepository.getActiveHabits()
            ) { sleepSessions, hrvReadings, todaySteps, focusSessions, habits ->
                // Sleep score: latest session's score
                val sleepScore = sleepSessions.firstOrNull()?.sleepScore ?: 0
                val sleepHistory = sleepSessions
                    .sortedBy { it.startTime }
                    .takeLast(7)
                    .map { it.sleepScore.toFloat() }

                // HRV score: latest reading's recovery score
                val hrvScore = hrvReadings.firstOrNull()?.recoveryScore ?: 0
                val hrvHistory = hrvReadings
                    .sortedBy { it.timestamp }
                    .takeLast(7)
                    .map { it.rmssd }

                // Focus score: based on completed sessions ratio
                val completedFocusSessions = focusSessions.count { it.completed }
                val focusGoal = 4
                val focusScore = ((completedFocusSessions.toFloat() / focusGoal.coerceAtLeast(1)) * 100)
                    .toInt()
                    .coerceIn(0, 100)
                val focusHistory = listOf(focusScore.toFloat()) // simplified for today

                // Fitness score: step-based approximation
                val stepGoal = prefs.getInt("step_goal", 10000)
                val fitnessScore = ((todaySteps.toFloat() / stepGoal.coerceAtLeast(1)) * 100)
                    .toInt()
                    .coerceIn(0, 100)
                val fitnessHistory = listOf(fitnessScore.toFloat())

                // Stress defaults to moderate (50) when no mood data
                val stressScore = 50

                // Habit momentum
                val habitMomentumScore = if (habits.isNotEmpty()) {
                    val avgStreak = habits.map { it.currentStreak }.average()
                    (avgStreak * 10).toInt().coerceIn(0, 100)
                } else 50

                // Readiness calculation
                val readinessResult = ReadinessCalculator.calculate(
                    sleepScore = sleepScore,
                    hrvScore = hrvScore,
                    stressScore = stressScore,
                    fitnessScore = fitnessScore,
                    habitMomentumScore = habitMomentumScore
                )

                // Habits summary: first 3
                val today = LocalDate.now().format(dateFormatter)
                val habitsSummary = habits.take(3).map { habit ->
                    HabitSummary(
                        name = habit.name,
                        completedToday = false, // Will be determined separately
                        currentStreak = habit.currentStreak,
                        color = habit.color
                    )
                }

                // Build summary text
                val summaryText = buildSummaryText(
                    readinessResult.score,
                    sleepScore,
                    todaySteps,
                    stepGoal,
                    completedFocusSessions
                )

                HomeUiState(
                    readinessScore = readinessResult.score,
                    sleepScore = sleepScore,
                    hrvScore = hrvScore,
                    focusScore = focusScore,
                    fitnessScore = fitnessScore,
                    todaySteps = todaySteps,
                    currentPhase = _uiState.value.currentPhase,
                    greeting = _uiState.value.greeting,
                    gradientColorStart = _uiState.value.gradientColorStart,
                    gradientColorEnd = _uiState.value.gradientColorEnd,
                    quickActions = _uiState.value.quickActions,
                    habitsSummary = habitsSummary,
                    userName = _uiState.value.userName,
                    currentHour = _uiState.value.currentHour,
                    sleepHistory = sleepHistory,
                    hrvHistory = hrvHistory,
                    focusHistory = focusHistory,
                    fitnessHistory = fitnessHistory,
                    summaryText = summaryText
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                _uiState.value
            ).collect { state ->
                _uiState.value = state
            }
        }

        // Observe habits with today status separately
        viewModelScope.launch {
            val today = LocalDate.now().format(dateFormatter)
            db.habitDao().getAllHabitsWithTodayStatus(today)
                .collect { habitsWithStatus ->
                    val summary = habitsWithStatus.take(3).map { h ->
                        HabitSummary(
                            name = h.name,
                            completedToday = h.completedToday,
                            currentStreak = h.currentStreak,
                            color = h.color
                        )
                    }
                    _uiState.value = _uiState.value.copy(habitsSummary = summary)
                }
        }
    }

    private fun buildSummaryText(
        readiness: Int,
        sleepScore: Int,
        steps: Int,
        stepGoal: Int,
        focusSessions: Int
    ): String {
        val parts = mutableListOf<String>()

        when {
            readiness >= 80 -> parts.add("Readiness excelent ($readiness/100)")
            readiness >= 60 -> parts.add("Readiness bun ($readiness/100)")
            readiness >= 40 -> parts.add("Readiness moderat ($readiness/100)")
            else -> parts.add("Readiness scăzut ($readiness/100)")
        }

        if (sleepScore > 0) {
            parts.add("Somn: $sleepScore/100")
        }

        val stepPercent = ((steps.toFloat() / stepGoal.coerceAtLeast(1)) * 100).toInt()
        parts.add("Pași: $steps ($stepPercent%)")

        if (focusSessions > 0) {
            parts.add("Focus: $focusSessions sesiuni")
        }

        return parts.joinToString(" • ")
    }
}
