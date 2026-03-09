package com.vitanova.app.ui.fitness

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitanova.app.data.local.entity.FitnessActivity
import com.vitanova.app.ui.theme.FitnessAccent
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaSurfaceElevated
import com.vitanova.app.ui.theme.VitaSurfaceVariant
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import com.vitanova.app.ui.theme.VitaWarning
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// ── 30-Day Challenge Data ────────────────────────────────────────────────────

private data class DailyChallenge(
    val day: Int,
    val exerciseName: String,
    val description: String
)

private val challengeWorkouts = listOf(
    DailyChallenge(1, "20 Genuflexiuni", "Incepe usor cu genuflexiuni simple"),
    DailyChallenge(2, "15 Flotari", "Flotari clasice sau pe genunchi"),
    DailyChallenge(3, "30s Plank", "Mentine pozitia de plank"),
    DailyChallenge(4, "20 Fandari", "10 pe fiecare picior"),
    DailyChallenge(5, "25 Abdomene", "Crunches clasice"),
    DailyChallenge(6, "30 Jumping Jacks", "Cardio de baza"),
    DailyChallenge(7, "Zi de odihna", "Stretching usor 10 min"),
    DailyChallenge(8, "25 Genuflexiuni", "Adauga greutate proprie"),
    DailyChallenge(9, "20 Flotari", "Mentine forma corecta"),
    DailyChallenge(10, "45s Plank", "Creste durata"),
    DailyChallenge(11, "30 Fandari", "15 pe fiecare picior"),
    DailyChallenge(12, "30 Abdomene", "Adauga bicycle crunches"),
    DailyChallenge(13, "40 Jumping Jacks", "Creste intensitatea"),
    DailyChallenge(14, "Zi de odihna", "Stretching complet 15 min"),
    DailyChallenge(15, "30 Genuflexiuni Sumo", "Picioarele mai departe"),
    DailyChallenge(16, "25 Flotari diamant", "Maini apropiate"),
    DailyChallenge(17, "60s Plank", "1 minut complet"),
    DailyChallenge(18, "20 Burpees", "Full body cardio"),
    DailyChallenge(19, "40 Mountain Climbers", "Ritm sustinut"),
    DailyChallenge(20, "35 Abdomene + 20 Bicycle", "Combinatie abdomen"),
    DailyChallenge(21, "Zi de odihna", "Yoga sau stretching 15 min"),
    DailyChallenge(22, "40 Genuflexiuni cu salt", "Jump squats"),
    DailyChallenge(23, "30 Flotari decline", "Picioarele ridicate"),
    DailyChallenge(24, "75s Plank", "Rezistenta crescuta"),
    DailyChallenge(25, "25 Burpees", "Fara pauza"),
    DailyChallenge(26, "50 Mountain Climbers", "Intensitate maxima"),
    DailyChallenge(27, "Circuit complet", "Toate exercitiile x10"),
    DailyChallenge(28, "Zi de odihna", "Recuperare activa"),
    DailyChallenge(29, "Circuit dublu", "Toate exercitiile x15"),
    DailyChallenge(30, "Test final", "Max repetari 2 min per exercitiu")
)

// ── Main Fitness Screen ──────────────────────────────────────────────────────

