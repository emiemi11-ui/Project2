package com.vitanova.app.ui.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitanova.app.ui.theme.FocusAccent
import com.vitanova.app.ui.theme.VitaBackground
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaOutline
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaSurfaceVariant
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import com.vitanova.app.ui.theme.VitaWarning

private val durationOptions = listOf(25, 45, 60, 90)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: FocusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Inapoi",
                        tint = VitaTextPrimary
                    )
                }
                Text(
                    text = "Focus Timer",
                    style = MaterialTheme.typography.titleLarge,
                    color = VitaTextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Duration selector chips
        item {
            DurationSelector(
                selectedDuration = uiState.selectedDurationMinutes,
                isTimerRunning = uiState.isTimerRunning,
                onDurationSelected = { viewModel.setSelectedDuration(it) }
            )
        }

        // Circular countdown timer
        item {
            CircularCountdownTimer(
                currentSeconds = uiState.currentTimerSeconds,
                totalSeconds = uiState.selectedDurationMinutes * 60,
                isRunning = uiState.isTimerRunning,
                isPaused = uiState.isTimerPaused
            )
        }

        // Control buttons
        item {
            TimerControls(
                isRunning = uiState.isTimerRunning,
                isPaused = uiState.isTimerPaused,
                selectedDuration = uiState.selectedDurationMinutes,
                onStart = { viewModel.startFocusTimer(uiState.selectedDurationMinutes) },
                onPause = { viewModel.pauseTimer() },
                onStop = { viewModel.stopFocusTimer() }
            )
        }

        // Session info
        if (uiState.isTimerRunning) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.isTimerPaused) "Pauza" else "Concentreaza-te",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (uiState.isTimerPaused) VitaWarning else FocusAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Elimina distractiile. Ramai prezent.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VitaTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Completion celebration
        if (uiState.isTimerCompleted) {
            item {
                CompletionCard(
                    durationMinutes = uiState.selectedDurationMinutes,
                    streak = uiState.focusStreak,
                    onDismiss = { viewModel.dismissCompletion() }
                )
            }
        }

        // Streak info
        if (!uiState.isTimerRunning && !uiState.isTimerCompleted && uiState.focusStreak > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "\uD83D\uDD25",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Serie curenta: ${uiState.focusStreak} zile",
                            style = MaterialTheme.typography.titleMedium,
                            color = VitaWarning,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DurationSelector(
    selectedDuration: Int,
    isTimerRunning: Boolean,
    onDurationSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Durata sesiune",
            style = MaterialTheme.typography.titleSmall,
            color = VitaTextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            durationOptions.forEach { minutes ->
                FilterChip(
                    selected = selectedDuration == minutes,
                    onClick = { if (!isTimerRunning) onDurationSelected(minutes) },
                    label = {
                        Text(
                            text = "${minutes} min",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedDuration == minutes) FontWeight.Bold
                            else FontWeight.Normal
                        )
                    },
                    enabled = !isTimerRunning,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VitaGreen,
                        selectedLabelColor = Color(0xFF003321),
                        containerColor = VitaSurfaceVariant,
                        labelColor = VitaTextSecondary,
                        disabledContainerColor = if (selectedDuration == minutes)
                            VitaGreen.copy(alpha = 0.6f) else VitaSurfaceVariant,
                        disabledLabelColor = if (selectedDuration == minutes)
                            Color(0xFF003321) else VitaTextTertiary,
                        disabledSelectedContainerColor = VitaGreen.copy(alpha = 0.6f),
                        disabledSelectedLabelColor = Color(0xFF003321)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun CircularCountdownTimer(
    currentSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    isPaused: Boolean
) {
    val progress = if (totalSeconds > 0) currentSeconds.toFloat() / totalSeconds else 1f
    val minutes = currentSeconds / 60
    val seconds = currentSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val glowAlpha = if (isRunning && !isPaused) pulseAlpha else 0.3f

    Box(
        modifier = Modifier
            .size(260.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft = Offset(
                (size.width - 2 * radius) / 2,
                (size.height - 2 * radius) / 2
            )
            val arcSize = Size(radius * 2, radius * 2)

            // Background track
            drawArc(
                color = VitaOutline,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            val sweepAngle = progress * 360f
            if (sweepAngle > 0f) {
                // Glow effect
                drawArc(
                    color = FocusAccent.copy(alpha = glowAlpha * 0.4f),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        topLeft.x - 2.dp.toPx(),
                        topLeft.y - 2.dp.toPx()
                    ),
                    size = Size(
                        arcSize.width + 4.dp.toPx(),
                        arcSize.height + 4.dp.toPx()
                    ),
                    style = Stroke(
                        width = strokeWidth + 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                drawArc(
                    color = FocusAccent,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center time text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 52.sp,
                    letterSpacing = 2.sp,
                    color = VitaTextPrimary
                )
            )
            if (isRunning) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isPaused) "PAUZA" else "FOCUS",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isPaused) VitaWarning else FocusAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
            }
        }
    }
}

@Composable
private fun TimerControls(
    isRunning: Boolean,
    isPaused: Boolean,
    selectedDuration: Int,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRunning) {
            // Stop button
            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(VitaSurfaceVariant),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = VitaTextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = "Stop",
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(32.dp))

            // Pause/Resume button
            Button(
                onClick = onPause,
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPaused) VitaGreen else VitaWarning,
                    contentColor = if (isPaused) Color(0xFF003321) else Color(0xFF3D2800)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Filled.PlayArrow
                    else Icons.Filled.Pause,
                    contentDescription = if (isPaused) "Continua" else "Pauza",
                    modifier = Modifier.size(36.dp)
                )
            }
        } else {
            // Start button
            Button(
                onClick = onStart,
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth(0.6f),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitaGreen,
                    contentColor = Color(0xFF003321)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Incepe ${selectedDuration} min",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CompletionCard(
    durationMinutes: Int,
    streak: Int,
    onDismiss: () -> Unit
) {
    val scaleAnim = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = androidx.compose.animation.core.EaseOutBack
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = VitaGreen.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\uD83C\uDF89",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Felicitari!",
                style = MaterialTheme.typography.headlineMedium,
                color = VitaGreen,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$durationMinutes min focus completat",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextPrimary
            )
            if (streak > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Serie: $streak zile consecutive",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VitaWarning,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitaGreen,
                    contentColor = Color(0xFF003321)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Continua",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
