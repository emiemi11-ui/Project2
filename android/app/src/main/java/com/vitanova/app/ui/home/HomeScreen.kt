package com.vitanova.app.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitanova.app.ui.components.RadialGauge
import com.vitanova.app.ui.components.SparkLine
import com.vitanova.app.ui.components.CircadianTimeline
import com.vitanova.app.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToHrvMeasure: () -> Unit = {},
    onNavigateToFocusTimer: () -> Unit = {},
    onNavigateToWorkout: () -> Unit = {},
    onNavigateToHabits: () -> Unit = {},
    onNavigateToSleep: () -> Unit = {},
    onNavigateToEnergy: () -> Unit = {},
    onNavigateToFocus: () -> Unit = {},
    onNavigateToFitness: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val animatedReadiness by animateFloatAsState(
        targetValue = uiState.readinessScore.toFloat(),
        animationSpec = tween(durationMillis = 1200),
        label = "readiness"
    )

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(uiState.gradientColorStart),
            Color(uiState.gradientColorEnd),
            VitaBackground
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Greeting
        Text(
            text = uiState.greeting,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = VitaTextPrimary
        )
        Text(
            text = uiState.summaryText,
            style = MaterialTheme.typography.bodyMedium,
            color = VitaTextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main Readiness Gauge
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            RadialGauge(
                value = animatedReadiness,
                maxValue = 100f,
                label = "Readiness",
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mini Cards Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 12.dp)
        ) {
            item {
                MiniScoreCard(
                    title = "Sleep",
                    score = uiState.sleepScore,
                    sparkData = uiState.sleepHistory,
                    accentColor = SleepAccent,
                    icon = Icons.Filled.Nightlight,
                    onClick = onNavigateToSleep
                )
            }
            item {
                MiniScoreCard(
                    title = "Energy",
                    score = uiState.hrvScore,
                    sparkData = uiState.hrvHistory,
                    accentColor = EnergyAccent,
                    icon = Icons.Filled.ElectricBolt,
                    onClick = onNavigateToEnergy
                )
            }
            item {
                MiniScoreCard(
                    title = "Focus",
                    score = uiState.focusScore,
                    sparkData = uiState.focusHistory,
                    accentColor = FocusAccent,
                    icon = Icons.Filled.Visibility,
                    onClick = onNavigateToFocus
                )
            }
            item {
                MiniScoreCard(
                    title = "Fitness",
                    score = uiState.fitnessScore,
                    sparkData = uiState.fitnessHistory,
                    accentColor = FitnessAccent,
                    icon = Icons.Filled.DirectionsRun,
                    onClick = onNavigateToFitness
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Circadian Timeline
        Text(
            text = "Circadian Rhythm",
            style = MaterialTheme.typography.titleSmall,
            color = VitaTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        CircadianTimeline(
            currentHour = uiState.currentHour,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleSmall,
            color = VitaTextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                label = "Measure HRV",
                icon = Icons.Filled.FavoriteBorder,
                color = EnergyAccent,
                onClick = onNavigateToHrvMeasure,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Start Focus",
                icon = Icons.Filled.Timer,
                color = FocusAccent,
                onClick = onNavigateToFocusTimer,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                label = "Workout",
                icon = Icons.Filled.FitnessCenter,
                color = FitnessAccent,
                onClick = onNavigateToWorkout,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Habits Preview
        if (uiState.habitsSummary.isNotEmpty()) {
            Text(
                text = "Today's Habits",
                style = MaterialTheme.typography.titleSmall,
                color = VitaTextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            uiState.habitsSummary.take(3).forEach { habit ->
                HabitPreviewRow(
                    name = habit.name,
                    completed = habit.completedToday,
                    streak = habit.currentStreak,
                    color = try { Color(android.graphics.Color.parseColor(habit.color)) } catch (_: Exception) { VitaGreen }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (uiState.habitsSummary.size > 3) {
                TextButton(onClick = onNavigateToHabits) {
                    Text("View all habits →", color = VitaGreen, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun MiniScoreCard(
    title: String,
    score: Int,
    sparkData: List<Float>,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = VitaTextTertiary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
            if (sparkData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                SparkLine(
                    data = sparkData,
                    lineColor = accentColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HabitPreviewRow(
    name: String,
    completed: Boolean,
    streak: Int,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (completed) color else VitaSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (completed) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Done",
                        tint = VitaBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (completed) VitaTextTertiary else VitaTextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${streak}d",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}
