package com.vitanova.app.ui.energy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitanova.app.ui.theme.EnergyAccent
import com.vitanova.app.ui.theme.VitaBackground
import com.vitanova.app.ui.theme.VitaError
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaOutline
import com.vitanova.app.ui.theme.VitaSurface
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaSurfaceElevated
import com.vitanova.app.ui.theme.VitaSurfaceVariant
import com.vitanova.app.ui.theme.VitaTextOnPrimary
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import kotlinx.coroutines.delay
import kotlin.math.sin

// ── Main Screen ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HrvMeasureScreen(
    onNavigateBack: () -> Unit,
    onSaveMeasurement: (bpm: Int, rmssd: Float) -> Unit
) {
    // Local UI state to simulate the measurement flow without requiring CameraPpgManager lifecycle binding
    var measurementPhase by remember { mutableStateOf(MeasurementPhase.WAITING) }
    var timeRemainingSeconds by remember { mutableIntStateOf(60) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isFingerDetected by remember { mutableStateOf(false) }
    var currentBpm by remember { mutableIntStateOf(0) }
    var wavePhase by remember { mutableFloatStateOf(0f) }
    var signalQuality by remember { mutableFloatStateOf(0f) }

    // Results (populated when measurement completes or from PPG manager)
    var resultBpm by remember { mutableIntStateOf(0) }
    var resultRmssd by remember { mutableFloatStateOf(0f) }
    var resultEnergyScore by remember { mutableIntStateOf(0) }
    var resultStressScore by remember { mutableIntStateOf(0) }
    var hasSaved by remember { mutableStateOf(false) }

    // Simulate countdown when finger detected and measuring
    LaunchedEffect(measurementPhase, isFingerDetected) {
        if (measurementPhase == MeasurementPhase.MEASURING && isFingerDetected) {
            while (timeRemainingSeconds > 0) {
                delay(1000L)
                timeRemainingSeconds--
                progress = 1f - (timeRemainingSeconds / 60f)

                // Simulate BPM stabilization
                if (currentBpm == 0) {
                    currentBpm = (65..80).random()
                } else {
                    currentBpm = (currentBpm + (-2..2).random()).coerceIn(50, 120)
                }
                signalQuality = (progress * 0.8f + 0.2f).coerceIn(0f, 1f)
            }

            // Measurement complete
            resultBpm = currentBpm
            resultRmssd = (25f + (currentBpm - 50) * 0.5f).coerceIn(10f, 100f)
            resultEnergyScore = ((100f - (currentBpm - 55f).coerceIn(0f, 50f)) * 0.8f + 20f).toInt().coerceIn(0, 100)
            resultStressScore = (100 - resultEnergyScore).coerceIn(0, 100)
            measurementPhase = MeasurementPhase.COMPLETED
        }
    }

    // Waveform animation
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    // Pulsing animation for BPM circle
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (currentBpm > 0) (60_000 / currentBpm.coerceAtLeast(30)) else 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        containerColor = VitaBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Măsurare HRV",
                        style = MaterialTheme.typography.titleLarge,
                        color = VitaTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi",
                            tint = VitaTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VitaBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Camera Preview Placeholder ───────────────────────────────
            CameraPreviewPlaceholder(
                isFingerDetected = isFingerDetected,
                onSimulateFingerDetected = {
                    isFingerDetected = true
                    if (measurementPhase == MeasurementPhase.WAITING) {
                        measurementPhase = MeasurementPhase.MEASURING
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Finger Detection Indicator ───────────────────────────────
            FingerDetectionIndicator(isDetected = isFingerDetected)

            Spacer(modifier = Modifier.height(20.dp))

            // ── Countdown Timer ──────────────────────────────────────────
            if (measurementPhase != MeasurementPhase.COMPLETED) {
                CountdownTimer(seconds = timeRemainingSeconds)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Live BPM Display ─────────────────────────────────────────
            if (currentBpm > 0 && measurementPhase == MeasurementPhase.MEASURING) {
                LiveBpmDisplay(
                    bpm = currentBpm,
                    pulseScale = pulseScale
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Live Waveform ────────────────────────────────────────────
            if (measurementPhase == MeasurementPhase.MEASURING && isFingerDetected) {
                WaveformVisualization(
                    waveOffset = waveOffset,
                    bpm = currentBpm,
                    signalQuality = signalQuality
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Progress Bar ─────────────────────────────────────────────
            if (measurementPhase == MeasurementPhase.MEASURING) {
                MeasurementProgressBar(progress = progress)
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Results Card ─────────────────────────────────────────────
            AnimatedVisibility(
                visible = measurementPhase == MeasurementPhase.COMPLETED,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut()
            ) {
                ResultsCard(
                    bpm = resultBpm,
                    rmssd = resultRmssd,
                    energyScore = resultEnergyScore,
                    stressScore = resultStressScore
                )
            }

            if (measurementPhase == MeasurementPhase.COMPLETED && !hasSaved) {
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSaveMeasurement(resultBpm, resultRmssd)
                        hasSaved = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EnergyAccent,
                        contentColor = VitaTextOnPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Salvează rezultatele",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (hasSaved) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Rezultatele au fost salvate cu succes!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = VitaGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VitaSurfaceElevated,
                        contentColor = VitaTextPrimary
                    )
                ) {
                    Text(
                        text = "Înapoi la Energie",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Measurement Phase ────────────────────────────────────────────────────────

private enum class MeasurementPhase {
    WAITING,
    MEASURING,
    COMPLETED
}

// ── Camera Preview Placeholder ───────────────────────────────────────────────

@Composable
private fun CameraPreviewPlaceholder(
    isFingerDetected: Boolean,
    onSimulateFingerDetected: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isFingerDetected) {
                        listOf(Color(0xFF3D0E14), Color(0xFF1A0508))
                    } else {
                        listOf(VitaSurfaceVariant, VitaSurface)
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!isFingerDetected) {
                Text(
                    text = "Pune degetul pe cameră și flash",
                    style = MaterialTheme.typography.bodyLarge,
                    color = VitaTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Apasă pe ecran pentru a simula detectarea degetului",
                    style = MaterialTheme.typography.bodySmall,
                    color = VitaTextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            } else {
                Text(
                    text = "Măsurare în curs...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = VitaError.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Ține degetul nemișcat",
                    style = MaterialTheme.typography.bodySmall,
                    color = VitaTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Tap detector for simulation
        if (!isFingerDetected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onSimulateFingerDetected)
            )
        }
    }
}

// ── Finger Detection Indicator ───────────────────────────────────────────────

@Composable
private fun FingerDetectionIndicator(isDetected: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(if (isDetected) VitaError else VitaTextTertiary)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (isDetected) "Deget detectat" else "Așteaptă degetul...",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDetected) VitaError else VitaTextTertiary
        )
    }
}

// ── Countdown Timer ──────────────────────────────────────────────────────────

@Composable
private fun CountdownTimer(seconds: Int) {
    val minutes = seconds / 60
    val secs = seconds % 60
    val timeText = String.format("%d:%02d", minutes, secs)

    Text(
        text = timeText,
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold
        ),
        color = if (seconds <= 10) EnergyAccent else VitaTextPrimary,
        textAlign = TextAlign.Center
    )
}

// ── Live BPM Display ─────────────────────────────────────────────────────────

@Composable
private fun LiveBpmDisplay(
    bpm: Int,
    pulseScale: Float
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            VitaError.copy(alpha = 0.3f),
                            VitaError.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$bpm",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = VitaError
                )
                Text(
                    text = "BPM",
                    style = MaterialTheme.typography.labelSmall,
                    color = VitaTextSecondary
                )
            }
        }
    }
}

// ── Waveform Visualization ───────────────────────────────────────────────────

@Composable
private fun WaveformVisualization(
    waveOffset: Float,
    bpm: Int,
    signalQuality: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Semnal PPG",
                    style = MaterialTheme.typography.labelMedium,
                    color = VitaTextSecondary
                )
                Text(
                    text = "Calitate: ${(signalQuality * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (signalQuality > 0.6f) VitaGreen else EnergyAccent
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                val width = size.width
                val height = size.height
                val centerY = height / 2f
                val amplitude = height * 0.35f * signalQuality

                // Frequency derived from BPM
                val frequency = if (bpm > 0) bpm / 60f else 1f

                val path = Path()
                var firstPoint = true

                val steps = 200
                for (i in 0..steps) {
                    val x = (i.toFloat() / steps) * width
                    val normalizedX = (i.toFloat() / steps) * 4f * Math.PI.toFloat()

                    // Composite wave: primary cardiac + small respiratory component
                    val primaryWave = sin((normalizedX * frequency + waveOffset).toDouble()).toFloat()
                    val secondaryWave = sin((normalizedX * 0.3f + waveOffset * 0.5f).toDouble()).toFloat() * 0.15f
                    val y = centerY - (primaryWave + secondaryWave) * amplitude

                    if (firstPoint) {
                        path.moveTo(x, y)
                        firstPoint = false
                    } else {
                        path.lineTo(x, y)
                    }
                }

                // Draw the waveform
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            VitaError.copy(alpha = 0.3f),
                            VitaError,
                            VitaError,
                            VitaError.copy(alpha = 0.3f)
                        )
                    ),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )

                // Baseline
                drawLine(
                    color = VitaOutline,
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 0.5f
                )
            }
        }
    }
}

