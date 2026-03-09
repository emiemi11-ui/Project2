package com.vitanova.app.ui.fitness

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitanova.app.ui.theme.FitnessAccent
import com.vitanova.app.ui.theme.VitaBackground
import com.vitanova.app.ui.theme.VitaError
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaSurfaceElevated
import com.vitanova.app.ui.theme.VitaSurfaceVariant
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import com.vitanova.app.ui.theme.VitaWarning
import kotlinx.coroutines.delay
import java.util.Locale

// ── Data Models ──────────────────────────────────────────────────────────────

private data class ExerciseCategory(
    val name: String,
    val color: Color,
    val exercises: List<Exercise>
)

private data class Exercise(
    val name: String,
    val sets: Int,
    val reps: Int,
    val description: String,
    val caloriesPerSet: Int = 5
)

private data class ChallengeDay(
    val day: Int,
    val exercises: List<ChallengeExercise>
)

private data class ChallengeExercise(
    val name: String,
    val sets: Int,
    val reps: Int
)

// ── Exercise Database ────────────────────────────────────────────────────────

private val exerciseCategories = listOf(
    ExerciseCategory(
        name = "Upper Body",
        color = Color(0xFFEF4444),
        exercises = listOf(
            Exercise("Flotari clasice", 3, 15, "Maini la latimea umerilor, coboari pieptul pana aproape de sol, impingi in sus cu control."),
            Exercise("Flotari diamant", 3, 12, "Maini apropiate sub piept, formand un diamant cu degetele. Accentueaza tricepsul."),
            Exercise("Flotari decline", 3, 10, "Picioarele pe o suprafata inaltata. Creste implicarea umerilor si pieptului superior."),
            Exercise("Dips pe scaun", 3, 12, "Mainile pe marginea scaunului, coboari corpul cu control. Triceps si piept inferior."),
            Exercise("Pike push-ups", 3, 10, "Pozitie de V inversat, impingi vertical. Simuleaza shoulder press cu greutatea corpului."),
            Exercise("Commando plank", 3, 10, "Din plank pe antebrate, ridica-te pe maini alternativ. Umeri si core."),
            Exercise("Izometrie perete", 3, 30, "Tine pozitia de flotare la jumatate, cu spatele la perete. Izometric piept/umeri.")
        )
    ),
    ExerciseCategory(
        name = "Core",
        color = Color(0xFFFBBF24),
        exercises = listOf(
            Exercise("Plank", 3, 45, "Mentine corpul drept pe antebrate si varfuri. Contracta abdomenul constant."),
            Exercise("Bicycle crunches", 3, 20, "Cotul drept spre genunchiul stang alternativ. Rotatie controlata a trunchiului."),
            Exercise("Mountain climbers", 3, 20, "Din pozitia de plank, adu genunchii alternativ spre piept cu ritm sustinut."),
            Exercise("Russian twists", 3, 20, "Sezand cu picioarele ridicate, roteste trunchiul de la o parte la alta."),
            Exercise("Leg raises", 3, 15, "Intins pe spate, ridica picioarele drepte la 90 de grade cu control."),
            Exercise("Dead bug", 3, 12, "Pe spate, brate si picioare sus. Coboara alternativ bratul si piciorul opus."),
            Exercise("Plank lateral", 3, 30, "Pe antebrat lateral, mentine corpul drept. Oblic si stabilizatori.")
        )
    ),
    ExerciseCategory(
        name = "Lower Body",
        color = Color(0xFF3B82F6),
        exercises = listOf(
            Exercise("Genuflexiuni", 4, 15, "Picioarele la latimea umerilor, coboari ca si cum te-ai aseza pe scaun. Genunchii nu depasesc varfurile."),
            Exercise("Fandari", 3, 12, "Pas mare inainte, coboari pana genunchiul din spate aproape atinge solul. Alternativ."),
            Exercise("Sumo squats", 3, 15, "Picioarele departe, varfuri in afara. Accentueaza aductorii si fesierii."),
            Exercise("Glute bridges", 3, 15, "Pe spate, genunchi indoiti, ridica soldurile pana corpul e drept. Stringe fesierii sus."),
            Exercise("Jump squats", 3, 12, "Genuflexiune completa urmata de saritura explosiva. Aterizare moale pe varfuri."),
            Exercise("Calf raises", 3, 20, "Pe marginea unei trepte, ridica-te pe varfuri si coboari calcaiele sub nivel."),
            Exercise("Wall sit", 3, 45, "Spatele lipit de perete, genunchi la 90 grade. Izometrie cvadricepsi.")
        )
    ),
    ExerciseCategory(
        name = "Full Body",
        color = Color(0xFFA855F7),
        exercises = listOf(
            Exercise("Burpees", 3, 10, "Din stand: genuflexiune, plank, flotare, saritura. Cel mai complet exercitiu full body."),
            Exercise("Bear crawl", 3, 20, "Mergi in 4 labe cu genunchii la 2 cm de sol. Coordonare si stabilitate."),
            Exercise("Inchworm", 3, 10, "Din stand, mergi pe maini in plank, faci o flotare, mergi inapoi cu mainile."),
            Exercise("Squat to press", 3, 12, "Genuflexiune urmata de ridicare pe varfuri cu bratele intinse sus."),
            Exercise("Plank to squat", 3, 10, "Din plank, sari cu picioarele langa maini in genuflexiune, revino."),
            Exercise("Star jumps", 3, 15, "Saritura cu bratele si picioarele deschise in stea. Cardio si coordonare."),
            Exercise("Turkish get-up", 3, 5, "De la culcat la stand cu bratul intins vertical. Stabilitate complexa.")
        )
    ),
    ExerciseCategory(
        name = "Cardio",
        color = Color(0xFFEC4899),
        exercises = listOf(
            Exercise("Jumping jacks", 3, 30, "Saritura cu deschiderea bratelor si picioarelor simultan. Incalzire si cardio."),
            Exercise("High knees", 3, 30, "Alergare pe loc ridicand genunchii la nivelul soldurilor. Ritm sustinut."),
            Exercise("Butt kicks", 3, 30, "Alergare pe loc, calcaiele lovesc fesierii. Incalzire ischiogambieri."),
            Exercise("Skipping", 3, 20, "Sarituri alternand genunchii sus cu miscare de brate. Coordonare si cardio."),
            Exercise("Box step-ups", 3, 15, "Urca pe o treapta/scaun alternand picioarele. Cvadricepsi si cardio."),
            Exercise("Speed skaters", 3, 20, "Sarituri laterale de pe un picior pe altul. Echilibru si agilitate."),
            Exercise("Shuttle runs", 3, 10, "Sprint scurt 5m, atingere sol, sprint inapoi. Agilitate si explozie.")
        )
    )
)

