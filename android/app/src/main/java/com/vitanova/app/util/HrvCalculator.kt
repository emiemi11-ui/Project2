package com.vitanova.app.util

import kotlin.math.pow
import kotlin.math.sqrt

data class HrvResult(
    val rmssd: Double,
    val sdnn: Double,
    val pnn50: Double,
    val bpm: Double,
    val stressScore: Int,
    val energyScore: Int
)

object HrvCalculator {

    /**
     * Calculates HRV metrics from a list of R-R intervals in milliseconds.
     *
     * @param rrIntervals List of R-R intervals in milliseconds. Must contain at least 2 values.
     * @param sleepQualityFactor Optional sleep quality factor (0.0-1.0) that modulates the energy score.
     *                          Defaults to 0.7 (neutral). Higher values boost energy; lower values reduce it.
     * @return [HrvResult] containing all computed metrics, or null if input is insufficient.
     */
    fun calculate(rrIntervals: List<Double>, sleepQualityFactor: Double = 0.7): HrvResult? {
        if (rrIntervals.size < 2) return null

        val filtered = filterArtifacts(rrIntervals)
        if (filtered.size < 2) return null

        val successiveDifferences = filtered.zipWithNext { a, b -> b - a }
        val rmssd = calculateRmssd(successiveDifferences)
        val sdnn = calculateSdnn(filtered)
        val pnn50 = calculatePnn50(successiveDifferences)
        val bpm = calculateBpm(filtered)
        val stressScore = calculateStressScore(rmssd)
        val energyScore = calculateEnergyScore(stressScore, sleepQualityFactor)

        return HrvResult(
            rmssd = roundTo(rmssd, 2),
            sdnn = roundTo(sdnn, 2),
            pnn50 = roundTo(pnn50, 2),
            bpm = roundTo(bpm, 1),
            stressScore = stressScore,
            energyScore = energyScore
        )
    }

    /**
     * Filters out physiologically implausible R-R intervals and artifacts.
     * Valid range: 300ms (200 bpm) to 2000ms (30 bpm).
     * Also removes intervals that deviate more than 20% from the local median.
     */
    private fun filterArtifacts(intervals: List<Double>): List<Double> {
        val physiological = intervals.filter { it in 300.0..2000.0 }
        if (physiological.size < 5) return physiological

        val windowSize = 5
        return physiological.filterIndexed { index, value ->
            val start = maxOf(0, index - windowSize / 2)
            val end = minOf(physiological.size, index + windowSize / 2 + 1)
            val window = physiological.subList(start, end).sorted()
            val localMedian = window[window.size / 2]
            val deviation = kotlin.math.abs(value - localMedian) / localMedian
            deviation < 0.20
        }
    }

    /**
     * Root Mean Square of Successive Differences.
     * Primary parasympathetic (vagal) HRV metric.
     */
    private fun calculateRmssd(successiveDiffs: List<Double>): Double {
        if (successiveDiffs.isEmpty()) return 0.0
        val sumSquared = successiveDiffs.sumOf { it.pow(2) }
        return sqrt(sumSquared / successiveDiffs.size)
    }

    /**
     * Standard Deviation of NN intervals.
     * Reflects overall HRV including both sympathetic and parasympathetic contributions.
     */
    private fun calculateSdnn(intervals: List<Double>): Double {
        if (intervals.isEmpty()) return 0.0
        val mean = intervals.average()
        val variance = intervals.sumOf { (it - mean).pow(2) } / intervals.size
        return sqrt(variance)
    }

    /**
     * Percentage of successive differences greater than 50ms.
     * Another parasympathetic HRV metric, correlates with RMSSD.
     */
    private fun calculatePnn50(successiveDiffs: List<Double>): Double {
        if (successiveDiffs.isEmpty()) return 0.0
        val nn50Count = successiveDiffs.count { kotlin.math.abs(it) > 50.0 }
        return (nn50Count.toDouble() / successiveDiffs.size) * 100.0
    }

    /**
     * Average heart rate in beats per minute derived from R-R intervals.
     */
    private fun calculateBpm(intervals: List<Double>): Double {
        if (intervals.isEmpty()) return 0.0
        val avgRR = intervals.average()
        return 60_000.0 / avgRR
    }

    /**
     * Stress score (0-100) based on RMSSD thresholds.
     * Higher RMSSD indicates stronger parasympathetic activity and lower stress.
     *
     * Mapping:
     *   RMSSD > 60ms  -> stress 20 (low stress)
     *   RMSSD 40-60ms -> stress 40 (moderate)
     *   RMSSD 20-40ms -> stress 70 (elevated)
     *   RMSSD < 20ms  -> stress 90 (high stress)
     *
     * Values are linearly interpolated within each band for smoother transitions.
     */
    private fun calculateStressScore(rmssd: Double): Int {
        val raw = when {
            rmssd >= 60.0 -> {
                // 60-100+ maps to 10-20
                val t = ((rmssd - 60.0) / 40.0).coerceIn(0.0, 1.0)
                20.0 - t * 10.0
            }
            rmssd >= 40.0 -> {
                // 40-60 maps to 40-20
                val t = (rmssd - 40.0) / 20.0
                40.0 - t * 20.0
            }
            rmssd >= 20.0 -> {
                // 20-40 maps to 70-40
                val t = (rmssd - 20.0) / 20.0
                70.0 - t * 30.0
            }
            else -> {
                // 0-20 maps to 90-70
                val t = rmssd / 20.0
                90.0 - t * 20.0
            }
        }
        return raw.toInt().coerceIn(0, 100)
    }

    /**
     * Energy score (0-100) inversely mirrors stress, modulated by sleep quality.
     *
     * @param stressScore The computed stress score (0-100).
     * @param sleepFactor Sleep quality factor (0.0-1.0). 1.0 = excellent sleep, 0.0 = very poor.
     */
    private fun calculateEnergyScore(stressScore: Int, sleepFactor: Double): Int {
        val baseEnergy = 100 - stressScore
        val sleepMultiplier = 0.6 + (sleepFactor.coerceIn(0.0, 1.0) * 0.6) // range: 0.6 to 1.2
        val adjusted = (baseEnergy * sleepMultiplier).toInt()
        return adjusted.coerceIn(0, 100)
    }

    private fun roundTo(value: Double, decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return kotlin.math.round(value * factor) / factor
    }
}
