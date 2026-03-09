package com.vitanova.app.ui.sleep

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitanova.app.ui.theme.SleepAccent
import com.vitanova.app.ui.theme.VitaError
import com.vitanova.app.ui.theme.VitaOutline
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaSurfaceVariant
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import kotlinx.coroutines.delay
import kotlin.math.sin

private val TrackingBackground = Color(0xFF010208)
private val DimSleepAccent = SleepAccent.copy(alpha = 0.7f)

@Composable
fun SleepTrackingScreen(
    viewModel: SleepViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSmartAlarm: () -> Unit
) {
    val context = LocalContext.current
    val isTracking by viewModel.isTracking.collectAsState()
    val trackingStartTime by viewModel.trackingStartTime.collectAsState()
    val smartAlarmEnabled by viewModel.smartAlarmEnabled.collectAsState()
    val alarmTimeStart by viewModel.alarmTimeStart.collectAsState()
    val alarmTimeEnd by viewModel.alarmTimeEnd.collectAsState()

    // Elapsed time counter
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isTracking, trackingStartTime) {
        if (isTracking && trackingStartTime > 0L) {
            while (true) {
                elapsedMillis = System.currentTimeMillis() - trackingStartTime
                delay(1000L)
            }
        } else {
            elapsedMillis = 0L
        }
    }

    // Simulated movement data for sparkline visualization
    val movementValues = remember { mutableStateListOf<Float>() }
    LaunchedEffect(isTracking) {
        if (isTracking) {
            while (true) {
                delay(2000L)
                val time = System.currentTimeMillis() / 1000.0
                val simulated = (sin(time * 0.3) * 0.3f + sin(time * 0.7) * 0.15f + 0.5f).toFloat()
                    .coerceIn(0f, 1f)
                movementValues.add(simulated)
                if (movementValues.size > 120) {
                    movementValues.removeAt(0)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrackingBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 32.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Inapoi",
                    tint = VitaTextSecondary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Monitorizare somn",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextSecondary
            )
            Spacer(modifier = Modifier.weight(1f))
            // Balance spacer
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Status indicator with pulsing dot
        if (isTracking) {
            PulsingStatusIndicator()
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large timer
        ElapsedTimeDisplay(elapsedMillis = elapsedMillis)

        Spacer(modifier = Modifier.height(32.dp))

        // Movement visualization
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Miscare detectata",
                    style = MaterialTheme.typography.labelMedium,
                    color = VitaTextTertiary
                )

                Spacer(modifier = Modifier.height(12.dp))

                MovementSparkline(
                    values = movementValues,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Smart alarm toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Alarm,
                            contentDescription = null,
                            tint = DimSleepAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Alarma inteligenta",
                            style = MaterialTheme.typography.titleSmall,
                            color = VitaTextPrimary
                        )
                    }
                    Switch(
                        checked = smartAlarmEnabled,
                        onCheckedChange = { viewModel.toggleSmartAlarm(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SleepAccent,
                            checkedTrackColor = SleepAccent.copy(alpha = 0.3f),
                            uncheckedThumbColor = VitaTextTertiary,
                            uncheckedTrackColor = VitaSurfaceVariant
                        )
                    )
                }

                if (smartAlarmEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (alarmTimeStart > 0L && alarmTimeEnd > 0L) {
                        Text(
                            text = "Fereastra: ${formatTimeFromMillis(alarmTimeStart)} - ${formatTimeFromMillis(alarmTimeEnd)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DimSleepAccent
                        )
                    } else {
                        Button(
                            onClick = onNavigateToSmartAlarm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleepAccent.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Configureaza alarma",
                                style = MaterialTheme.typography.labelMedium,
                                color = DimSleepAccent
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Start / Stop button
        if (isTracking) {
            Button(
                onClick = {
                    viewModel.stopTracking(context)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitaError
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Opreste monitorizarea",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Button(
                onClick = {
                    viewModel.startTracking(context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleepAccent
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Porneste monitorizarea",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hint text
        Text(
            text = if (isTracking) {
                "Pune telefonul pe perna sau pe noptiera. Ecranul se va opri automat."
            } else {
                "Apasa butonul cand esti gata de somn. Telefonul va monitoriza miscarea."
            },
            style = MaterialTheme.typography.bodySmall,
            color = VitaTextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Pulsing Status Indicator ─────────────────────────────────────────────────

@Composable
private fun PulsingStatusIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    color = SleepAccent.copy(alpha = alpha),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Monitorizare activa...",
            style = MaterialTheme.typography.titleSmall,
            color = DimSleepAccent.copy(alpha = alpha)
        )
    }
}

// ── Elapsed Time Display ─────────────────────────────────────────────────────

@Composable
private fun ElapsedTimeDisplay(elapsedMillis: Long) {
    val totalSeconds = elapsedMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            color = VitaTextPrimary.copy(alpha = 0.9f),
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "timp scurs",
            style = MaterialTheme.typography.bodySmall,
            color = VitaTextTertiary
        )
    }
}

// ── Movement Sparkline ───────────────────────────────────────────────────────

@Composable
private fun MovementSparkline(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val gradientColors = listOf(
        SleepAccent.copy(alpha = 0.3f),
        SleepAccent.copy(alpha = 0.0f)
    )

    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height
        val displayValues = if (values.isEmpty()) listOf(0.5f) else values

        // Draw baseline
        drawLine(
            color = VitaOutline,
            start = Offset(0f, chartHeight * 0.5f),
            end = Offset(chartWidth, chartHeight * 0.5f),
            strokeWidth = 0.5.dp.toPx()
        )

        if (displayValues.size < 2) return@Canvas

        val stepX = chartWidth / (displayValues.size - 1).coerceAtLeast(1)
        val padding = 4.dp.toPx()
        val usableHeight = chartHeight - padding * 2

        // Build the line path
        val linePath = Path()
        val fillPath = Path()

        displayValues.forEachIndexed { index, value ->
            val x = index * stepX
            val y = padding + (1f - value) * usableHeight

            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, chartHeight)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // Close fill path
        fillPath.lineTo((displayValues.size - 1) * stepX, chartHeight)
        fillPath.close()

        // Draw gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(gradientColors)
        )

        // Draw the line
        drawPath(
            path = linePath,
            color = SleepAccent,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw the latest point
        val lastX = (displayValues.size - 1) * stepX
        val lastY = padding + (1f - displayValues.last()) * usableHeight
        drawCircle(
            color = SleepAccent,
            radius = 4.dp.toPx(),
            center = Offset(lastX, lastY)
        )
        drawCircle(
            color = SleepAccent.copy(alpha = 0.3f),
            radius = 8.dp.toPx(),
            center = Offset(lastX, lastY)
        )
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun formatTimeFromMillis(epochMillis: Long): String {
    val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(epochMillis))
}