// 30-day challenge daily workouts
private val challengeDays: List<ChallengeDay> = (1..30).map { day ->
    val intensity = when {
        day % 7 == 0 -> 0 // rest day
        day <= 10 -> 1
        day <= 20 -> 2
        else -> 3
    }
    if (intensity == 0) {
        ChallengeDay(day, listOf(ChallengeExercise("Zi de odihna - stretching", 1, 1)))
    } else {
        val baseReps = 8 + intensity * 3
        val baseSets = 2 + (intensity - 1)
        ChallengeDay(
            day,
            listOf(
                ChallengeExercise("Genuflexiuni", baseSets, baseReps),
                ChallengeExercise("Flotari", baseSets, baseReps - 3),
                ChallengeExercise("Plank (sec)", baseSets, 20 + intensity * 10),
                ChallengeExercise("Fandari", baseSets, baseReps - 2),
                ChallengeExercise("Mountain climbers", baseSets, baseReps)
            )
        )
    }
}

// ── Workout Screen ───────────────────────────────────────────────────────────

@Composable
fun WorkoutScreen(
    onNavigateBack: () -> Unit
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(-1) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var showTimer by remember { mutableStateOf(false) }
    var showChallenge by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Inapoi",
                    tint = VitaTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = null,
                tint = Color(0xFFA855F7),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Antrenament",
                style = MaterialTheme.typography.titleLarge,
                color = VitaTextPrimary
            )
        }

        if (showTimer && selectedExercise != null) {
            WorkoutTimerView(
                exercise = selectedExercise!!,
                onClose = {
                    showTimer = false
                    selectedExercise = null
                }
            )
        } else if (showChallenge) {
            ChallengeView(
                onClose = { showChallenge = false }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 30-day challenge banner
                item {
                    ChallengeQuickCard(onClick = { showChallenge = true })
                }

                // Category chips
                item {
                    Text(
                        text = "Categorii exercitii",
                        style = MaterialTheme.typography.titleMedium,
                        color = VitaTextPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(exerciseCategories) { index, category ->
                            val isSelected = index == selectedCategoryIndex
                            Card(
                                modifier = Modifier.clickable {
                                    selectedCategoryIndex = if (isSelected) -1 else index
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) category.color.copy(alpha = 0.2f) else VitaSurfaceElevated
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) category.color else VitaTextSecondary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                // Exercise list for selected category
                if (selectedCategoryIndex >= 0) {
                    val category = exerciseCategories[selectedCategoryIndex]
                    items(category.exercises) { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            accentColor = category.color,
                            onStartWorkout = {
                                selectedExercise = exercise
                                showTimer = true
                            }
                        )
                    }
                } else {
                    // Show all categories with top exercises
                    items(exerciseCategories) { category ->
                        CategoryPreviewCard(
                            category = category,
                            onSelectCategory = {
                                selectedCategoryIndex = exerciseCategories.indexOf(category)
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ── Challenge Quick Card ─────────────────────────────────────────────────────

@Composable
private fun ChallengeQuickCard(onClick: () -> Unit) {
    val today = remember { java.time.LocalDate.now() }
    val challengeDay = remember { ((today.dayOfMonth - 1) % 30) + 1 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFA855F7).copy(alpha = 0.15f),
                            FitnessAccent.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = FitnessAccent.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = FitnessAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Provocare 30 Zile",
                        style = MaterialTheme.typography.titleSmall,
                        color = FitnessAccent
                    )
                    Text(
                        text = "Ziua $challengeDay/30",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = VitaTextPrimary
                    )
                    Text(
                        text = "Apasa pentru detalii antrenament",
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextTertiary
                    )
                }
            }
        }
    }
}

// ── Category Preview Card ────────────────────────────────────────────────────

@Composable
private fun CategoryPreviewCard(
    category: ExerciseCategory,
    onSelectCategory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectCategory),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = category.color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = VitaTextPrimary
                    )
                    Text(
                        text = "${category.exercises.size} exercitii",
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextTertiary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.exercises.take(3).joinToString(" | ") { it.name },
                style = MaterialTheme.typography.bodySmall,
                color = VitaTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Exercise Card ────────────────────────────────────────────────────────────

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    accentColor: Color,
    onStartWorkout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = VitaTextPrimary
                    )
                    Text(
                        text = "${exercise.sets} x ${exercise.reps} repetari",
                        style = MaterialTheme.typography.bodyMedium,
                        color = accentColor
                    )
                }
                FilledTonalButton(
                    onClick = onStartWorkout,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = accentColor.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodySmall,
                color = VitaTextTertiary
            )
        }
    }
}

