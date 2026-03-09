package com.vitanova.app.ui.energy

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitanova.app.data.local.entity.HrvReading
import com.vitanova.app.ui.theme.EnergyAccent
import com.vitanova.app.ui.theme.VitaBackground
import com.vitanova.app.ui.theme.VitaError
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaOutline
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaSurfaceElevated
import com.vitanova.app.ui.theme.VitaTextOnPrimary
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import com.vitanova.app.ui.theme.VitaWarning

// ── Color helpers ────────────────────────────────────────────────────────────

private val GaugeRed = Color(0xFFFF4757)
private val GaugeYellow = Color(0xFFFFA502)
private val GaugeGreen = Color(0xFF2ED573)

private val StressGreen = Color(0xFF2ED573)
private val StressYellow = Color(0xFFFFA502)
private val StressRed = Color(0xFFFF4757)

private fun energyColor(score: Int): Color = when {
    score >= 80 -> GaugeGreen
    score >= 60 -> Color(0xFF7BED9F)
    score >= 40 -> GaugeYellow
    score >= 20 -> Color(0xFFFF6348)
    else -> GaugeRed
}

private fun stressColor(score: Int): Color = when {
    score <= 20 -> StressGreen
    score <= 40 -> Color(0xFF7BED9F)
    score <= 60 -> StressYellow
    score <= 80 -> Color(0xFFFF6348)
    else -> StressRed
}

// ── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun EnergyScreen(
    onNavigateToMeasure: () -> Unit,
    viewModel: EnergyViewModel = viewModel()
) {
    val energyScore by viewModel.energyScore.collectAsState()
    val stressScore by viewModel.stressScore.collectAsState()
    val latestHrv by viewModel.latestHrv.collectAsState()
    val last7Days by viewModel.last7DaysHrv.collectAsState()

    Scaffold(
        containerColor = VitaBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToMeasure,
                containerColor = EnergyAccent,
                contentColor = VitaTextOnPrimary,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.MonitorHeart,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = "Măsoară HRV",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Energie & HRV",
                style = MaterialTheme.typography.headlineMedium,
                color = VitaTextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Energy Gauge (large) ─────────────────────────────────────
            RadialGauge(
                score = energyScore,
                label = "Energie",
                size = 220,
                strokeWidth = 20f,
                colorResolver = ::energyColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Stress Gauge (smaller) ───────────────────────────────────
            RadialGauge(
                score = stressScore,
                label = "Stres",
                size = 140,
                strokeWidth = 14f,
                colorResolver = ::stressColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── HRV Trend (7 days) ──────────────────────────────────────
            if (last7Days.isNotEmpty()) {
                HrvTrendCard(readings = last7Days)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Latest Readings ──────────────────────────────────────────
            latestHrv?.let { reading ->
                LatestReadingsCard(reading = reading)
                Spacer(modifier = Modifier.height(16.dp))

                // ── ANS Balance ──────────────────────────────────────────
                AnsBalanceCard(stressScore = stressScore)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Recommendation ───────────────────────────────────────────
            RecommendationCard(energyScore = energyScore)

            Spacer(modifier = Modifier.height(96.dp)) // room for FAB
        }
    }
}

// ── Radial Gauge ─────────────────────────────────────────────────────────────

@Composable
private fun RadialGauge(
    score: Int,
    label: String,
    size: Int,
    strokeWidth: Float,
    colorResolver: (Int) -> Color
) {
    val animatedSweep = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedSweep.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val gaugeColor = colorResolver(score)
    val textMeasurer = rememberTextMeasurer()

    val scoreText = score.toString()
    val labelText = label

    Canvas(
        modifier = Modifier.size(size.dp)
    ) {
        val canvasSize = this.size
        val padding = strokeWidth + 4f
        val arcSize = Size(canvasSize.width - padding * 2, canvasSize.height - padding * 2)
        val topLeft = Offset(padding, padding)

        // Total arc: 270 degrees, from 135 to 405 (i.e. start at bottom-left, sweep clockwise)
        val startAngle = 135f
        val totalSweep = 270f

        // Background track
        drawArc(
            color = VitaOutline,
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Filled arc proportional to score
        val fillSweep = (animatedSweep.value / 100f) * totalSweep

        // Gradient brush from red through yellow to the score color
        val gradientBrush = Brush.sweepGradient(
            0f to GaugeRed,
            0.33f to GaugeYellow,
            0.66f to GaugeGreen,
            1f to GaugeGreen
        )

        drawArc(
            brush = gradientBrush,
            startAngle = startAngle,
            sweepAngle = fillSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Score number centered
        val scoreStyle = TextStyle(
            fontSize = (size / 5).sp,
            fontWeight = FontWeight.Bold,
            color = gaugeColor,
            textAlign = TextAlign.Center
        )
        val scoreLayout = textMeasurer.measure(scoreText, scoreStyle)
        drawText(
            textLayoutResult = scoreLayout,
            topLeft = Offset(
                (canvasSize.width - scoreLayout.size.width) / 2f,
                (canvasSize.height - scoreLayout.size.height) / 2f - (size / 12).dp.toPx()
            )
        )

        // Label below score
        val labelStyle = TextStyle(
            fontSize = (size / 11).sp,
            fontWeight = FontWeight.Medium,
            color = VitaTextSecondary,
            textAlign = TextAlign.Center
        )
        val labelLayout = textMeasurer.measure(labelText, labelStyle)
        drawText(
            textLayoutResult = labelLayout,
            topLeft = Offset(
                (canvasSize.width - labelLayout.size.width) / 2f,
                (canvasSize.height - labelLayout.size.height) / 2f + (size / 8).dp.toPx()
            )
        )
    }
}

// ── HRV Trend Sparkline ─────────────────────────────────────────────────────

@Composable
private fun HrvTrendCard(readings: List<HrvReading>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Trend HRV - 7 zile",
                style = MaterialTheme.typography.titleSmall,
                color = VitaTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            val sortedReadings = readings.sortedBy { it.timestamp }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (sortedReadings.isEmpty()) return@Canvas

                val rmssdValues = sortedReadings.map { it.rmssd }
                val minVal = (rmssdValues.minOrNull() ?: 0f) * 0.8f
                val maxVal = (rmssdValues.maxOrNull() ?: 100f) * 1.2f
                val range = (maxVal - minVal).coerceAtLeast(1f)

                val horizontalPadding = 16f
                val verticalPadding = 8f
                val drawWidth = size.width - horizontalPadding * 2
                val drawHeight = size.height - verticalPadding * 2

                val points = rmssdValues.mapIndexed { index, value ->
                    val x = if (rmssdValues.size == 1) {
                        drawWidth / 2f + horizontalPadding
                    } else {
                        horizontalPadding + (index.toFloat() / (rmssdValues.size - 1)) * drawWidth
                    }
                    val y = verticalPadding + drawHeight - ((value - minVal) / range) * drawHeight
                    Offset(x, y)
                }

                // Draw connecting lines
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = EnergyAccent,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }

                // Draw dots at each data point
                points.forEach { point ->
                    drawCircle(
                        color = EnergyAccent,
                        radius = 6f,
                        center = point
                    )
                    drawCircle(
                        color = VitaSurfaceCard,
                        radius = 3f,
                        center = point
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day labels
            val sortedDates = readings.sortedBy { it.timestamp }.map { it.date }
            val displayDates = sortedDates.map { dateStr ->
                val parts = dateStr.split("-")
                if (parts.size == 3) "${parts[2]}/${parts[1]}" else dateStr
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (displayDates.isNotEmpty()) {
                    Text(
                        text = displayDates.first(),
                        style = MaterialTheme.typography.labelSmall,
                        color = VitaTextTertiary
                    )
                }
                if (displayDates.size > 1) {
                    Text(
                        text = displayDates.last(),
                        style = MaterialTheme.typography.labelSmall,
                        color = VitaTextTertiary
                    )
                }
            }
        }
    }
}

// ── Latest Readings Card ────────────────────────────────────────────────────

@Composable
private fun LatestReadingsCard(reading: HrvReading) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ultimele citiri",
                style = MaterialTheme.typography.titleSmall,
                color = VitaTextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ReadingMetric(
                    value = "${(60_000.0 / (reading.rmssd * 10).coerceAtLeast(1f)).toInt().coerceIn(40, 200)}",
                    unit = "bpm",
                    label = "BPM",
                    color = VitaError
                )
                ReadingMetric(
                    value = String.format("%.1f", reading.rmssd),
                    unit = "ms",
                    label = "RMSSD",
                    color = EnergyAccent
                )
                ReadingMetric(
                    value = String.format("%.1f", reading.sdnn),
                    unit = "ms",
                    label = "SDNN",
                    color = VitaGreen
                )
            }
        }
    }
}

