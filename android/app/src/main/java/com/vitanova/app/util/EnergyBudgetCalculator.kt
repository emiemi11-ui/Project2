package com.vitanova.app.util

import kotlin.math.roundToInt

/**
 * Represents the current state of a user's daily energy budget.
 */
data class EnergyBudgetResult(
    val totalBudget: Int,
    val deductions: Int,
    val additions: Int,
    val remainingEnergy: Int,
    val level: EnergyLevel,
    val breakdown: EnergyBreakdown,
    val recommendation: String
)

enum class EnergyLevel {
    SURPLUS, HIGH, MODERATE, LOW, DEPLETED
}

/**
 * Itemized breakdown of energy deductions and additions.
 */
data class EnergyBreakdown(
    val screenTimeDeduction: Int,
    val bigTasksDeduction: Int,
    val meetingsDeduction: Int,
    val exerciseAddition: Int,
    val sleepBonusAddition: Int,
    val hrvBonusAddition: Int,
    val meditationAddition: Int
)

/**
 * Input parameters for energy budget calculation.
 */
data class EnergyBudgetInput(
    val screenTimeHours: Float = 0f,
    val bigTasksCompleted: Int = 0,
    val meetingsAttended: Int = 0,
    val moderateExerciseMinutes: Int = 0,
    val sleepScore: Int = 0,
    val hrvRmssd: Double = 0.0,
    val meditationMinutes: Int = 0
)

/**
 * Calculates a daily energy budget starting at 100 points.
 *
 * Energy is a finite resource that is depleted by cognitively or physically demanding
 * activities and replenished by recovery activities. The budget is clamped to 0-120,
 * allowing recovery activities to push energy slightly above the daily baseline.
 */
object EnergyBudgetCalculator {

    private const val BASE_BUDGET = 100
    private const val MAX_BUDGET = 120
    private const val MIN_BUDGET = 0

    // Deduction rates
    private const val SCREEN_TIME_COST_PER_HOUR = 8
    private const val BIG_TASK_COST = 15
    private const val MEETING_COST = 10

    // Addition values
    private const val EXERCISE_BONUS = 12
    private const val SLEEP_BONUS = 10
    private const val HRV_BONUS = 8
    private const val MEDITATION_BONUS = 5

    // Thresholds for bonuses
    private const val EXERCISE_THRESHOLD_MINUTES = 20
    private const val SLEEP_SCORE_THRESHOLD = 80
    private const val HRV_RMSSD_THRESHOLD = 50.0

    /**
     * Computes the energy budget from the given inputs.
     *
     * Deductions:
     *   - Screen time:  screenTimeHours x 8 points
     *   - Big tasks:    bigTasksCompleted x 15 points
     *   - Meetings:     meetingsAttended x 10 points
     *
     * Additions:
     *   - Moderate exercise (>= 20 min): +12 points
     *   - Sleep score >= 80: +10 points
     *   - HRV RMSSD > 50ms: +8 points
     *   - Any meditation performed: +5 points
     *
     * Result is clamped to [0, 120].
     *
     * @param input [EnergyBudgetInput] with all relevant daily metrics.
     * @return [EnergyBudgetResult] with computed budget and breakdown.
     */
    fun calculate(input: EnergyBudgetInput): EnergyBudgetResult {
        // Calculate deductions
        val screenDeduction = (input.screenTimeHours.coerceAtLeast(0f) * SCREEN_TIME_COST_PER_HOUR)
            .roundToInt()
        val taskDeduction = input.bigTasksCompleted.coerceAtLeast(0) * BIG_TASK_COST
        val meetingDeduction = input.meetingsAttended.coerceAtLeast(0) * MEETING_COST
        val totalDeductions = screenDeduction + taskDeduction + meetingDeduction

        // Calculate additions
        val exerciseAdd = if (input.moderateExerciseMinutes >= EXERCISE_THRESHOLD_MINUTES) {
            EXERCISE_BONUS
        } else {
            // Partial credit for some exercise
            (input.moderateExerciseMinutes.toFloat() / EXERCISE_THRESHOLD_MINUTES * EXERCISE_BONUS)
                .roundToInt()
                .coerceAtMost(EXERCISE_BONUS)
        }

        val sleepAdd = if (input.sleepScore >= SLEEP_SCORE_THRESHOLD) SLEEP_BONUS else {
            // Partial credit for decent sleep (60-80)
            if (input.sleepScore >= 60) (SLEEP_BONUS * 0.5).roundToInt() else 0
        }

        val hrvAdd = if (input.hrvRmssd > HRV_RMSSD_THRESHOLD) HRV_BONUS else {
            // Partial credit for moderate HRV (30-50)
            if (input.hrvRmssd > 30.0) (HRV_BONUS * 0.5).roundToInt() else 0
        }

        val meditationAdd = if (input.meditationMinutes > 0) MEDITATION_BONUS else 0

        val totalAdditions = exerciseAdd + sleepAdd + hrvAdd + meditationAdd

        // Compute final budget
        val remaining = (BASE_BUDGET - totalDeductions + totalAdditions)
            .coerceIn(MIN_BUDGET, MAX_BUDGET)

        val level = when {
            remaining > 100 -> EnergyLevel.SURPLUS
            remaining >= 70 -> EnergyLevel.HIGH
            remaining >= 40 -> EnergyLevel.MODERATE
            remaining >= 15 -> EnergyLevel.LOW
            else -> EnergyLevel.DEPLETED
        }

        val recommendation = when (level) {
            EnergyLevel.SURPLUS ->
                "You have surplus energy. Channel it into creative or challenging work."
            EnergyLevel.HIGH ->
                "Energy is high. Good time for focused work and productive tasks."
            EnergyLevel.MODERATE ->
                "Moderate energy remaining. Prioritize your most important tasks."
            EnergyLevel.LOW ->
                "Energy is running low. Consider lighter tasks or a restorative break."
            EnergyLevel.DEPLETED ->
                "Energy depleted. Rest, meditate, or do gentle movement to recharge."
        }

        val breakdown = EnergyBreakdown(
            screenTimeDeduction = screenDeduction,
            bigTasksDeduction = taskDeduction,
            meetingsDeduction = meetingDeduction,
            exerciseAddition = exerciseAdd,
            sleepBonusAddition = sleepAdd,
            hrvBonusAddition = hrvAdd,
            meditationAddition = meditationAdd
        )

        return EnergyBudgetResult(
            totalBudget = BASE_BUDGET,
            deductions = totalDeductions,
            additions = totalAdditions,
            remainingEnergy = remaining,
            level = level,
            breakdown = breakdown,
            recommendation = recommendation
        )
    }
}