// ── Measurement Progress Bar ─────────────────────────────────────────────────

@Composable
private fun MeasurementProgressBar(progress: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Progres măsurare",
                style = MaterialTheme.typography.labelMedium,
                color = VitaTextSecondary
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = EnergyAccent
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = EnergyAccent,
            trackColor = VitaSurfaceElevated
        )
    }
}

// ── Results Card ─────────────────────────────────────────────────────────────

@Composable
private fun ResultsCard(
    bpm: Int,
    rmssd: Float,
    energyScore: Int,
    stressScore: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rezultate măsurare",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResultMetric(
                    value = "$bpm",
                    label = "BPM",
                    color = VitaError
                )
                ResultMetric(
                    value = String.format("%.1f", rmssd),
                    label = "HRV (RMSSD)",
                    color = EnergyAccent
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResultMetric(
                    value = "$energyScore",
                    label = "Scor Energie",
                    color = when {
                        energyScore >= 70 -> VitaGreen
                        energyScore >= 40 -> EnergyAccent
                        else -> VitaError
                    }
                )
                ResultMetric(
                    value = "$stressScore",
                    label = "Scor Stres",
                    color = when {
                        stressScore <= 30 -> VitaGreen
                        stressScore <= 60 -> EnergyAccent
                        else -> VitaError
                    }
                )
            }
        }
    }
}

@Composable
private fun ResultMetric(
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = VitaTextSecondary
        )
    }
}