// ── Workout Timer View ───────────────────────────────────────────────────────

private enum class TimerPhase { WORK, REST, FINISHED }

@Composable
private fun WorkoutTimerView(
    exercise: Exercise,
    onClose: () -> Unit
) {
    val workDurationSec = 45
    val restDurationSec = 15
    val totalSets = exercise.sets

    var currentSet by remember { mutableIntStateOf(1) }
    var currentRep by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf(TimerPhase.WORK) }
    var timeRemainingMs by remember { mutableStateOf((workDurationSec * 1000).toLong()) }
    var isRunning by remember { mutableStateOf(false) }
    var totalElapsedMs by remember { mutableStateOf(0L) }
    val completedSets = remember { mutableStateListOf<Boolean>() }

    // Initialize completed sets tracking
    LaunchedEffect(totalSets) {
        completedSets.clear()
        for (i in 0 until totalSets) {
            completedSets.add(false)
        }
    }

    // Timer countdown
    LaunchedEffect(isRunning, phase) {
        if (isRunning && phase != TimerPhase.FINISHED) {
            while (isRunning && timeRemainingMs > 0) {
                delay(100)
                timeRemainingMs -= 100
                totalElapsedMs += 100
            }
            if (timeRemainingMs <= 0 && isRunning) {
                when (phase) {
                    TimerPhase.WORK -> {
                        if (currentSet - 1 < completedSets.size) {
                            completedSets[currentSet - 1] = true
                        }
                        if (currentSet >= totalSets) {
                            phase = TimerPhase.FINISHED
                            isRunning = false
                        } else {
                            phase = TimerPhase.REST
                            timeRemainingMs = (restDurationSec * 1000).toLong()
                        }
                    }
                    TimerPhase.REST -> {
                        currentSet++
                        phase = TimerPhase.WORK
                        timeRemainingMs = (workDurationSec * 1000).toLong()
                    }
                    TimerPhase.FINISHED -> {}
                }
            }
        }
    }

    val totalPhaseDuration = when (phase) {
        TimerPhase.WORK -> workDurationSec * 1000L
        TimerPhase.REST -> restDurationSec * 1000L
        TimerPhase.FINISHED -> 1L
    }
    val progress = if (phase != TimerPhase.FINISHED) {
        1f - (timeRemainingMs.toFloat() / totalPhaseDuration)
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(100, easing = LinearEasing),
        label = "timer_progress"
    )

    val phaseColor = when (phase) {
        TimerPhase.WORK -> FitnessAccent
        TimerPhase.REST -> VitaGreen
        TimerPhase.FINISHED -> Color(0xFFA855F7)
    }

    val timeSeconds = (timeRemainingMs / 1000).toInt()
    val displayMinutes = timeSeconds / 60
    val displaySeconds = timeSeconds % 60
    val totalWorkoutSeconds = totalElapsedMs / 1000
    val totalCalories = ((exercise.caloriesPerSet * completedSets.count { it }) +
            (exercise.caloriesPerSet * (totalElapsedMs / 60_000.0) * 0.5)).toInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = VitaTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${exercise.sets} seturi x ${exercise.reps} repetari",
                style = MaterialTheme.typography.bodyMedium,
                color = VitaTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Timer ring
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                val trackColor = VitaSurfaceVariant
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val topLeft = Offset(
                        (size.width - radius * 2) / 2,
                        (size.height - radius * 2) / 2
                    )
                    val arcSize = Size(radius * 2, radius * 2)

                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = phaseColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (phase) {
                            TimerPhase.WORK -> "LUCRU"
                            TimerPhase.REST -> "PAUZA"
                            TimerPhase.FINISHED -> "GATA!"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = phaseColor
                    )
                    if (phase != TimerPhase.FINISHED) {
                        Text(
                            text = String.format(Locale.getDefault(), "%d:%02d", displayMinutes, displaySeconds),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 64.sp
                            ),
                            color = VitaTextPrimary
                        )
                    }
                    Text(
                        text = "Set $currentSet/$totalSets",
                        style = MaterialTheme.typography.titleSmall,
                        color = VitaTextSecondary
                    )
                }
            }
        }

        // Set completion checkboxes
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Progres seturi",
                        style = MaterialTheme.typography.titleSmall,
                        color = VitaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (i in 0 until totalSets) {
                            val isComplete = i < completedSets.size && completedSets[i]
                            val isCurrent = i == currentSet - 1 && phase != TimerPhase.FINISHED
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    if (i < completedSets.size) {
                                        completedSets[i] = !completedSets[i]
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isComplete) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                    contentDescription = "Set ${i + 1}",
                                    tint = when {
                                        isComplete -> VitaGreen
                                        isCurrent -> FitnessAccent
                                        else -> VitaTextTertiary
                                    },
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "S${i + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        isComplete -> VitaGreen
                                        isCurrent -> FitnessAccent
                                        else -> VitaTextTertiary
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = FitnessAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatWorkoutTime(totalWorkoutSeconds),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = VitaTextPrimary
                        )
                        Text(
                            text = "Timp total",
                            style = MaterialTheme.typography.labelSmall,
                            color = VitaTextTertiary
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalCalories",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = VitaTextPrimary
                        )
                        Text(
                            text = "Calorii",
                            style = MaterialTheme.typography.labelSmall,
                            color = VitaTextTertiary
                        )
                    }
                }
            }
        }

        // Control buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (phase == TimerPhase.FINISHED) {
                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VitaGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "TERMINAT",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    FilledTonalButton(
                        onClick = { isRunning = !isRunning },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isRunning) VitaSurfaceElevated else phaseColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = if (isRunning) VitaTextPrimary else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "PAUZA" else "START",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isRunning) VitaTextPrimary else Color.White
                        )
                    }

                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VitaError),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STOP",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

