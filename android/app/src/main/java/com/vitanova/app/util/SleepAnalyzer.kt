package com.vitanova.app.util

import kotlin.math.abs
import kotlin.math.roundToInt

enum class SleepPhase {
    DEEP, LIGHT, REM, AWAKE
}

data class SleepWindow(
    val startTimestamp: Long,
    val endTimestamp: Long,
    val phase: SleepPhase,
    val averageMovement: Float
)

data class SleepAnalysis(
    val totalDurationMinutes: Int,
    val efficiencyPercent: Float,
    val deepMinutes: Int,
    val lightMinutes: Int,
    val remMinutes: Int,
    val awakeMinutes: Int,
    val cyclesCount: Int,
    val sleepScore: Int,
    val windows: List<SleepWindow>,
    val sleepOnsetLatencyMinutes: Int,
    val wakeAfterSleepOnsetMinutes: Int
)

/**
 * Input sample for sleep analysis.
 * [timestamp] is epoch millis, [movementIntensity] is an accelerometer-derived value >= 0.
 */
data class SleepSampleInput(
    val timestamp: Long,
    val movementIntensity: Float
)

object SleepAnalyzer {

    private const val WINDOW_DURATION_MS = 5L * 60 * 1000 // 5 minutes
    private const val WINDOW_DURATION_MIN = 5

    /**
     * Analyzes a list of sleep samples and returns a complete [SleepAnalysis].
     *
     * @param samples Raw movement samples collected during the sleep session, sorted by timestamp.
     * @param consistencyScore A pre-computed consistency score (0.0-1.0) representing how regular
     *                         the user's sleep schedule has been. Defaults to 0.7.
     * @return [SleepAnalysis] with all metrics, or null if insufficient data.
     */
    fun analyze(
        samples: List<SleepSampleInput>,
        consistencyScore: Float = 0.7f
    ): SleepAnalysis? {
        if (samples.size < 2) return null

        val sorted = samples.sortedBy { it.timestamp }
        val windows = classifyWindows(sorted)
        if (windows.isEmpty()) return null

        val deepMinutes = countMinutes(windows, SleepPhase.DEEP)
        val lightMinutes = countMinutes(windows, SleepPhase.LIGHT)
        val remMinutes = countMinutes(windows, SleepPhase.REM)
        val awakeMinutes = countMinutes(windows, SleepPhase.AWAKE)
        val totalDurationMinutes = deepMinutes + lightMinutes + remMinutes + awakeMinutes
        val sleepMinutes = deepMinutes + lightMinutes + remMinutes

        val efficiencyPercent = if (totalDurationMinutes > 0) {
            (sleepMinutes.toFloat() / totalDurationMinutes) * 100f
        } else 0f

        val cyclesCount = countCycles(windows)

        val sleepOnsetLatency = calculateSleepOnsetLatency(windows)
        val waso = calculateWaso(windows)

        val sleepScore = calculateSleepScore(
            totalDurationMinutes = totalDurationMinutes,
            efficiencyPercent = efficiencyPercent,
            deepMinutes = deepMinutes,
            sleepMinutes = sleepMinutes,
            cyclesCount = cyclesCount,
            consistencyScore = consistencyScore
        )

        return SleepAnalysis(
            totalDurationMinutes = totalDurationMinutes,
            efficiencyPercent = roundToOneDecimal(efficiencyPercent),
            deepMinutes = deepMinutes,
            lightMinutes = lightMinutes,
            remMinutes = remMinutes,
            awakeMinutes = awakeMinutes,
            cyclesCount = cyclesCount,
            sleepScore = sleepScore,
            windows = windows,
            sleepOnsetLatencyMinutes = sleepOnsetLatency,
            wakeAfterSleepOnsetMinutes = waso
        )
    }

    /**
     * Groups samples into 5-minute windows and classifies each by movement variance.
     *
     * Variance thresholds:
     *   < 0.01  -> DEEP
     *   0.01-0.05 -> LIGHT
     *   0.05-0.15 -> REM
     *   > 0.15  -> AWAKE
     */
    private fun classifyWindows(sorted: List<SleepSampleInput>): List<SleepWindow> {
        if (sorted.isEmpty()) return emptyList()

        val startTime = sorted.first().timestamp
        val endTime = sorted.last().timestamp
        val windows = mutableListOf<SleepWindow>()

        var windowStart = startTime
        while (windowStart < endTime) {
            val windowEnd = windowStart + WINDOW_DURATION_MS
            val windowSamples = sorted.filter { it.timestamp in windowStart until windowEnd }

            if (windowSamples.isNotEmpty()) {
                val movements = windowSamples.map { it.movementIntensity.toDouble() }
                val variance = calculateVariance(movements)
                val avgMovement = movements.average().toFloat()

                val phase = when {
                    variance < 0.01 -> SleepPhase.DEEP
                    variance < 0.05 -> SleepPhase.LIGHT
                    variance < 0.15 -> SleepPhase.REM
                    else -> SleepPhase.AWAKE
                }

                windows.add(
                    SleepWindow(
                        startTimestamp = windowStart,
                        endTimestamp = windowEnd,
                        phase = phase,
                        averageMovement = avgMovement
                    )
                )
            }

            windowStart = windowEnd
        }

        return windows
    }

    private fun calculateVariance(values: List<Double>): Double {
        if (values.size < 2) return 0.0
        val mean = values.average()
        return values.sumOf { (it - mean) * (it - mean) } / values.size
    }

