package com.vitanova.app.ui.focus

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vitanova.app.VitaNovaApp
import com.vitanova.app.data.local.entity.AppUsage
import com.vitanova.app.data.local.entity.FocusSession
import com.vitanova.app.data.repository.FocusRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

data class AppUsageUiModel(
    val appName: String,
    val packageName: String,
    val usageMinutes: Int,
    val category: String
)

data class CategoryBreakdown(
    val category: String,
    val minutes: Int,
    val percentage: Float
)

data class FocusUiState(
    val totalScreenTimeMinutes: Int = 0,
    val yesterdayScreenTimeMinutes: Int = 0,
    val topApps: List<AppUsageUiModel> = emptyList(),
    val focusSessionsToday: List<FocusSession> = emptyList(),
    val focusGoalPerDay: Int = 3,
    val currentTimerSeconds: Int = 0,
    val selectedDurationMinutes: Int = 25,
    val isTimerRunning: Boolean = false,
    val isTimerPaused: Boolean = false,
    val isTimerCompleted: Boolean = false,
    val focusStreak: Int = 0,
    val pickupsCount: Int = 0,
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val vulnerableHourMessage: String = "",
    val hasUsageStatsPermission: Boolean = false,
    val detoxStartHour: Int = 22,
    val detoxStartMinute: Int = 0,
    val isDetoxEnabled: Boolean = false,
    val weekendDetoxGoalHours: Int = 4,
    val detoxHistory: List<String> = emptyList()
)

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val database = VitaNovaApp.getInstance().database
    private val focusRepository = FocusRepository(database.focusDao())
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadLocalData()
        checkUsageStatsPermission()
    }

    private fun checkUsageStatsPermission() {
        val context = getApplication<Application>()
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 60_000L
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )
        _uiState.update { it.copy(hasUsageStatsPermission = stats.isNotEmpty()) }
    }

    private fun loadLocalData() {
        viewModelScope.launch {
            focusRepository.getTodayScreenTime().collect { totalMinutes ->
                _uiState.update { it.copy(totalScreenTimeMinutes = totalMinutes) }
            }
        }
        viewModelScope.launch {
            val today = LocalDate.now().format(dateFormatter)
            focusRepository.getTopApps(today).collect { apps ->
                val topApps = apps.take(5).map { app ->
                    AppUsageUiModel(
                        appName = app.appName,
                        packageName = app.packageName,
                        usageMinutes = app.usageMinutes,
                        category = app.category ?: "other"
                    )
                }
                _uiState.update { it.copy(topApps = topApps) }
            }
        }
        viewModelScope.launch {
            focusRepository.getTodayFocusSessions().collect { sessions ->
                val streak = calculateStreak()
                _uiState.update {
                    it.copy(
                        focusSessionsToday = sessions,
                        focusStreak = streak
                    )
                }
            }
        }
    }

    fun loadUsageStats(context: Context) {
        viewModelScope.launch {
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val packageManager = context.packageManager

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            val endOfDay = System.currentTimeMillis()

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, startOfDay, endOfDay
            )

            val today = LocalDate.now().format(dateFormatter)
            var totalMinutes = 0
            val appUsages = mutableListOf<AppUsageUiModel>()
            val categoryMinutes = mutableMapOf<String, Int>()

            val filteredStats = stats
                .filter { it.totalTimeInForeground > 60_000 }
                .sortedByDescending { it.totalTimeInForeground }

            for (stat in filteredStats) {
                val minutes = (stat.totalTimeInForeground / 60_000).toInt()
                totalMinutes += minutes

                val appName = try {
                    val appInfo = packageManager.getApplicationInfo(stat.packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    stat.packageName.substringAfterLast('.')
                }

                val category = categorizeApp(stat.packageName)

                categoryMinutes[category] = (categoryMinutes[category] ?: 0) + minutes

                appUsages.add(
                    AppUsageUiModel(
                        appName = appName,
                        packageName = stat.packageName,
                        usageMinutes = minutes,
                        category = category
                    )
                )

                val appUsageEntity = AppUsage(
                    date = today,
                    packageName = stat.packageName,
                    appName = appName,
                    usageMinutes = minutes,
                    category = category
                )
                focusRepository.saveAppUsage(appUsageEntity)
            }

            // Yesterday stats for comparison
            val yesterdayCal = Calendar.getInstance()
            yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
            yesterdayCal.set(Calendar.HOUR_OF_DAY, 0)
            yesterdayCal.set(Calendar.MINUTE, 0)
            yesterdayCal.set(Calendar.SECOND, 0)
            val yesterdayStart = yesterdayCal.timeInMillis
            yesterdayCal.set(Calendar.HOUR_OF_DAY, 23)
            yesterdayCal.set(Calendar.MINUTE, 59)
            yesterdayCal.set(Calendar.SECOND, 59)
            val yesterdayEnd = yesterdayCal.timeInMillis

            val yesterdayStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, yesterdayStart, yesterdayEnd
            )
            val yesterdayTotal = yesterdayStats
                .filter { it.totalTimeInForeground > 60_000 }
                .sumOf { (it.totalTimeInForeground / 60_000).toInt() }

            // Pickups estimation from usage events
            val pickups = estimatePickups(usageStatsManager, startOfDay, endOfDay)

            // Vulnerable hours
            val vulnerableMsg = findVulnerableHours(usageStatsManager, startOfDay, endOfDay)

            // Category breakdown
            val totalCatMinutes = categoryMinutes.values.sum().coerceAtLeast(1)
            val breakdown = categoryMinutes.map { (cat, min) ->
                CategoryBreakdown(
                    category = cat,
                    minutes = min,
                    percentage = min.toFloat() / totalCatMinutes
                )
            }.sortedByDescending { it.minutes }

            _uiState.update {
                it.copy(
                    totalScreenTimeMinutes = totalMinutes,
                    yesterdayScreenTimeMinutes = yesterdayTotal,
                    topApps = appUsages.take(5),
                    pickupsCount = pickups,
                    categoryBreakdown = breakdown,
                    vulnerableHourMessage = vulnerableMsg,
                    hasUsageStatsPermission = true
                )
            }
        }
    }

    private fun categorizeApp(packageName: String): String {
        return when {
            packageName.contains("instagram") || packageName.contains("facebook") ||
                    packageName.contains("twitter") || packageName.contains("tiktok") ||
                    packageName.contains("snapchat") || packageName.contains("whatsapp") ||
                    packageName.contains("telegram") || packageName.contains("messenger") ||
                    packageName.contains("reddit") -> "Social"

            packageName.contains("youtube") || packageName.contains("netflix") ||
                    packageName.contains("spotify") || packageName.contains("twitch") ||
                    packageName.contains("disney") || packageName.contains("hbo") ||
                    packageName.contains("music") || packageName.contains("video") ||
                    packageName.contains("game") -> "Entertainment"

            packageName.contains("docs") || packageName.contains("sheets") ||
                    packageName.contains("slides") || packageName.contains("calendar") ||
                    packageName.contains("notion") || packageName.contains("slack") ||
                    packageName.contains("office") || packageName.contains("outlook") ||
                    packageName.contains("teams") || packageName.contains("drive") ||
                    packageName.contains("mail") || packageName.contains("gmail") -> "Productive"

            else -> "Other"
        }
    }

    private fun estimatePickups(
        usageStatsManager: UsageStatsManager,
        startTime: Long,
        endTime: Long
    ): Int {
        return try {
            val events = usageStatsManager.queryEvents(startTime, endTime)
            var count = 0
            val event = android.app.usage.UsageEvents.Event()
            var lastResumeTime = 0L
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) {
                    if (event.timeStamp - lastResumeTime > 30_000) {
                        count++
                    }
                    lastResumeTime = event.timeStamp
                }
            }
            count
        } catch (e: Exception) {
            0
        }
    }

    private fun findVulnerableHours(
        usageStatsManager: UsageStatsManager,
        startTime: Long,
        endTime: Long
    ): String {
        return try {
            val events = usageStatsManager.queryEvents(startTime, endTime)
            val hourlyUsage = IntArray(24)
            val hourlyApps = mutableMapOf<Int, MutableMap<String, Long>>()
            val event = android.app.usage.UsageEvents.Event()

            var lastPackage: String? = null
            var lastTimestamp = 0L

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                when (event.eventType) {
                    android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED -> {
                        lastPackage = event.packageName
                        lastTimestamp = event.timeStamp
                    }
                    android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED -> {
                        if (lastPackage == event.packageName && lastTimestamp > 0) {
                            val duration = event.timeStamp - lastTimestamp
                            val cal = Calendar.getInstance().apply { timeInMillis = lastTimestamp }
                            val hour = cal.get(Calendar.HOUR_OF_DAY)
                            hourlyUsage[hour] += (duration / 60_000).toInt()
                            val apps = hourlyApps.getOrPut(hour) { mutableMapOf() }
                            apps[event.packageName] =
                                (apps[event.packageName] ?: 0L) + duration / 60_000
                        }
                        lastPackage = null
                        lastTimestamp = 0
                    }
                }
            }

            val peakHour = hourlyUsage.indices.maxByOrNull { hourlyUsage[it] } ?: 0
            val peakMinutes = hourlyUsage[peakHour]
            if (peakMinutes > 10) {
                val topApp = hourlyApps[peakHour]?.maxByOrNull { it.value }
                val appLabel = topApp?.key?.substringAfterLast('.') ?: "telefon"
                val endHour = (peakHour + 2).coerceAtMost(23)
                "Intre $peakHour-$endHour ai folosit $appLabel $peakMinutes min"
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun calculateStreak(): Int {
        var streak = 0
        var date = LocalDate.now()
        for (i in 0 until 365) {
            val dateStr = date.format(dateFormatter)
            val sessions = try {
                focusRepository.getTodayFocusSessions().first()
            } catch (e: Exception) {
                emptyList()
            }
            val completedOnDate = sessions.any { it.date == dateStr && it.completed }
            if (i == 0 || completedOnDate) {
                if (completedOnDate) streak++
                else if (i > 0) break
            }
            date = date.minusDays(1)
        }
        return streak
    }

    fun startFocusTimer(durationMinutes: Int) {
        timerJob?.cancel()
        val totalSeconds = durationMinutes * 60
        _uiState.update {
            it.copy(
                currentTimerSeconds = totalSeconds,
                selectedDurationMinutes = durationMinutes,
                isTimerRunning = true,
                isTimerPaused = false,
                isTimerCompleted = false
            )
        }
        timerJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000L)
                if (!_uiState.value.isTimerPaused) {
                    remaining--
                    _uiState.update { it.copy(currentTimerSeconds = remaining) }
                }
            }
            onTimerCompleted()
        }
    }

    fun pauseTimer() {
        _uiState.update {
            it.copy(isTimerPaused = !it.isTimerPaused)
        }
    }

    fun stopFocusTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update {
            it.copy(
                isTimerRunning = false,
                isTimerPaused = false,
                currentTimerSeconds = 0,
                isTimerCompleted = false
            )
        }
    }

    private fun onTimerCompleted() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val durationMinutes = _uiState.value.selectedDurationMinutes
            val session = FocusSession(
                date = LocalDate.now().format(dateFormatter),
                startTime = now - (durationMinutes * 60_000L),
                endTime = now,
                durationMinutes = durationMinutes,
                sessionType = "pomodoro",
                completed = true,
                distractionsCount = 0
            )
            focusRepository.saveSession(session)
            _uiState.update {
                it.copy(
                    isTimerRunning = false,
                    isTimerPaused = false,
                    isTimerCompleted = true,
                    focusStreak = it.focusStreak + 1
                )
            }
        }
    }

    fun dismissCompletion() {
        _uiState.update { it.copy(isTimerCompleted = false, currentTimerSeconds = 0) }
    }

    fun setDetoxTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(detoxStartHour = hour, detoxStartMinute = minute) }
    }

    fun toggleDetox(enabled: Boolean) {
        _uiState.update { it.copy(isDetoxEnabled = enabled) }
    }

    fun setWeekendDetoxGoal(hours: Int) {
        _uiState.update { it.copy(weekendDetoxGoalHours = hours) }
    }

    fun setSelectedDuration(minutes: Int) {
        if (!_uiState.value.isTimerRunning) {
            _uiState.update {
                it.copy(
                    selectedDurationMinutes = minutes,
                    currentTimerSeconds = minutes * 60
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
