package com.vitanova.app.ui.brain

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
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitanova.app.ui.theme.BrainAccent
import com.vitanova.app.ui.theme.VitaBackground
import com.vitanova.app.ui.theme.VitaError
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaOutline
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaTextOnPrimary
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import com.vitanova.app.ui.theme.VitaWarning

// ── Color helpers ────────────────────────────────────────────────────────────

private val ReactionColor = Color(0xFFFF6B6B)
private val MemoryColor = Color(0xFF4ECDC4)
private val AttentionColor = Color(0xFFFFE66D)
private val LogicColor = Color(0xFF95E1D3)

private fun scoreColor(score: Int): Color = when {
    score >= 80 -> VitaGreen
    score >= 60 -> Color(0xFF7BED9F)
    score >= 40 -> VitaWarning
    score >= 20 -> Color(0xFFFF6348)
    else -> VitaError
}

// ── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun BrainScreen(
    onNavigateToTest: (TestType) -> Unit,
    onStartQuickTest: () -> Unit,
    viewModel: BrainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = VitaBackground) { innerPadding ->
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
                text = "Sanatate Cognitiva",
                style = MaterialTheme.typography.headlineMedium,
                color = VitaTextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Global Cognitive Score gauge
            CognitiveGauge(
                score = uiState.globalScore,
                label = "Scor Cognitiv Global"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4 sub-score cards in 2x2 grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SubScoreCard(
                    modifier = Modifier.weight(1f),
                    title = "Reactie",
                    score = uiState.reactionScore,
                    icon = Icons.Filled.Speed,
                    color = ReactionColor,
                    history = uiState.reactionHistory,
                    onClick = { onNavigateToTest(TestType.REACTION) }
                )
                SubScoreCard(
                    modifier = Modifier.weight(1f),
                    title = "Memorie",
                    score = uiState.memoryScore,
                    icon = Icons.Filled.Memory,
                    color = MemoryColor,
                    history = uiState.memoryHistory,
                    onClick = { onNavigateToTest(TestType.MEMORY) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SubScoreCard(
                    modifier = Modifier.weight(1f),
                    title = "Atentie",
                    score = uiState.attentionScore,
                    icon = Icons.Filled.Visibility,
                    color = AttentionColor,
                    history = uiState.attentionHistory,
                    onClick = { onNavigateToTest(TestType.ATTENTION) }
                )
                SubScoreCard(
                    modifier = Modifier.weight(1f),
                    title = "Logica",
                    score = uiState.logicScore,
                    icon = Icons.Filled.Psychology,
                    color = LogicColor,
                    history = uiState.logicHistory,
                    onClick = { onNavigateToTest(TestType.LOGIC) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick test button
            Button(
                onClick = onStartQuickTest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrainAccent,
                    contentColor = VitaTextOnPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Incalzire cognitiva (2 min)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Last test info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val lastDateText = uiState.lastTestDate?.let { dateStr ->
                        val parts = dateStr.split("-")
                        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else dateStr
                    } ?: "Niciun test efectuat"

                    Text(
                        text = "Ultimul test: $lastDateText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VitaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Recomandare: Testeaza-ti functiile cognitive zilnic pentru a urmari progresul.",
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 30-day trend line chart
            if (uiState.globalHistory.isNotEmpty()) {
                TrendChart(
                    data = uiState.globalHistory,
                    title = "Trend cognitiv - 30 zile"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Cognitive Gauge ──────────────────────────────────────────────────────────

@Composable
private fun CognitiveGauge(score: Int, label: String) {
    val animatedSweep = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedSweep.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    val gaugeColor = scoreColor(score)
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.size(220.dp)) {
        val strokeWidth = 20f
        val padding = strokeWidth + 4f
        val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
        val topLeft = Offset(padding, padding)
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

        // Filled arc
        val fillSweep = (animatedSweep.value / 100f) * totalSweep
        val gradientBrush = Brush.sweepGradient(
            0f to VitaError,
            0.33f to VitaWarning,
            0.66f to VitaGreen,
            1f to VitaGreen
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

        // Score text centered
        val scoreStyle = TextStyle(
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = gaugeColor,
            textAlign = TextAlign.Center
        )
        val scoreLayout = textMeasurer.measure(score.toString(), scoreStyle)
        drawText(
            textLayoutResult = scoreLayout,
            topLeft = Offset(
                (size.width - scoreLayout.size.width) / 2f,
                (size.height - scoreLayout.size.height) / 2f - 16.dp.toPx()
            )
        )

        // Label below score
        val labelStyle = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = VitaTextSecondary,
            textAlign = TextAlign.Center
        )
        val labelLayout = textMeasurer.measure(label, labelStyle)
        drawText(
            textLayoutResult = labelLayout,
            topLeft = Offset(
                (size.width - labelLayout.size.width) / 2f,
                (size.height - labelLayout.size.height) / 2f + 24.dp.toPx()
            )
        )
    }
}

// ── Sub Score Card ───────────────────────────────────────────────────────────

@Composable
private fun SubScoreCard(
    modifier: Modifier = Modifier,
    title: String,
    score: Int,
    icon: ImageVector,
    color: Color,
    history: List<Float>,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = scoreColor(score)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = VitaTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mini sparkline
            if (history.isNotEmpty()) {
                MiniSparkline(
                    data = history,
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Fara date",
                        style = MaterialTheme.typography.labelSmall,
                        color = VitaTextTertiary
                    )
                }
            }
        }
    }
}

// ── Mini Sparkline ───────────────────────────────────────────────────────────

@Composable
private fun MiniSparkline(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val padding = 2f
        val drawWidth = size.width - padding * 2
        val drawHeight = size.height - padding * 2

        val minVal = (data.minOrNull() ?: 0f) * 0.9f
        val maxVal = (data.maxOrNull() ?: 100f) * 1.1f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val points = data.mapIndexed { index, value ->
            val x = if (data.size == 1) drawWidth / 2f + padding
            else padding + (index.toFloat() / (data.size - 1)) * drawWidth
            val y = padding + drawHeight - ((value - minVal) / range) * drawHeight
            Offset(x, y)
        }

        if (points.size >= 2) {
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Last point dot
        points.lastOrNull()?.let { last ->
            drawCircle(color = color, radius = 3f, center = last)
        }
    }
}

// ── 30-Day Trend Chart ───────────────────────────────────────────────────────

@Composable
private fun TrendChart(data: List<Float>, title: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = VitaTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                if (data.isEmpty()) return@Canvas

                val hPad = 16f
                val vPad = 8f
                val drawWidth = size.width - hPad * 2
                val drawHeight = size.height - vPad * 2

                val minVal = 0f
                val maxVal = 100f
                val range = maxVal - minVal

                val points = data.mapIndexed { index, value ->
                    val x = if (data.size == 1) drawWidth / 2f + hPad
                    else hPad + (index.toFloat() / (data.size - 1)) * drawWidth
                    val y = vPad + drawHeight - ((value.coerceIn(minVal, maxVal) - minVal) / range) * drawHeight
                    Offset(x, y)
                }

                // Grid lines at 25, 50, 75
                listOf(25f, 50f, 75f).forEach { level ->
                    val y = vPad + drawHeight - ((level - minVal) / range) * drawHeight
                    drawLine(
                        color = VitaOutline,
                        start = Offset(hPad, y),
                        end = Offset(size.width - hPad, y),
                        strokeWidth = 1f
                    )
                }

                // Fill
                if (points.size >= 2) {
                    val fillPath = Path().apply {
                        moveTo(points.first().x, size.height - vPad)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, size.height - vPad)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                BrainAccent.copy(alpha = 0.25f),
                                BrainAccent.copy(alpha = 0f)
                            )
                        )
                    )

                    // Line
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = linePath,
                        color = BrainAccent,
                        style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // Dots
                points.forEach { point ->
                    drawCircle(color = BrainAccent, radius = 4f, center = point)
                    drawCircle(color = VitaSurfaceCard, radius = 2f, center = point)
                }
            }
        }
    }
}