    private fun countMinutes(windows: List<SleepWindow>, phase: SleepPhase): Int {
        return windows.count { it.phase == phase } * WINDOW_DURATION_MIN
    }

    /**
     * Counts complete sleep cycles. A complete cycle follows the pattern:
     * LIGHT -> DEEP -> REM (in sequence, possibly with intervening windows of the same phase).
     */
    private fun countCycles(windows: List<SleepWindow>): Int {
        if (windows.isEmpty()) return 0

        // Collapse consecutive same-phase windows into a phase sequence
        val phaseSequence = mutableListOf(windows.first().phase)
        for (w in windows.drop(1)) {
            if (w.phase != phaseSequence.last()) {
                phaseSequence.add(w.phase)
            }
        }

        var cycles = 0
        var state = 0 // 0 = looking for LIGHT, 1 = looking for DEEP, 2 = looking for REM

        for (phase in phaseSequence) {
            when (state) {
                0 -> if (phase == SleepPhase.LIGHT) state = 1
                1 -> when (phase) {
                    SleepPhase.DEEP -> state = 2
                    SleepPhase.LIGHT -> state = 1
                    else -> state = 0
                }
                2 -> when (phase) {
                    SleepPhase.REM -> {
                        cycles++
                        state = 0
                    }
                    SleepPhase.DEEP -> state = 2
                    else -> state = 0
                }
            }
        }

        return cycles
    }

    /**
     * Sleep onset latency: minutes of AWAKE at the beginning of the session.
     */
    private fun calculateSleepOnsetLatency(windows: List<SleepWindow>): Int {
        var count = 0
        for (w in windows) {
            if (w.phase == SleepPhase.AWAKE) count++ else break
        }
        return count * WINDOW_DURATION_MIN
    }

    /**
     * Wake After Sleep Onset: total AWAKE minutes after the first non-AWAKE window,
     * excluding trailing awake windows.
     */
    private fun calculateWaso(windows: List<SleepWindow>): Int {
        val firstSleepIndex = windows.indexOfFirst { it.phase != SleepPhase.AWAKE }
        if (firstSleepIndex < 0) return 0
        val lastSleepIndex = windows.indexOfLast { it.phase != SleepPhase.AWAKE }
        if (lastSleepIndex <= firstSleepIndex) return 0

        val middleWindows = windows.subList(firstSleepIndex, lastSleepIndex + 1)
        return middleWindows.count { it.phase == SleepPhase.AWAKE } * WINDOW_DURATION_MIN
    }

    /**
     * Sleep Score (0-100) formula:
     *
     * Duration component  (30%): 7-9h = 100, <5h = 20, linearly interpolated
     * Efficiency component (25%): direct percentage
     * Deep% component     (20%): deep% >= 20 = 100, scales linearly below
     * Cycles component    (15%): 4-5 cycles = 100, scales proportionally
     * Consistency component(10%): direct from input
     */
    private fun calculateSleepScore(
        totalDurationMinutes: Int,
        efficiencyPercent: Float,
        deepMinutes: Int,
        sleepMinutes: Int,
        cyclesCount: Int,
        consistencyScore: Float
    ): Int {
        val durationScore = calculateDurationScore(totalDurationMinutes)
        val efficiencyScore = efficiencyPercent.coerceIn(0f, 100f)
        val deepPercent = if (sleepMinutes > 0) (deepMinutes.toFloat() / sleepMinutes) * 100f else 0f
        val deepScore = calculateDeepScore(deepPercent)
        val cycleScore = calculateCycleScore(cyclesCount)
        val consistencyValue = (consistencyScore.coerceIn(0f, 1f) * 100f)

        val weighted = durationScore * 0.30f +
                efficiencyScore * 0.25f +
                deepScore * 0.20f +
                cycleScore * 0.15f +
                consistencyValue * 0.10f

        return weighted.roundToInt().coerceIn(0, 100)
    }

    /**
     * Duration scoring:
     *   >= 540 min (9h): 100
     *   420-540 min (7-9h): 100
     *   300-420 min (5-7h): linear 20-100
     *   < 300 min (< 5h): 20
     */
    private fun calculateDurationScore(minutes: Int): Float {
        return when {
            minutes >= 420 && minutes <= 540 -> 100f
            minutes > 540 -> {
                // Oversleep penalty: gentle decrease beyond 9h
                val excess = (minutes - 540).toFloat()
                (100f - excess / 6f).coerceAtLeast(60f)
            }
            minutes >= 300 -> {
                // Linear interpolation from 20 at 300min to 100 at 420min
                20f + (minutes - 300).toFloat() / 120f * 80f
            }
            else -> 20f
        }
    }

    /**
     * Deep sleep percentage scoring:
     *   >= 20% -> 100
     *   0-20% -> linear 0-100
     */
    private fun calculateDeepScore(deepPercent: Float): Float {
        return if (deepPercent >= 20f) 100f
        else (deepPercent / 20f * 100f).coerceIn(0f, 100f)
    }

    /**
     * Cycle scoring:
     *   4-5 cycles -> 100
     *   < 4 -> proportional (cycles / 4 * 100)
     *   > 5 -> slight decrease
     */
    private fun calculateCycleScore(cycles: Int): Float {
        return when {
            cycles in 4..5 -> 100f
            cycles < 4 -> (cycles.toFloat() / 4f * 100f)
            else -> (100f - (cycles - 5) * 10f).coerceAtLeast(60f)
        }
    }

    private fun roundToOneDecimal(value: Float): Float {
        return (value * 10f).roundToInt() / 10f
    }
}