@Composable
fun FitnessScreen(
    viewModel: FitnessViewModel,
    onNavigateToActiveTracking: (String) -> Unit,
    onNavigateToWorkout: () -> Unit,
    onNavigateToStretching: () -> Unit
) {
    val todaySteps by viewModel.todaySteps.collectAsState()
    val stepGoal by viewModel.stepGoal.collectAsState()
    val recentActivities by viewModel.recentActivities.collectAsState()
    val weeklyDistanceKm by viewModel.weeklyDistanceKm.collectAsState()
    val weeklyCalories by viewModel.weeklyCalories.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Fitness",
                style = MaterialTheme.typography.headlineLarge,
                color = VitaTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Step Progress Ring
        item {
            StepProgressCard(
                currentSteps = todaySteps,
                goalSteps = stepGoal
            )
        }

        // Weekly Summary Cards
        item {
            WeeklySummaryRow(
                distanceKm = weeklyDistanceKm,
                calories = weeklyCalories
            )
        }

        // Quick Actions
        item {
            QuickActionsSection(
                onStartRunning = { onNavigateToActiveTracking("running") },
                onStartCycling = { onNavigateToActiveTracking("cycling") },
                onStartWalking = { onNavigateToActiveTracking("walking") },
                onNavigateToWorkout = onNavigateToWorkout,
                onNavigateToStretching = onNavigateToStretching
            )
        }

        // 30-Day Challenge Card
        item {
            ChallengeCard()
        }

        // Recent Activities
        if (recentActivities.isNotEmpty()) {
            item {
                Text(
                    text = "Activitati recente",
                    style = MaterialTheme.typography.titleMedium,
                    color = VitaTextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(recentActivities.take(10)) { activity ->
                ActivityCard(activity = activity)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ── Step Progress Ring ───────────────────────────────────────────────────────

@Composable
private fun StepProgressCard(
    currentSteps: Int,
    goalSteps: Int
) {
    val progress = (currentSteps.toFloat() / goalSteps.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "step_progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pasi astazi",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                val ringColor = FitnessAccent
                val trackColor = VitaSurfaceVariant
                val completedColor = VitaGreen

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 16.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2f
                    val topLeft = Offset(
                        (size.width - radius * 2) / 2f,
                        (size.height - radius * 2) / 2f
                    )
                    val arcSize = Size(radius * 2, radius * 2)

                    // Background track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc
                    val progressColor = if (animatedProgress >= 1f) completedColor else ringColor
                    drawArc(
                        color = progressColor,
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
                        text = "%,d".format(currentSteps),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        ),
                        color = VitaTextPrimary
                    )
                    Text(
                        text = "/ %,d".format(goalSteps),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VitaTextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val percentage = ((currentSteps.toFloat() / goalSteps) * 100).toInt().coerceAtMost(100)
            Text(
                text = "$percentage% din obiectiv",
                style = MaterialTheme.typography.bodyMedium,
                color = if (percentage >= 100) VitaGreen else FitnessAccent
            )
        }
    }
}

// ── Weekly Summary ───────────────────────────────────────────────────────────

@Composable
private fun WeeklySummaryRow(
    distanceKm: Float,
    calories: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Route,
            label = "Distanta saptamana",
            value = "%.1f km".format(distanceKm),
            accentColor = FitnessAccent
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.LocalFireDepartment,
            label = "Calorii saptamana",
            value = "%,d kcal".format(calories),
            accentColor = Color(0xFFEF4444)
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = VitaTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = VitaTextTertiary
            )
        }
    }
}

// ── Quick Actions ────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsSection(
    onStartRunning: () -> Unit,
    onStartCycling: () -> Unit,
    onStartWalking: () -> Unit,
    onNavigateToWorkout: () -> Unit,
    onNavigateToStretching: () -> Unit
) {
    Column {
        Text(
            text = "Incepe activitate",
            style = MaterialTheme.typography.titleMedium,
            color = VitaTextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                QuickActionChip(
                    icon = Icons.Filled.DirectionsRun,
                    label = "Start Alergare",
                    gradientColors = listOf(FitnessAccent, Color(0xFFEA580C)),
                    onClick = onStartRunning
                )
            }
            item {
                QuickActionChip(
                    icon = Icons.Filled.DirectionsBike,
                    label = "Start Ciclism",
                    gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
                    onClick = onStartCycling
                )
            }
            item {
                QuickActionChip(
                    icon = Icons.Filled.DirectionsWalk,
                    label = "Start Plimbare",
                    gradientColors = listOf(VitaGreen, Color(0xFF059669)),
                    onClick = onStartWalking
                )
            }
            item {
                QuickActionChip(
                    icon = Icons.Filled.FitnessCenter,
                    label = "Antrenament",
                    gradientColors = listOf(Color(0xFFA855F7), Color(0xFF7C3AED)),
                    onClick = onNavigateToWorkout
                )
            }
            item {
                QuickActionChip(
                    icon = Icons.Filled.SelfImprovement,
                    label = "Stretching",
                    gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF0891B2)),
                    onClick = onNavigateToStretching
                )
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── 30-Day Challenge Card ────────────────────────────────────────────────────

@Composable
private fun ChallengeCard() {
    val today = remember { LocalDate.now() }
    val challengeStartDay = remember {
        // Challenge day based on day-of-month, cycling 1-30
        ((today.dayOfMonth - 1) % 30) + 1
    }
    val todayChallenge = remember { challengeWorkouts[challengeStartDay - 1] }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            FitnessAccent.copy(alpha = 0.15f),
                            VitaWarning.copy(alpha = 0.1f)
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
                        .size(56.dp)
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
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Provocare 30 Zile",
                        style = MaterialTheme.typography.titleSmall,
                        color = FitnessAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ziua $challengeStartDay/30",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = VitaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todayChallenge.exerciseName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VitaTextSecondary
                    )
                    Text(
                        text = todayChallenge.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextTertiary
                    )
                }
            }
        }
    }
}

// ── Activity Card ────────────────────────────────────────────────────────────

@Composable
private fun ActivityCard(activity: FitnessActivity) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale("ro")) }
    val dateStr = remember(activity.startTime) { dateFormat.format(Date(activity.startTime)) }

    val icon = when (activity.type) {
        "running" -> Icons.Filled.DirectionsRun
        "cycling" -> Icons.Filled.DirectionsBike
        "walking" -> Icons.Filled.DirectionsWalk
        "swimming" -> Icons.Filled.Pool
        "gym" -> Icons.Filled.FitnessCenter
        else -> Icons.Filled.DirectionsRun
    }

    val typeName = when (activity.type) {
        "running" -> "Alergare"
        "cycling" -> "Ciclism"
        "walking" -> "Plimbare"
        "swimming" -> "Inot"
        "gym" -> "Sala"
        else -> activity.type.replaceFirstChar { it.uppercase() }
    }

    val iconColor = when (activity.type) {
        "running" -> FitnessAccent
        "cycling" -> Color(0xFF3B82F6)
        "walking" -> VitaGreen
        "swimming" -> Color(0xFF06B6D4)
        "gym" -> Color(0xFFA855F7)
        else -> FitnessAccent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = iconColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeName,
                    style = MaterialTheme.typography.titleSmall,
                    color = VitaTextPrimary
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = VitaTextTertiary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (activity.distanceMeters != null && activity.distanceMeters > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Route,
                            contentDescription = null,
                            tint = VitaTextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.2f km".format(activity.distanceMeters / 1000f),
                            style = MaterialTheme.typography.bodySmall,
                            color = VitaTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = null,
                        tint = VitaTextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDurationMinutes(activity.durationMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextSecondary
                    )
                }

                if (activity.avgPace != null && activity.avgPace > 0) {
                    Text(
                        text = formatPace(activity.avgPace),
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextTertiary
                    )
                }
            }
        }
    }
}

// ── Utility ──────────────────────────────────────────────────────────────────

private fun formatDurationMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}min" else "${m} min"
}

private fun formatPace(paceMinPerKm: Float): String {
    val wholeMinutes = paceMinPerKm.toInt()
    val seconds = ((paceMinPerKm - wholeMinutes) * 60).toInt()
    return "%d:%02d /km".format(wholeMinutes, seconds)
}
