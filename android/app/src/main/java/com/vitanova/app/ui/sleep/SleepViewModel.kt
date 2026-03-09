package com.vitanova.app.ui.sleep

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vitanova.app.data.local.VitaNovaDatabase
import com.vitanova.app.data.local.entity.SleepSample
import com.vitanova.app.data.local.entity.SleepSession
import com.vitanova.app.data.repository.SleepRepository
import com.vitanova.app.service.SleepTrackingService
import com.vitanova.app.util.SleepAnalyzer
import com.vitanova.app.util.SleepSampleInput
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SleepViewModel(application: Application) : AndroidViewModel(application) {

    private val db = VitaNovaDatabase.getInstance(application)
    private val repository = SleepRepository(db.sleepDao())

    val latestSession: StateFlow<SleepSession?> = repository.getLatestSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val last7Sessions: StateFlow<List<SleepSession>> = repository.getLast7Sessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val sleepSamples: StateFlow<List<SleepSample>> = latestSession
        .flatMapLatest { session ->
            if (session != null) repository.getSamples(session.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _smartAlarmEnabled = MutableStateFlow(false)
    val smartAlarmEnabled: StateFlow<Boolean> = _smartAlarmEnabled.asStateFlow()

    private val _alarmTimeStart = MutableStateFlow(0L)
    val alarmTimeStart: StateFlow<Long> = _alarmTimeStart.asStateFlow()

    private val _alarmTimeEnd = MutableStateFlow(0L)
    val alarmTimeEnd: StateFlow<Long> = _alarmTimeEnd.asStateFlow()

    private val _trackingStartTime = MutableStateFlow(0L)
    val trackingStartTime: StateFlow<Long> = _trackingStartTime.asStateFlow()

    fun startTracking(context: Context) {
        _isTracking.value = true
        _trackingStartTime.value = System.currentTimeMillis()

        val intent = SleepTrackingService.startIntent(
            context,
            alarmStartMillis = _alarmTimeStart.value,
            alarmEndMillis = _alarmTimeEnd.value
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopTracking(context: Context) {
        _isTracking.value = false
        _trackingStartTime.value = 0L

        val intent = SleepTrackingService.stopIntent(context)
        context.startService(intent)
    }

    fun setSmartAlarm(start: Long, end: Long) {
        _alarmTimeStart.value = start
        _alarmTimeEnd.value = end
        _smartAlarmEnabled.value = start > 0L && end > 0L
    }

    fun toggleSmartAlarm(enabled: Boolean) {
        _smartAlarmEnabled.value = enabled
        if (!enabled) {
            _alarmTimeStart.value = 0L
            _alarmTimeEnd.value = 0L
        }
    }
}
