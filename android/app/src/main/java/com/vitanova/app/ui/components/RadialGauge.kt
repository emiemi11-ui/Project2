package com.vitanova.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitanova.app.ui.theme.VitaError
import com.vitanova.app.ui.theme.VitaSuccess
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaWarning

/**
 * A reusable radial gauge composable that draws an arc from 135 degrees to 405 degrees
 * (270 degree sweep). The foreground arc is colored by score using a red-yellow-green gradient.
 *
 * @param score Current score value.
 * @param maxScore Maximum possible score.
 * @param size Diameter of the gauge.
 * @param label Text displayed below the score number.
 * @param trackColor Background arc color.
 * @param strokeWidth Width of the arc stroke.
 */
@Composable
fun RadialGauge(
    score: Int,
    maxScore: Int = 100,
    size: Dp = 180.dp,
    label: String = "",
    trackColor: Color = Color(0xFF1E2A42),
    strokeWidth: Dp = 12.dp
) {
    val fraction = (score.toFloat() / maxScore.toFloat()).coerceIn(0f, 1f)
    val animatedFraction = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedFraction.animateTo(
            targetValue = fraction,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    val scoreColor = when {
        fraction < 0.33f -> VitaError
        fraction < 0.66f -> VitaWarning
        else -> VitaSuccess
    }

    val gradientColors = listOf(VitaError, VitaWarning, VitaSuccess)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(
                width = this.size.width - strokePx,
                height = this.size.height - strokePx
            )
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background arc
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Foreground arc with gradient
            val sweepAngle = 270f * animatedFraction.value
            drawArc(
                brush = Brush.sweepGradient(
                    colors = gradientColors,
                    center = Offset(this.size.width / 2f, this.size.height / 2f)
                ),
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = scoreColor,
                    fontSize = (size.value * 0.22f).sp
                )
            )
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = VitaTextSecondary
                    )
                )
            }
        }
    }
}
