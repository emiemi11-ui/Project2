package com.vitanova.app.ui.fitness

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vitanova.app.VitaNovaApp
import com.vitanova.app.data.local.entity.FitnessActivity
import com.vitanova.app.data.local.entity.StepRecord
import com.vitanova.app.data.repository.FitnessRepository
import com.vitanova.app.sensor.GpsTracker
import com.vitanova.app.sensor.GpsTrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FitnessRepository = run {
        val db = (application as VitaNovaApp).database
        FitnessRepository(db.fitnessDao())
    }

    private var gpsTracker: GpsTracker? = null

    val todaySteps: StateFlow<Int> = repository.getTodaySteps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _stepGoal = MutableStateFlow(10_000)
    val stepGoal: StateFlow<Int> = _stepGoal.asStateFlow()

    val recentActivities: StateFlow<List<FitnessActivity>> = repository.getRecentActivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _currentTrackingState = MutableStateFlow(GpsTrackingState())
    val currentTrackingState: StateFlow<GpsTrackingState> = _currentTrackingState.asStateFlow()

    private val _currentActivityType = MutableStateFlow("running")
    val currentActivityType: StateFlow<String> = _currentActivityType.asStateFlow()

    private val _weeklyDistanceKm = MutableStateFlow(0f)
    val weeklyDistanceKm: StateFlow<Float> = _weeklyDistanceKm.asStateFlow()

    private val _weeklyCalories = MutableStateFlow(0)
    val weeklyCalories: StateFlow<Int> = _weeklyCalories.asStateFlow()

    private val _stepsRecords = repository.getStepsLast7Days()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _stepsRecords.collect { records ->
                val totalDistance = records.sumOf { (it.distanceMeters ?: 0f).toDouble() }
                _weeklyDistanceKm.value = (totalDistance / 1000.0).toFloat()
                _weeklyCalories.value = records.sumOf { it.caloriesBurned ?: 0 }
            }
        }
    }

    fun startGpsTracking(context: Context, activityType: String = "running") {
        _currentActivityType.value = activityType
        val tracker = GpsTracker(context)
        gpsTracker = tracker
        tracker.startTracking(context)
        _isTracking.value = true

        viewModelScope.launch {
            tracker.trackingState.collect { state ->
                _currentTrackingState.value = state
            }
        }
    }

    fun stopGpsTracking(context: Context) {
        val tracker = gpsTracker ?: return
        val state = _currentTrackingState.value
        tracker.stopTracking()
        _isTracking.value = false

        viewModelScope.launch {
            val activity = FitnessActivity(
                type = _currentActivityType.value,
                startTime = System.currentTimeMillis() - state.durationMs,
                endTime = System.currentTimeMillis(),
                durationMinutes = (state.durationMs / 60_000).toInt(),
                caloriesBurned = estimateCalories(
                    _currentActivityType.value,
                    state.durationMs,
                    state.totalDistanceMeters
                ),
                distanceMeters = state.totalDistanceMeters.toFloat(),
                avgPace = if (state.totalDistanceMeters > 0) {
                    ((state.durationMs / 60_000.0) / (state.totalDistanceMeters / 1000.0)).toFloat()
                } else null,
                elevationGainMeters = state.elevationGainMeters.toFloat()
            )
            val activityId = repository.saveActivity(activity)
            state.points.forEach { point ->
                repository.saveGpsPoint(
                    com.vitanova.app.data.local.entity.GpsPoint(
                        activityId = activityId,
                        timestamp = point.timestamp,
                        latitude = point.latitude,
                        longitude = point.longitude,
                        altitude = point.altitude,
                        speed = point.speed,
                        accuracy = point.accuracy
                    )
                )
            }
        }

        gpsTracker = null
    }

    fun pauseTracking() {
        val tracker = gpsTracker ?: return
        val state = _currentTrackingState.value
        if (state.isPaused) {
            tracker.resumeTracking()
        } else {
            tracker.pauseTracking()
        }
    }

    private fun estimateCalories(type: String, durationMs: Long, distanceMeters: Double): Int {
        val durationMinutes = durationMs / 60_000.0
        val metValue = when (type) {
            "running" -> 9.8
            "cycling" -> 7.5
            "walking" -> 3.8
            "swimming" -> 8.0
            else -> 5.0
        }
        // Approximate: MET * 3.5 * weightKg / 200 * durationMinutes
        // Using 70kg default weight
        return (metValue * 3.5 * 70.0 / 200.0 * durationMinutes).toInt()
    }
}
