package com.vitanova.app.ui.sleep

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitanova.app.data.local.entity.SleepSample
import com.vitanova.app.data.local.entity.SleepSession
import com.vitanova.app.ui.theme.SleepAccent
import com.vitanova.app.ui.theme.VitaBackground
import com.vitanova.app.ui.theme.VitaCyan
import com.vitanova.app.ui.theme.VitaError
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaOutline
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaSurfaceElevated
import com.vitanova.app.ui.theme.VitaSurfaceVariant
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import com.vitanova.app.ui.theme.VitaWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Sleep phase colors
private val DeepSleepColor = Color(0xFF4338CA)    // Indigo
private val LightSleepColor = Color(0xFF06B6D4)   // Cyan
private val RemSleepColor = Color(0xFF9333EA)      // Purple
private val AwakeColor = Color(0xFFEF4444)         // Red

@Composable
fun SleepScreen(
    viewModel: SleepViewModel,
    onNavigateToTracking: () -> Unit,
    onNavigateToSmartAlarm: () -> Unit
) {
    val latestSession by viewModel.latestSession.collectAsState()
    val last7Sessions by viewModel.last7Sessions.collectAsState()
    val sleepSamples by viewModel.sleepSamples.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Somn",
                    style = MaterialTheme.typography.headlineLarge,
                    color = VitaTextPrimary
                )
                Text(
                    text = formatDateHeader(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = VitaTextSecondary
                )
            }
            Icon(
                imageVector = Icons.Filled.Nightlight,
                contentDescription = null,
                tint = SleepAccent,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (latestSession == null || latestSession?.sleepScore == 0) {
            // Empty state
            EmptyStateCard(
                isTracking = isTracking,
                onStartTracking = onNavigateToTracking
            )
        } else {
            val session = latestSession!!

            // Sleep Score Gauge
            SleepScoreGauge(score = session.sleepScore)

            Spacer(modifier = Modifier.height(20.dp))

            // Stat cards
            SleepStatCards(session = session)

            Spacer(modifier = Modifier.height(20.dp))

            // Hypnogram
            if (sleepSamples.isNotEmpty()) {
                HypnogramCard(
                    samples = sleepSamples,
                    startTime = session.startTime,
                    endTime = session.endTime
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Last 7 nights trend
            if (last7Sessions.isNotEmpty()) {
                Last7NightsTrend(sessions = last7Sessions)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Action buttons
        ActionButtons(
            isTracking = isTracking,
            onNavigateToTracking = onNavigateToTracking,
            onNavigateToSmartAlarm = onNavigateToSmartAlarm
        )
    }
}

// ── Sleep Score Gauge ────────────────────────────────────────────────────────

@Composable
private fun SleepScoreGauge(score: Int) {
    val scoreColor = when {
        score > 80 -> VitaGreen
        score in 60..80 -> VitaWarning
        else -> VitaError
    }

    var animationTarget by remember { mutableFloatStateOf(0f) }
    val animatedSweep by animateFloatAsState(
        targetValue = animationTarget,
        animationSpec = tween(durationMillis = 1200),
        label = "scoreSweep"
    )

    LaunchedEffect(score) {
        animationTarget = (score / 100f) * 270f
    }

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
                text = "Scor somn",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    // Background track
                    drawArc(
                        color = VitaSurfaceVariant,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Score arc
                    drawArc(
                        color = scoreColor,
                        startAngle = 135f,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                    Text(
                        text = "din 100",
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scoreLabel(score),
                style = MaterialTheme.typography.titleSmall,
                color = scoreColor
            )
        }
    }
}

private fun scoreLabel(score: Int): String = when {
    score >= 90 -> "Excelent"
    score >= 80 -> "Foarte bine"
    score >= 70 -> "Bine"
    score >= 60 -> "Acceptabil"
    score >= 40 -> "Slab"
    else -> "Foarte slab"
}

// ── Stat Cards ───────────────────────────────────────────────────────────────

@Composable
private fun SleepStatCards(session: SleepSession) {
    val hours = session.totalDurationMinutes / 60
    val minutes = session.totalDurationMinutes % 60

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            label = "Durată",
            value = "${hours}h ${minutes}m",
            color = SleepAccent,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Eficiență",
            value = "${session.efficiencyPercent.toInt()}%",
            color = VitaGreen,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            label = "Profund",
            value = "${session.deepMinutes}m",
            color = DeepSleepColor,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Ușor",
            value = "${session.lightMinutes}m",
            color = LightSleepColor,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            label = "REM",
            value = "${session.remMinutes}m",
            color = RemSleepColor,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Treaz",
            value = "${session.awakeMinutes}m",
            color = AwakeColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = VitaTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = VitaTextPrimary
            )
        }
    }
}