// ── Challenge View ───────────────────────────────────────────────────────────

@Composable
private fun ChallengeView(onClose: () -> Unit) {
    val today = remember { java.time.LocalDate.now() }
    val challengeDay = remember { ((today.dayOfMonth - 1) % 30) + 1 }
    val todayWorkout = remember { challengeDays[challengeDay - 1] }

    val completedExercises = remember { mutableStateListOf<Boolean>() }
    LaunchedEffect(todayWorkout) {
        completedExercises.clear()
        todayWorkout.exercises.forEach { _ -> completedExercises.add(false) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Inapoi",
                        tint = VitaTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Provocare 30 Zile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = FitnessAccent
                    )
                    Text(
                        text = "Ziua $challengeDay/30",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VitaTextSecondary
                    )
                }
            }
        }

        // Progress bar for 30 days
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Progres general",
                        style = MaterialTheme.typography.titleSmall,
                        color = VitaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val challengeProgress = challengeDay / 30f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(VitaSurfaceVariant, RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(challengeProgress)
                                .height(8.dp)
                                .background(FitnessAccent, RoundedCornerShape(4.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(challengeProgress * 100).toInt()}% completat",
                        style = MaterialTheme.typography.bodySmall,
                        color = FitnessAccent
                    )
                }
            }
        }

        // Today's exercises
        item {
            Text(
                text = "Antrenamentul de azi",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        itemsIndexed(todayWorkout.exercises) { index, exercise ->
            val isComplete = index < completedExercises.size && completedExercises[index]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (index < completedExercises.size) {
                            completedExercises[index] = !completedExercises[index]
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isComplete) VitaGreen.copy(alpha = 0.1f) else VitaSurfaceCard
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isComplete) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isComplete) VitaGreen else VitaTextTertiary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isComplete) VitaGreen else VitaTextPrimary
                        )
                        Text(
                            text = "${exercise.sets} x ${exercise.reps}",
                            style = MaterialTheme.typography.bodySmall,
                            color = VitaTextTertiary
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

// ── Utility ──────────────────────────────────────────────────────────────────

private fun formatWorkoutTime(totalSeconds: Long): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", m, s)
}
