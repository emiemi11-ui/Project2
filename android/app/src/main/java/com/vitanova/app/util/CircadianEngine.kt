package com.vitanova.app.util

import java.util.Calendar

/**
 * Circadian phase representing a period of the day.
 */
enum class CircadianPhase {
    MORNING,
    MIDDAY,
    AFTERNOON,
    EVENING,
    NIGHT
}

/**
 * Complete circadian state for a given time of day.
 */
data class CircadianState(
    val currentPhase: CircadianPhase,
    val greeting: String,
    val gradientColors: Pair<Long, Long>,
    val recommendedActions: List<String>,
    val energyMultiplier: Float,
    val hoursIntoPhase: Float,
    val phaseProgressPercent: Float
)

/**
 * Engine that maps the current time of day to circadian rhythm data.
 *
 * Provides phase classification, contextual greetings, UI gradient colors,
 * activity recommendations, and an energy multiplier based on typical
 * human circadian biology.
 *
 * Phase definitions:
 *   MORNING:   05:00 - 11:59  (cortisol peak, alertness rising)
 *   MIDDAY:    12:00 - 13:59  (post-lunch dip)
 *   AFTERNOON: 14:00 - 17:59  (second alertness peak)
 *   EVENING:   18:00 - 21:59  (melatonin onset, winding down)
 *   NIGHT:     22:00 - 04:59  (sleep / deep rest)
 */
object CircadianEngine {

    // Color constants as ARGB Long values matching the app's theme.
    // Morning: warm sunrise tones
    private const val MORNING_GRADIENT_START = 0xFFFFA502L
    private const val MORNING_GRADIENT_END = 0xFFFF7979L

    // Midday: bright daylight
    private const val MIDDAY_GRADIENT_START = 0xFF00D4FFL
    private const val MIDDAY_GRADIENT_END = 0xFF00E5A0L

    // Afternoon: energetic greens/cyans
    private const val AFTERNOON_GRADIENT_START = 0xFF00E5A0L
    private const val AFTERNOON_GRADIENT_END = 0xFF7BED9FL

    // Evening: warm dusk purples/pinks
    private const val EVENING_GRADIENT_START = 0xFFE056A0L
    private const val EVENING_GRADIENT_END = 0xFF6366F1L

    // Night: deep blues
    private const val NIGHT_GRADIENT_START = 0xFF1E3A5FL
    private const val NIGHT_GRADIENT_END = 0xFF03050AL

    /**
     * Returns the circadian state for the current system time.
     */
    fun getCurrentState(): CircadianState {
        val calendar = Calendar.getInstance()
        return getStateForTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }

    /**
     * Returns the circadian state for a specific hour and minute.
     *
     * @param hour Hour of the day (0-23).
     * @param minute Minute of the hour (0-59).
     * @return [CircadianState] for the given time.
     */
    fun getStateForTime(hour: Int, minute: Int = 0): CircadianState {
        val phase = classifyPhase(hour)
        val greeting = buildGreeting(phase, hour)
        val gradientColors = getGradientColors(phase)
        val recommendedActions = getRecommendedActions(phase, hour)
        val energyMultiplier = calculateEnergyMultiplier(hour, minute)
        val (hoursIntoPhase, phaseProgress) = calculatePhaseProgress(phase, hour, minute)

        return CircadianState(
            currentPhase = phase,
            greeting = greeting,
            gradientColors = gradientColors,
            recommendedActions = recommendedActions,
            energyMultiplier = energyMultiplier,
            hoursIntoPhase = hoursIntoPhase,
            phaseProgressPercent = phaseProgress
        )
    }

    /**
     * Classifies the hour into a circadian phase.
     */
    private fun classifyPhase(hour: Int): CircadianPhase {
        return when (hour) {
            in 5..11 -> CircadianPhase.MORNING
            in 12..13 -> CircadianPhase.MIDDAY
            in 14..17 -> CircadianPhase.AFTERNOON
            in 18..21 -> CircadianPhase.EVENING
            else -> CircadianPhase.NIGHT // 22-23, 0-4
        }
    }

