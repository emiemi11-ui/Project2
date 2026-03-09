package com.vitanova.app.util

import kotlin.math.roundToInt

/**
 * Result of a cognitive load assessment based on behavioral and physiological inputs.
 */
data class CognitiveLoadResult(
    val cognitiveLoadScore: Int,
    val level: CognitiveLoadLevel,
    val recommendation: String
)

enum class CognitiveLoadLevel {
    LOW, MODERATE, HIGH, OVERLOADED
}

/**
 * Result of individual cognitive test scoring.
 */
data class CognitiveTestScores(
    val reactionTimeScore: Int,
    val nBackScore: Int,
    val stroopScore: Int,
    val patternScore: Int,
    val globalScore: Int,
    val percentile: Int
)

/**
 * Calculator for cognitive load and cognitive performance metrics.
 *
 * Cognitive Load Score quantifies mental fatigue based on device usage patterns
 * and physiological indicators. Individual test scores measure raw cognitive
 * performance across multiple domains.
 */
object CognitiveScoreCalculator {

    // ---------- Cognitive Load Score ----------

    /**
     * Computes a Cognitive Load Score (0-100) representing current mental fatigue.
     * A higher score means LESS cognitive load (more capacity remaining).
     *
     * @param screenTimeHours Total screen time in hours for the assessment period.
     * @param appSwitchesPerHour Average number of app context-switches per hour.
     * @param notificationsDismissed Number of notifications dismissed without action.
     * @param sleepQualityScore Sleep quality score (0-100). Higher is better.
     * @param hrvScore HRV-derived score (0-100). Higher is better.
     * @return [CognitiveLoadResult] with the computed score, level, and recommendation.
     */
    fun calculateCognitiveLoad(
        screenTimeHours: Float,
        appSwitchesPerHour: Float,
        notificationsDismissed: Int,
        sleepQualityScore: Int,
        hrvScore: Int
    ): CognitiveLoadResult {
        val inverseSleepQuality = (100 - sleepQualityScore.coerceIn(0, 100)) / 100f
        val inverseHrvScore = (100 - hrvScore.coerceIn(0, 100)) / 100f

        val deductions = screenTimeHours.coerceAtLeast(0f) * 8f +
                appSwitchesPerHour.coerceAtLeast(0f) * 3f +
                notificationsDismissed.coerceAtLeast(0) * 2f +
                inverseSleepQuality * 15f +
                inverseHrvScore * 12f

        val score = (100f - deductions).roundToInt().coerceIn(0, 100)

        val level = when {
            score >= 75 -> CognitiveLoadLevel.LOW
            score >= 50 -> CognitiveLoadLevel.MODERATE
            score >= 25 -> CognitiveLoadLevel.HIGH
            else -> CognitiveLoadLevel.OVERLOADED
        }

        val recommendation = when (level) {
            CognitiveLoadLevel.LOW ->
                "Your cognitive capacity is high. Great time for demanding mental tasks."
            CognitiveLoadLevel.MODERATE ->
                "Moderate cognitive load detected. Take short breaks between focused work sessions."
            CognitiveLoadLevel.HIGH ->
                "High cognitive load. Consider reducing screen time and taking a mindful break."
            CognitiveLoadLevel.OVERLOADED ->
                "Cognitive overload detected. Step away from screens, rest, or do light physical activity."
        }

        return CognitiveLoadResult(
            cognitiveLoadScore = score,
            level = level,
            recommendation = recommendation
        )
    }

    // ---------- Individual Cognitive Test Scores ----------

    /**
     * Scores a reaction time test result.
     * Faster reaction times yield higher scores.
     *
     * Mapping (milliseconds -> score):
     *   <= 200ms -> 100  (exceptional)
     *   200-300ms -> 80-100 (excellent)
     *   300-400ms -> 60-80  (good)
     *   400-600ms -> 30-60  (average)
     *   > 600ms   -> 0-30   (slow)
     *
     * @param reactionTimeMs Average reaction time in milliseconds.
     * @return Score 0-100.
     */
    fun scoreReactionTime(reactionTimeMs: Int): Int {
        val ms = reactionTimeMs.coerceAtLeast(100)
        val score = when {
            ms <= 200 -> 100.0
            ms <= 300 -> 80.0 + (300 - ms).toDouble() / 100.0 * 20.0
            ms <= 400 -> 60.0 + (400 - ms).toDouble() / 100.0 * 20.0
            ms <= 600 -> 30.0 + (600 - ms).toDouble() / 200.0 * 30.0
            else -> (30.0 * (1000 - ms).coerceAtLeast(0).toDouble() / 400.0).coerceAtLeast(0.0)
        }
        return score.roundToInt().coerceIn(0, 100)
    }

