package com.vitanova.app.service

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings

enum class AppCategory {
    SOCIAL,
    PRODUCTIVITY,
    ENTERTAINMENT,
    OTHER
}

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val category: AppCategory,
    val usageMinutes: Int
)

data class ScreenTimeSummary(
    val totalScreenTimeMinutes: Int,
    val pickupCount: Int,
    val appUsageList: List<AppUsageInfo>
)

class UsageStatsService(private val context: Context) {

    companion object {
        private val SOCIAL_PACKAGES = setOf(
            "com.instagram.android",
            "com.facebook.katana",
            "com.facebook.orca",
            "com.zhiliaoapp.musically",       // TikTok
            "com.ss.android.ugc.trill",       // TikTok alternate
            "com.twitter.android",
            "com.snapchat.android"
        )

        private val PRODUCTIVITY_PACKAGES = setOf(
            "com.google.android.gm",          // Gmail
            "com.google.android.apps.docs",   // Google Docs
            "com.google.android.apps.docs.editors.docs",
            "com.google.android.apps.docs.editors.sheets",
            "com.google.android.apps.docs.editors.slides",
            "com.slack"
        )

        private val ENTERTAINMENT_PACKAGES = setOf(
            "com.google.android.youtube",
            "com.netflix.mediaclient",
            "com.spotify.music"
        )

        private val SOCIAL_KEYWORDS = listOf("instagram", "facebook", "tiktok", "twitter", "snapchat", "whatsapp", "telegram")
        private val PRODUCTIVITY_KEYWORDS = listOf("gmail", "docs", "slack", "notion", "trello", "drive", "office", "outlook")
        private val ENTERTAINMENT_KEYWORDS = listOf("youtube", "netflix", "spotify", "hulu", "disney", "twitch", "game")
    }

    fun hasUsageStatsPermission(): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getUsageStatsSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    fun getDailyUsage(dayStartMillis: Long, dayEndMillis: Long): ScreenTimeSummary {
        if (!hasUsageStatsPermission()) {
            return ScreenTimeSummary(
                totalScreenTimeMinutes = 0,
                pickupCount = 0,
                appUsageList = emptyList()
            )
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val usageMap = queryUsageTimes(usageStatsManager, dayStartMillis, dayEndMillis)
        val pickupCount = queryPickupCount(usageStatsManager, dayStartMillis, dayEndMillis)

        val appUsageList = usageMap
            .filter { it.value > 60_000L }
            .map { (packageName, timeMs) ->
                val appName = getAppName(packageName)
                val category = categorizeApp(packageName)
                val usageMinutes = (timeMs / 60_000L).toInt()
                AppUsageInfo(
                    packageName = packageName,
                    appName = appName,
                    category = category,
                    usageMinutes = usageMinutes
                )
            }
            .sortedByDescending { it.usageMinutes }

        val totalScreenTimeMinutes = appUsageList.sumOf { it.usageMinutes }

        return ScreenTimeSummary(
            totalScreenTimeMinutes = totalScreenTimeMinutes,
            pickupCount = pickupCount,
            appUsageList = appUsageList
        )
    }

    private fun queryUsageTimes(
        usageStatsManager: UsageStatsManager,
        startMillis: Long,
        endMillis: Long
    ): Map<String, Long> {
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startMillis,
            endMillis
        )

        val usageMap = mutableMapOf<String, Long>()
        if (stats != null) {
            for (stat in stats) {
                val totalTime = stat.totalTimeInForeground
                if (totalTime > 0) {
                    usageMap[stat.packageName] = (usageMap[stat.packageName] ?: 0L) + totalTime
                }
            }
        }
        return usageMap
    }

    private fun queryPickupCount(
        usageStatsManager: UsageStatsManager,
        startMillis: Long,
        endMillis: Long
    ): Int {
        var count = 0
        val events = usageStatsManager.queryEvents(startMillis, endMillis)
        val event = UsageEvents.Event()

        var screenOff = true
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.SCREEN_INTERACTIVE -> {
                    if (screenOff) {
                        count++
                        screenOff = false
                    }
                }
                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    screenOff = true
                }
            }
        }
        return count
    }

    private fun categorizeApp(packageName: String): AppCategory {
        if (packageName in SOCIAL_PACKAGES) return AppCategory.SOCIAL
        if (packageName in PRODUCTIVITY_PACKAGES) return AppCategory.PRODUCTIVITY
        if (packageName in ENTERTAINMENT_PACKAGES) return AppCategory.ENTERTAINMENT

        val lowerPackage = packageName.lowercase()
        if (SOCIAL_KEYWORDS.any { lowerPackage.contains(it) }) return AppCategory.SOCIAL
        if (PRODUCTIVITY_KEYWORDS.any { lowerPackage.contains(it) }) return AppCategory.PRODUCTIVITY
        if (ENTERTAINMENT_KEYWORDS.any { lowerPackage.contains(it) }) return AppCategory.ENTERTAINMENT

        return AppCategory.OTHER
    }

    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName.substringAfterLast('.')
                .replaceFirstChar { it.uppercase() }
        }
    }
}