    /**
     * Generates a contextual greeting based on phase and hour.
     */
    private fun buildGreeting(phase: CircadianPhase, hour: Int): String {
        return when (phase) {
            CircadianPhase.MORNING -> when {
                hour < 7 -> "Early riser! Your cortisol is waking up."
                hour < 9 -> "Good morning! Peak alertness window ahead."
                else -> "Good morning! Make the most of your focus peak."
            }
            CircadianPhase.MIDDAY ->
                "Good afternoon! A brief rest or walk can beat the post-lunch dip."
            CircadianPhase.AFTERNOON -> when {
                hour < 16 -> "Good afternoon! Your second wind is here."
                else -> "Late afternoon. Great time for creative work."
            }
            CircadianPhase.EVENING -> when {
                hour < 20 -> "Good evening! Time to start winding down."
                else -> "Getting late. Reduce blue light and prepare for sleep."
            }
            CircadianPhase.NIGHT -> when {
                hour >= 22 -> "It's nighttime. Rest is your best investment."
                else -> "Deep night. If you're awake, try to keep lights dim."
            }
        }
    }

    /**
     * Returns a pair of gradient colors (start, end) for the current phase.
     * Colors are provided as ARGB Long values suitable for Compose Color(long).
     */
    private fun getGradientColors(phase: CircadianPhase): Pair<Long, Long> {
        return when (phase) {
            CircadianPhase.MORNING -> Pair(MORNING_GRADIENT_START, MORNING_GRADIENT_END)
            CircadianPhase.MIDDAY -> Pair(MIDDAY_GRADIENT_START, MIDDAY_GRADIENT_END)
            CircadianPhase.AFTERNOON -> Pair(AFTERNOON_GRADIENT_START, AFTERNOON_GRADIENT_END)
            CircadianPhase.EVENING -> Pair(EVENING_GRADIENT_START, EVENING_GRADIENT_END)
            CircadianPhase.NIGHT -> Pair(NIGHT_GRADIENT_START, NIGHT_GRADIENT_END)
        }
    }

    /**
     * Returns a list of activity recommendations appropriate for the time of day.
     */
    private fun getRecommendedActions(phase: CircadianPhase, hour: Int): List<String> {
        return when (phase) {
            CircadianPhase.MORNING -> listOf(
                "Expose yourself to natural light",
                "Hydrate with water before coffee",
                "Do your most cognitively demanding work",
                "Light exercise or stretching",
                "Review daily goals and priorities"
            )
            CircadianPhase.MIDDAY -> listOf(
                "Take a 10-20 minute walk",
                "Eat a balanced lunch (protein + complex carbs)",
                "Avoid heavy caffeine after noon",
                "Brief mindfulness or breathing exercise",
                "Light social interaction"
            )
            CircadianPhase.AFTERNOON -> if (hour < 16) {
                listOf(
                    "Tackle creative or collaborative tasks",
                    "Moderate-intensity exercise is ideal now",
                    "Work on tasks requiring sustained attention",
                    "Have a small healthy snack if needed",
                    "Stand up and move every 45 minutes"
                )
            } else {
                listOf(
                    "Wrap up focused work for the day",
                    "Plan tomorrow's priorities",
                    "Strength training or cardio session",
                    "Begin transitioning to lighter activities",
                    "Social time or hobbies"
                )
            }
            CircadianPhase.EVENING -> listOf(
                "Reduce screen brightness and enable night mode",
                "Light stretching or yoga",
                "Journaling or reflection",
                "Prepare for tomorrow",
                "Avoid large meals close to bedtime"
            )
            CircadianPhase.NIGHT -> listOf(
                "Keep the environment dark and quiet",
                "Practice deep breathing or body scan",
                "Avoid screens and stimulating content",
                "Maintain a cool room temperature",
                "Follow your sleep routine consistently"
            )
        }
    }