    /**
     * Scores an N-back test result based on accuracy percentage.
     *
     * @param accuracyPercent Accuracy as a percentage (0-100).
     * @param nLevel The N-back level (1, 2, 3, etc.). Higher levels add a bonus.
     * @return Score 0-100.
     */
    fun scoreNBack(accuracyPercent: Float, nLevel: Int = 2): Int {
        val baseScore = accuracyPercent.coerceIn(0f, 100f)
        val levelBonus = when {
            nLevel >= 3 && baseScore >= 70f -> 10f
            nLevel >= 4 && baseScore >= 60f -> 15f
            else -> 0f
        }
        return (baseScore + levelBonus).roundToInt().coerceIn(0, 100)
    }

    /**
     * Scores a Stroop test result based on accuracy and completion time.
     *
     * @param accuracyPercent Accuracy as a percentage (0-100).
     * @param averageTimeMs Average response time per trial in milliseconds.
     * @return Score 0-100.
     */
    fun scoreStroop(accuracyPercent: Float, averageTimeMs: Int): Int {
        val accuracyScore = accuracyPercent.coerceIn(0f, 100f)

        // Speed component: faster is better
        val speedScore = when {
            averageTimeMs <= 500 -> 100f
            averageTimeMs <= 800 -> 70f + (800 - averageTimeMs).toFloat() / 300f * 30f
            averageTimeMs <= 1200 -> 40f + (1200 - averageTimeMs).toFloat() / 400f * 30f
            averageTimeMs <= 2000 -> 10f + (2000 - averageTimeMs).toFloat() / 800f * 30f
            else -> 10f
        }

        // Composite: accuracy weighted more heavily than speed
        val composite = accuracyScore * 0.65f + speedScore * 0.35f
        return composite.roundToInt().coerceIn(0, 100)
    }

    /**
     * Scores a pattern recognition test based on number correct out of total.
     *
     * @param correctCount Number of patterns correctly identified.
     * @param totalCount Total number of pattern challenges presented.
     * @return Score 0-100.
     */
    fun scorePattern(correctCount: Int, totalCount: Int): Int {
        if (totalCount <= 0) return 0
        val accuracy = correctCount.coerceIn(0, totalCount).toFloat() / totalCount * 100f
        return accuracy.roundToInt().coerceIn(0, 100)
    }

    /**
     * Computes a global cognitive performance score from individual test sub-scores.
     *
     * Weights:
     *   Reaction Time:  25%
     *   N-Back:         30%  (working memory is heavily weighted)
     *   Stroop:         25%  (executive function)
     *   Pattern:        20%
     *
     * @return [CognitiveTestScores] with individual and global scores.
     */
    fun calculateGlobalScore(
        reactionTimeMs: Int? = null,
        nBackAccuracy: Float? = null,
        nBackLevel: Int = 2,
        stroopAccuracy: Float? = null,
        stroopTimeMs: Int? = null,
        patternCorrect: Int? = null,
        patternTotal: Int? = null
    ): CognitiveTestScores {
        val rtScore = reactionTimeMs?.let { scoreReactionTime(it) }
        val nbScore = nBackAccuracy?.let { scoreNBack(it, nBackLevel) }
        val stScore = if (stroopAccuracy != null && stroopTimeMs != null) {
            scoreStroop(stroopAccuracy, stroopTimeMs)
        } else null
        val ptScore = if (patternCorrect != null && patternTotal != null) {
            scorePattern(patternCorrect, patternTotal)
        } else null

        // Weighted average using only available scores
        data class WeightedEntry(val score: Int, val weight: Float)

        val entries = mutableListOf<WeightedEntry>()
        rtScore?.let { entries.add(WeightedEntry(it, 0.25f)) }
        nbScore?.let { entries.add(WeightedEntry(it, 0.30f)) }
        stScore?.let { entries.add(WeightedEntry(it, 0.25f)) }
        ptScore?.let { entries.add(WeightedEntry(it, 0.20f)) }

        val globalScore = if (entries.isNotEmpty()) {
            val totalWeight = entries.sumOf { it.weight.toDouble() }.toFloat()
            val weightedSum = entries.sumOf { (it.score * it.weight).toDouble() }.toFloat()
            (weightedSum / totalWeight).roundToInt().coerceIn(0, 100)
        } else 0

        val percentile = estimatePercentile(globalScore)

        return CognitiveTestScores(
            reactionTimeScore = rtScore ?: 0,
            nBackScore = nbScore ?: 0,
            stroopScore = stScore ?: 0,
            patternScore = ptScore ?: 0,
            globalScore = globalScore,
            percentile = percentile
        )
    }

    /**
     * Rough percentile estimate based on population norms.
     * Maps global scores to approximate population percentiles.
     */
    private fun estimatePercentile(globalScore: Int): Int {
        return when {
            globalScore >= 95 -> 99
            globalScore >= 90 -> 95
            globalScore >= 80 -> 85
            globalScore >= 70 -> 70
            globalScore >= 60 -> 55
            globalScore >= 50 -> 40
            globalScore >= 40 -> 25
            globalScore >= 30 -> 15
            else -> 5
        }
    }
}