// ── Hypnogram ────────────────────────────────────────────────────────────────

@Composable
private fun HypnogramCard(
    samples: List<SleepSample>,
    startTime: Long,
    endTime: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Hipnogramă",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${formatTime(startTime)} - ${formatTime(endTime)}",
                style = MaterialTheme.typography.bodySmall,
                color = VitaTextTertiary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phase labels on the left
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.width(52.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    PhaseLabel("Treaz", AwakeColor)
                    Spacer(modifier = Modifier.height(20.dp))
                    PhaseLabel("REM", RemSleepColor)
                    Spacer(modifier = Modifier.height(20.dp))
                    PhaseLabel("Ușor", LightSleepColor)
                    Spacer(modifier = Modifier.height(20.dp))
                    PhaseLabel("Profund", DeepSleepColor)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // The hypnogram chart
                HypnogramChart(
                    samples = samples,
                    startTime = startTime,
                    endTime = endTime,
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time axis labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 60.dp)
            ) {
                val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                Text(
                    text = timeFormatter.format(Date(startTime)),
                    style = MaterialTheme.typography.labelSmall,
                    color = VitaTextTertiary
                )
                Spacer(modifier = Modifier.weight(1f))
                val midTime = startTime + (endTime - startTime) / 2
                Text(
                    text = timeFormatter.format(Date(midTime)),
                    style = MaterialTheme.typography.labelSmall,
                    color = VitaTextTertiary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = timeFormatter.format(Date(endTime)),
                    style = MaterialTheme.typography.labelSmall,
                    color = VitaTextTertiary
                )
            }
        }
    }
}

@Composable
private fun PhaseLabel(label: String, color: Color) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontSize = 9.sp
    )
}

