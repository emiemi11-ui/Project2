package com.vitanova.app.ui.energy

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vitanova.app.data.local.VitaNovaDatabase
import com.vitanova.app.data.local.entity.HrvReading
import com.vitanova.app.data.repository.HrvRepository
import com.vitanova.app.util.HrvCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EnergyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = VitaNovaDatabase.getInstance(application)
    private val hrvRepository = HrvRepository(database.hrvDao())
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val latestHrv: StateFlow<HrvReading?> = hrvRepository.getLatest()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val last7DaysHrv: StateFlow<List<HrvReading>> = hrvRepository.getLast7Days()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _energyScore = MutableStateFlow(0)
    val energyScore: StateFlow<Int> = _energyScore.asStateFlow()

    private val _stressScore = MutableStateFlow(0)
    val stressScore: StateFlow<Int> = _stressScore.asStateFlow()

    private val _isMeasuring = MutableStateFlow(false)
    val isMeasuring: StateFlow<Boolean> = _isMeasuring.asStateFlow()

    init {
        viewModelScope.launch {
            latestHrv.collect { reading ->
                if (reading != null) {
                    _stressScore.value = reading.stressIndex.toInt().coerceIn(0, 100)
                    _energyScore.value = reading.recoveryScore.coerceIn(0, 100)
                } else {
                    _stressScore.value = 0
                    _energyScore.value = 0
                }
            }
        }
    }

    fun startHrvMeasurement() {
        _isMeasuring.value = true
    }

    fun saveMeasurement(bpm: Int, rmssd: Float) {
        viewModelScope.launch {
            val rrIntervals = buildList {
                if (bpm > 0) {
                    val avgRr = 60_000.0 / bpm
                    repeat(60) { add(avgRr) }
                }
            }

            val hrvResult = HrvCalculator.calculate(rrIntervals)
            val now = Instant.now().toEpochMilli()
            val today = LocalDate.now().format(dateFormatter)

            val reading = HrvReading(
                timestamp = now,
                date = today,
                rmssd = hrvResult?.rmssd?.toFloat() ?: rmssd,
                sdnn = hrvResult?.sdnn?.toFloat() ?: (rmssd * 0.8f),
                lfHfRatio = null,
                stressIndex = hrvResult?.stressScore?.toFloat() ?: 50f,
                recoveryScore = hrvResult?.energyScore ?: 50,
                measurementDurationSeconds = 60
            )

            hrvRepository.saveReading(reading)
            _isMeasuring.value = false
        }
    }
}