@Composable
private fun ReadingMetric(
    value: String,
    unit: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = VitaTextTertiary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = VitaTextSecondary
        )
    }
}

// ── ANS Balance Card ────────────────────────────────────────────────────────

@Composable
private fun AnsBalanceCard(stressScore: Int) {
    val sympatheticPct = stressScore.coerceIn(0, 100)
    val parasympatheticPct = 100 - sympatheticPct

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Echilibru SNA",
                style = MaterialTheme.typography.titleSmall,
                color = VitaTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Simpatic vs Parasimpatic",
                style = MaterialTheme.typography.bodySmall,
                color = VitaTextTertiary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Labels above bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Simpatic $sympatheticPct%",
                    style = MaterialTheme.typography.labelMedium,
                    color = VitaError
                )
                Text(
                    text = "Parasimpatic $parasympatheticPct%",
                    style = MaterialTheme.typography.labelMedium,
                    color = VitaGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal balance bar
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                val barHeight = size.height
                val totalWidth = size.width
                val cornerRadius = barHeight / 2f

                // Background
                drawRoundRect(
                    color = VitaSurfaceElevated,
                    size = Size(totalWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                )

                // Sympathetic (left, red)
                val sympatheticWidth = (sympatheticPct / 100f) * totalWidth
                if (sympatheticWidth > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFF6348),
                                Color(0xFFFF4757)
                            )
                        ),
                        size = Size(sympatheticWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                    )
                }

                // Parasympathetic (right, green) — drawn from right edge
                val parasympatheticWidth = (parasympatheticPct / 100f) * totalWidth
                if (parasympatheticWidth > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF2ED573),
                                Color(0xFF7BED9F)
                            )
                        ),
                        topLeft = Offset(totalWidth - parasympatheticWidth, 0f),
                        size = Size(parasympatheticWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
                    )
                }

                // Center divider line
                drawLine(
                    color = VitaBackground,
                    start = Offset(totalWidth / 2f, 0f),
                    end = Offset(totalWidth / 2f, barHeight),
                    strokeWidth = 2f
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val balanceText = when {
                parasympatheticPct >= 65 -> "Dominanță parasimpatică — recuperare bună"
                sympatheticPct >= 65 -> "Dominanță simpatică — nivel ridicat de stres"
                else -> "Echilibru autonom bun"
            }

            Text(
                text = balanceText,
                style = MaterialTheme.typography.bodySmall,
                color = VitaTextSecondary
            )
        }
    }
}

// ── Recommendation Card ─────────────────────────────────────────────────────

@Composable
private fun RecommendationCard(energyScore: Int) {
    val (recommendation, recColor, recIcon) = when {
        energyScore > 80 -> Triple(
            "Energie optimală — poți face antrenament intens",
            VitaGreen,
            Icons.Filled.Favorite
        )
        energyScore in 60..80 -> Triple(
            "Energie bună — antrenament moderat recomandat",
            Color(0xFF7BED9F),
            Icons.Filled.Favorite
        )
        energyScore in 40..59 -> Triple(
            "Energie medie — activitate ușoară recomandată",
            VitaWarning,
            Icons.Filled.FavoriteBorder
        )
        else -> Triple(
            "Energie scăzută — odihnă recomandată",
            VitaError,
            Icons.Filled.FavoriteBorder
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = recIcon,
                contentDescription = null,
                tint = recColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Recomandare",
                    style = MaterialTheme.typography.titleSmall,
                    color = VitaTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = recColor
                )
            }
        }
    }
}