@Composable
private fun HypnogramChart(
    samples: List<SleepSample>,
    startTime: Long,
    endTime: Long,
    modifier: Modifier = Modifier
) {
    val sortedSamples = remember(samples) { samples.sortedBy { it.timestamp } }
    val totalDuration = (endTime - startTime).coerceAtLeast(1L).toFloat()

    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height

        // Draw horizontal grid lines (one per phase level)
        val phaseYPositions = mapOf(
            "awake" to 0f,
            "rem" to chartHeight * 0.30f,
            "light" to chartHeight * 0.60f,
            "deep" to chartHeight * 0.90f
        )

        // Draw grid lines
        phaseYPositions.values.forEach { y ->
            drawLine(
                color = VitaOutline,
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 0.5.dp.toPx()
            )
        }

        // Draw colored rectangles for each sample
        if (sortedSamples.isNotEmpty()) {
            for (i in sortedSamples.indices) {
                val sample = sortedSamples[i]
                val x = ((sample.timestamp - startTime) / totalDuration) * chartWidth

                val nextTimestamp = if (i < sortedSamples.lastIndex) {
                    sortedSamples[i + 1].timestamp
                } else {
                    endTime
                }
                val segmentWidth = ((nextTimestamp - sample.timestamp) / totalDuration) * chartWidth

                val color = when (sample.stage) {
                    "deep" -> DeepSleepColor
                    "light" -> LightSleepColor
                    "rem" -> RemSleepColor
                    "awake" -> AwakeColor
                    else -> LightSleepColor
                }

                val y = phaseYPositions[sample.stage] ?: (chartHeight * 0.60f)
                val rectHeight = chartHeight * 0.18f

                drawRect(
                    color = color,
                    topLeft = Offset(x, y - rectHeight / 2f),
                    size = Size(segmentWidth.coerceAtLeast(1f), rectHeight),
                    alpha = 0.85f
                )
            }

            // Draw connecting lines between phases
            for (i in 0 until sortedSamples.lastIndex) {
                val current = sortedSamples[i]
                val next = sortedSamples[i + 1]

                val x1 = ((current.timestamp - startTime) / totalDuration) * chartWidth
                val x2 = ((next.timestamp - startTime) / totalDuration) * chartWidth
                val y1 = phaseYPositions[current.stage] ?: (chartHeight * 0.60f)
                val y2 = phaseYPositions[next.stage] ?: (chartHeight * 0.60f)

                if (current.stage != next.stage) {
                    drawLine(
                        color = VitaTextTertiary.copy(alpha = 0.4f),
                        start = Offset(x1 + ((next.timestamp - current.timestamp) / totalDuration) * chartWidth, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }
    }
}

// ── Last 7 Nights Trend ──────────────────────────────────────────────────────

@Composable
private fun Last7NightsTrend(sessions: List<SleepSession>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Ultimele 7 nopți",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            val avgScore = if (sessions.isNotEmpty()) {
                sessions.map { it.sleepScore }.average().toInt()
            } else 0
            Text(
                text = "Medie: $avgScore",
                style = MaterialTheme.typography.bodySmall,
                color = VitaTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            ScoreBarChart(
                sessions = sessions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
        }
    }
}

@Composable
private fun ScoreBarChart(
    sessions: List<SleepSession>,
    modifier: Modifier = Modifier
) {
    val reversed = remember(sessions) { sessions.reversed() }
    val dateFormatter = remember { SimpleDateFormat("EEE", Locale.getDefault()) }

    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height
        val barCount = reversed.size.coerceAtMost(7)
        if (barCount == 0) return@Canvas

        val barSpacing = 12.dp.toPx()
        val totalSpacing = barSpacing * (barCount + 1)
        val barWidth = ((chartWidth - totalSpacing) / barCount).coerceAtLeast(16.dp.toPx())
        val maxBarHeight = chartHeight - 24.dp.toPx()

        for (i in 0 until barCount) {
            val session = reversed[i]
            val score = session.sleepScore
            val barHeight = (score / 100f) * maxBarHeight

            val color = when {
                score > 80 -> VitaGreen
                score in 60..80 -> VitaWarning
                else -> VitaError
            }

            val x = barSpacing + i * (barWidth + barSpacing)
            val y = chartHeight - 20.dp.toPx() - barHeight

            // Bar background
            drawRoundRect(
                color = VitaSurfaceVariant,
                topLeft = Offset(x, chartHeight - 20.dp.toPx() - maxBarHeight),
                size = Size(barWidth, maxBarHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )

            // Score bar
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight.coerceAtLeast(2.dp.toPx())),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )
        }
    }

    // Day labels below
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        reversed.take(7).forEach { session ->
            Text(
                text = dateFormatter.format(Date(session.startTime)),
                style = MaterialTheme.typography.labelSmall,
                color = VitaTextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp)
            )
        }
    }
}

// ── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyStateCard(
    isTracking: Boolean,
    onStartTracking: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Bedtime,
                contentDescription = null,
                tint = SleepAccent.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nu ai inregistrari de somn",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Porneste tracking diseara pentru a-ti analiza somnul.",
                style = MaterialTheme.typography.bodyMedium,
                color = VitaTextSecondary,
                textAlign = TextAlign.Center
            )

            if (!isTracking) {
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onStartTracking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleepAccent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Incepe monitorizarea",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

// ── Action Buttons ───────────────────────────────────────────────────────────

@Composable
private fun ActionButtons(
    isTracking: Boolean,
    onNavigateToTracking: () -> Unit,
    onNavigateToSmartAlarm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onNavigateToTracking,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isTracking) VitaWarning else SleepAccent
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = if (isTracking) Icons.Filled.Bedtime else Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isTracking) "Tracking activ" else "Porneste tracking",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Button(
            onClick = onNavigateToSmartAlarm,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = VitaSurfaceElevated
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Alarm,
                contentDescription = null,
                tint = SleepAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Alarma smart",
                style = MaterialTheme.typography.labelLarge,
                color = SleepAccent
            )
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun formatDateHeader(): String {
    val formatter = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
    return formatter.format(Date())
}

private fun formatTime(epochMillis: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(epochMillis))
}
