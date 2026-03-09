package com.vitanova.app.util

import kotlin.math.roundToInt

/**
 * Result of a readiness assessment.
 */
data class ReadinessResult(
    val score: Int,
    val level: ReadinessLevel,
    val recommendation: String,
    val componentScores: ReadinessComponents
)

enum class ReadinessLevel {
    OPTIMAL,
    GOOD,
    MODERATE,
    LOW
}

/**
 * Individual component scores that feed into the overall readiness score.
 */
data class ReadinessComponents(
    val sleepComponent: Float,
    val hrvComponent: Float,
    val stressComponent: Float,
    val fitnessComponent: Float,
    val habitMomentumComponent: Float
)

/**
 * Input data for readiness calculation.
 */
data class ReadinessInput(
    val sleepScore: Int,
    val hrvScore: Int,
    val stressScore: Int,
    val fitnessScore: Int,
    val habitMomentumScore: Int
)

/**
 * Calculates a daily Readiness Score (0-100) that summarizes how prepared
 * the user is for physical and cognitive demands.
 *
 * Readiness is a composite metric drawn from sleep quality, autonomic nervous system
 * state (HRV), psychological stress, physical fitness, and behavioral consistency.
 *
 * Formula:
 *   Readiness = Sleep × 0.30 + HRV × 0.25 + InverseStress × 0.20 + Fitness × 0.15 + HabitMomentum × 0.10
 *
 * The stress score is inverted because the input stressScore uses higher = more stress,
 * but readiness should benefit from LOW stress.
 */
object ReadinessCalculator {

    private const val SLEEP_WEIGHT = 0.30f
    private const val HRV_WEIGHT = 0.25f
    private const val STRESS_WEIGHT = 0.20f
    private const val FITNESS_WEIGHT = 0.15f
    private const val HABIT_WEIGHT = 0.10f

    /**
     * Computes the readiness score from provided input metrics.
     *
     * @param input [ReadinessInput] with all component scores (each 0-100).
     *              Note: stressScore uses higher = more stress.
     * @return [ReadinessResult] with the composite score, level, and recommendation.
     */
    fun calculate(input: ReadinessInput): ReadinessResult {
        val sleepNorm = input.sleepScore.coerceIn(0, 100).toFloat()
        val hrvNorm = input.hrvScore.coerceIn(0, 100).toFloat()
        val inverseStress = (100 - input.stressScore.coerceIn(0, 100)).toFloat()
        val fitnessNorm = input.fitnessScore.coerceIn(0, 100).toFloat()
        val habitNorm = input.habitMomentumScore.coerceIn(0, 100).toFloat()

        val weightedScore = sleepNorm * SLEEP_WEIGHT +
                hrvNorm * HRV_WEIGHT +
                inverseStress * STRESS_WEIGHT +
                fitnessNorm * FITNESS_WEIGHT +
                habitNorm * HABIT_WEIGHT

        val score = weightedScore.roundToInt().coerceIn(0, 100)

        val level = when {
            score >= 80 -> ReadinessLevel.OPTIMAL
            score >= 60 -> ReadinessLevel.GOOD
            score >= 40 -> ReadinessLevel.MODERATE
            else -> ReadinessLevel.LOW
        }

        val recommendation = buildRecommendation(level, input, score)

        val components = ReadinessComponents(
            sleepComponent = roundToOneDecimal(sleepNorm * SLEEP_WEIGHT),
            hrvComponent = roundToOneDecimal(hrvNorm * HRV_WEIGHT),
            stressComponent = roundToOneDecimal(inverseStress * STRESS_WEIGHT),
            fitnessComponent = roundToOneDecimal(fitnessNorm * FITNESS_WEIGHT),
            habitMomentumComponent = roundToOneDecimal(habitNorm * HABIT_WEIGHT)
        )

        return ReadinessResult(
            score = score,
            level = level,
            recommendation = recommendation,
            componentScores = components
        )
    }

    /**
     * Convenience overload that accepts individual scores directly.
     */
    fun calculate(
        sleepScore: Int,
        hrvScore: Int,
        stressScore: Int,
        fitnessScore: Int,
        habitMomentumScore: Int
    ): ReadinessResult {
        return calculate(
            ReadinessInput(
                sleepScore = sleepScore,
                hrvScore = hrvScore,
                stressScore = stressScore,
                fitnessScore = fitnessScore,
                habitMomentumScore = habitMomentumScore
            )
        )
    }

    /**
     * Builds a context-aware recommendation based on the readiness level
     * and identifies the weakest component for targeted advice.
     */
    private fun buildRecommendation(
        level: ReadinessLevel,
        input: ReadinessInput,
        score: Int
    ): String {
        val baseMessage = when (level) {
            ReadinessLevel.OPTIMAL ->
                "You're in peak readiness. Ideal for high-intensity training or demanding cognitive work."
            ReadinessLevel.GOOD ->
                "Readiness is good. You can handle moderate-to-high demands today."
            ReadinessLevel.MODERATE ->
                "Readiness is moderate. Consider lighter activities and focus on recovery."
            ReadinessLevel.LOW ->
                "Readiness is low. Prioritize rest, sleep, and stress management today."
        }

        // Identify the weakest component and add specific advice
        val weakest = findWeakestComponent(input)
        val specificAdvice = when (weakest) {
            "sleep" -> " Focus on improving sleep: aim for 7-9 hours in a dark, cool room."
            "hrv" -> " Your HRV is low, suggesting autonomic strain. Try breathing exercises or a rest day."
            "stress" -> " Stress levels are elevated. Consider meditation, a walk, or journaling."
            "fitness" -> " Fitness score is lagging. Incorporate regular moderate exercise."
            "habits" -> " Habit consistency could improve. Stick to your routines even when motivation dips."
            else -> ""
        }

        return baseMessage + specificAdvice
    }

    /**
     * Finds the weakest input component to provide targeted recommendations.
     * For stress, lower is better, so we compare (100 - stressScore) to other scores.
     */
    private fun findWeakestComponent(input: ReadinessInput): String {
        val components = mapOf(
            "sleep" to input.sleepScore,
            "hrv" to input.hrvScore,
            "stress" to (100 - input.stressScore), // invert so lower = worse
            "fitness" to input.fitnessScore,
            "habits" to input.habitMomentumScore
        )
        return components.minByOrNull { it.value }?.key ?: "sleep"
    }

    private fun roundToOneDecimal(value: Float): Float {
        return (value * 10f).roundToInt() / 10f
    }
}
