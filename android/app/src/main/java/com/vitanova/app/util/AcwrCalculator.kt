package com.vitanova.app.util

import kotlin.math.roundToInt

/**
 * Result of an Acute:Chronic Workload Ratio calculation.
 */
data class AcwrResult(
    val acuteLoad: Double,
    val chronicLoad: Double,
    val ratio: Double,
    val zone: AcwrZone,
    val recommendation: String,
    val weeklyLoads: List<Double>
)

/**
 * Training zones based on ACWR thresholds.
 */
enum class AcwrZone {
    UNDERTRAINING,
    SAFE,
    CAUTION,
    DANGER
}

/**
 * A single daily workload entry.
 *
 * @param date Identifier for the day (e.g., epoch millis or day index).
 * @param load Arbitrary-unit training load for the day. Can be session RPE x duration,
 *             TRIMP, or any consistent metric.
 */
data class DailyWorkload(
    val date: Long,
    val load: Double
)

/**
 * Calculates the Acute:Chronic Workload Ratio (ACWR) used to monitor training load
 * and injury risk in athletes and active individuals.
 *
 * The ACWR compares recent training load (acute, last 7 days) to longer-term
 * habitual load (chronic, last 28 days). Spikes in the ratio correlate with
 * increased injury risk.
 *
 * Zones:
 *   < 0.8  -> Undertraining (detraining risk)
 *   0.8-1.3 -> Safe zone (optimal adaptation)
 *   1.3-1.5 -> Caution zone (elevated risk)
 *   > 1.5  -> Danger zone (high injury risk)
 */
object AcwrCalculator {

    private const val ACUTE_WINDOW_DAYS = 7
    private const val CHRONIC_WINDOW_DAYS = 28

    /**
     * Calculates ACWR from a list of daily workloads.
     *
     * The list should contain at least 7 entries for meaningful results.
     * For best accuracy, provide 28+ days of data.
     * Entries are assumed to be sorted chronologically (oldest first).
     *
     * @param dailyLoads List of [DailyWorkload] sorted by date ascending.
     * @return [AcwrResult] or null if insufficient data.
     */
    fun calculate(dailyLoads: List<DailyWorkload>): AcwrResult? {
        if (dailyLoads.isEmpty()) return null

        val sortedLoads = dailyLoads.sortedBy { it.date }
        val loads = sortedLoads.map { it.load }

        // Extract acute window (last 7 days)
        val acuteLoads = loads.takeLast(ACUTE_WINDOW_DAYS.coerceAtMost(loads.size))
        val acuteAvg = acuteLoads.average()

        // Extract chronic window (last 28 days)
        val chronicLoads = loads.takeLast(CHRONIC_WINDOW_DAYS.coerceAtMost(loads.size))
        val chronicAvg = chronicLoads.average()

        // Compute ratio; guard against division by zero
        val ratio = if (chronicAvg > 0.001) {
            acuteAvg / chronicAvg
        } else {
            // If chronic load is essentially zero, any acute load is a spike
            if (acuteAvg > 0.001) 2.0 else 1.0
        }

        val roundedRatio = (ratio * 100).roundToInt() / 100.0

        val zone = classifyZone(roundedRatio)

        val recommendation = when (zone) {
            AcwrZone.UNDERTRAINING ->
                "Training load is below baseline. Consider gradually increasing volume to maintain fitness."
            AcwrZone.SAFE ->
                "Training load is in the safe zone. Continue current progression."
            AcwrZone.CAUTION ->
                "Training load is elevated. Monitor recovery closely and consider moderating intensity."
            AcwrZone.DANGER ->
                "Training load spike detected. High injury risk. Reduce volume and prioritize recovery."
        }

        // Compute weekly averages for trend visualization
        val weeklyLoads = computeWeeklyAverages(loads)

        return AcwrResult(
            acuteLoad = roundTo(acuteAvg, 1),
            chronicLoad = roundTo(chronicAvg, 1),
            ratio = roundedRatio,
            zone = zone,
            recommendation = recommendation,
            weeklyLoads = weeklyLoads
        )
    }

    /**
     * Calculates ACWR using the Exponentially Weighted Moving Average (EWMA) method.
     * This approach gives more weight to recent training loads and is considered
     * more sensitive to load changes than the rolling-average method.
     *
     * @param dailyLoads List of [DailyWorkload] sorted by date ascending.
     * @param acuteDecay Decay constant for acute EWMA. Default: 2 / (7 + 1) = 0.25
     * @param chronicDecay Decay constant for chronic EWMA. Default: 2 / (28 + 1) ≈ 0.069
     * @return [AcwrResult] or null if insufficient data.
     */
    fun calculateEwma(
        dailyLoads: List<DailyWorkload>,
        acuteDecay: Double = 2.0 / (ACUTE_WINDOW_DAYS + 1),
        chronicDecay: Double = 2.0 / (CHRONIC_WINDOW_DAYS + 1)
    ): AcwrResult? {
        if (dailyLoads.isEmpty()) return null

        val sortedLoads = dailyLoads.sortedBy { it.date }
        val loads = sortedLoads.map { it.load }

        var acuteEwma = loads.first()
        var chronicEwma = loads.first()

        for (i in 1 until loads.size) {
            acuteEwma = loads[i] * acuteDecay + acuteEwma * (1 - acuteDecay)
            chronicEwma = loads[i] * chronicDecay + chronicEwma * (1 - chronicDecay)
        }

        val ratio = if (chronicEwma > 0.001) {
            acuteEwma / chronicEwma
        } else {
            if (acuteEwma > 0.001) 2.0 else 1.0
        }

        val roundedRatio = (ratio * 100).roundToInt() / 100.0
        val zone = classifyZone(roundedRatio)

        val recommendation = when (zone) {
            AcwrZone.UNDERTRAINING ->
                "EWMA analysis: Load trending below baseline. Gradual increase recommended."
            AcwrZone.SAFE ->
                "EWMA analysis: Load management is optimal. Maintain current approach."
            AcwrZone.CAUTION ->
                "EWMA analysis: Recent load uptick detected. Watch for fatigue signs."
            AcwrZone.DANGER ->
                "EWMA analysis: Significant load spike. Prioritize recovery immediately."
        }

        val weeklyLoads = computeWeeklyAverages(loads)

        return AcwrResult(
            acuteLoad = roundTo(acuteEwma, 1),
            chronicLoad = roundTo(chronicEwma, 1),
            ratio = roundedRatio,
            zone = zone,
            recommendation = recommendation,
            weeklyLoads = weeklyLoads
        )
    }

    private fun classifyZone(ratio: Double): AcwrZone {
        return when {
            ratio < 0.8 -> AcwrZone.UNDERTRAINING
            ratio <= 1.3 -> AcwrZone.SAFE
            ratio <= 1.5 -> AcwrZone.CAUTION
            else -> AcwrZone.DANGER
        }
    }

    /**
     * Groups daily loads into 7-day windows and returns the average for each week.
     */
    private fun computeWeeklyAverages(dailyLoads: List<Double>): List<Double> {
        if (dailyLoads.isEmpty()) return emptyList()
        return dailyLoads.chunked(7).map { week ->
            roundTo(week.average(), 1)
        }
    }

    private fun roundTo(value: Double, decimals: Int): Double {
        val factor = Math.pow(10.0, decimals.toDouble())
        return Math.round(value * factor) / factor
    }
}