    /**
     * Calculates an energy multiplier (0.5 to 1.2) based on typical circadian alertness patterns.
     *
     * The multiplier models the biphasic alertness curve:
     *   - Peak around 10:00 (1.2)
     *   - Post-lunch dip around 13:00-14:00 (0.7)
     *   - Second peak around 16:00 (1.0)
     *   - Evening decline toward sleep (0.5-0.6)
     *   - Night minimum (0.5)
     *
     * Uses a piecewise linear approximation of the circadian alertness curve.
     */
    private fun calculateEnergyMultiplier(hour: Int, minute: Int): Float {
        val timeDecimal = hour + minute / 60f

        return when {
            // Night: 0:00 - 4:59 -> 0.5
            timeDecimal < 5f -> 0.5f

            // Early morning ramp: 5:00 - 7:00 -> 0.5 to 0.8
            timeDecimal < 7f -> {
                val t = (timeDecimal - 5f) / 2f
                0.5f + t * 0.3f
            }

            // Morning peak ramp: 7:00 - 10:00 -> 0.8 to 1.2
            timeDecimal < 10f -> {
                val t = (timeDecimal - 7f) / 3f
                0.8f + t * 0.4f
            }

            // Holding peak: 10:00 - 12:00 -> 1.2 to 1.0
            timeDecimal < 12f -> {
                val t = (timeDecimal - 10f) / 2f
                1.2f - t * 0.2f
            }

            // Post-lunch dip: 12:00 - 14:00 -> 1.0 to 0.7
            timeDecimal < 14f -> {
                val t = (timeDecimal - 12f) / 2f
                1.0f - t * 0.3f
            }

            // Afternoon recovery: 14:00 - 16:00 -> 0.7 to 1.0
            timeDecimal < 16f -> {
                val t = (timeDecimal - 14f) / 2f
                0.7f + t * 0.3f
            }

            // Afternoon plateau: 16:00 - 18:00 -> 1.0 to 0.85
            timeDecimal < 18f -> {
                val t = (timeDecimal - 16f) / 2f
                1.0f - t * 0.15f
            }

            // Evening decline: 18:00 - 21:00 -> 0.85 to 0.6
            timeDecimal < 21f -> {
                val t = (timeDecimal - 18f) / 3f
                0.85f - t * 0.25f
            }

            // Night onset: 21:00 - 24:00 -> 0.6 to 0.5
            else -> {
                val t = (timeDecimal - 21f) / 3f
                0.6f - t * 0.1f
            }
        }.coerceIn(0.5f, 1.2f)
    }

    /**
     * Calculates how far into the current phase the user is.
     *
     * @return Pair of (hoursIntoPhase, progressPercent 0-100).
     */
    private fun calculatePhaseProgress(
        phase: CircadianPhase,
        hour: Int,
        minute: Int
    ): Pair<Float, Float> {
        val phaseStart = when (phase) {
            CircadianPhase.MORNING -> 5
            CircadianPhase.MIDDAY -> 12
            CircadianPhase.AFTERNOON -> 14
            CircadianPhase.EVENING -> 18
            CircadianPhase.NIGHT -> 22
        }

        val phaseDuration = when (phase) {
            CircadianPhase.MORNING -> 7f    // 5-11
            CircadianPhase.MIDDAY -> 2f     // 12-13
            CircadianPhase.AFTERNOON -> 4f  // 14-17
            CircadianPhase.EVENING -> 4f    // 18-21
            CircadianPhase.NIGHT -> 7f      // 22-4 (wraps around midnight)
        }

        val currentDecimal = hour + minute / 60f
        val startDecimal = phaseStart.toFloat()

        val hoursInto = if (phase == CircadianPhase.NIGHT && hour < 5) {
            // After midnight: hours since 22:00 = (24 - 22) + hour + minute/60
            (24f - startDecimal) + currentDecimal
        } else {
            currentDecimal - startDecimal
        }.coerceAtLeast(0f)

        val progress = ((hoursInto / phaseDuration) * 100f).coerceIn(0f, 100f)

        return Pair(
            (hoursInto * 10f).toInt() / 10f, // round to 1 decimal
            (progress * 10f).toInt() / 10f
        )
    }
}
