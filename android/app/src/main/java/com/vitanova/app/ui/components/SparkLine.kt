package com.vitanova.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A compact sparkline chart that draws connected data points in a Canvas.
 * Includes an optional gradient fill below the line and dots at data points.
 *
 * @param data List of float values to plot.
 * @param color Primary line color.
 * @param height Chart height.
 * @param width Chart width.
 * @param showDots Whether to draw circles at each data point.
 * @param showFill Whether to draw a gradient fill below the line.
 * @param lineWidth Stroke width of the line.
 */
@Composable
fun SparkLine(
    data: List<Float>,
    color: Color,
    height: Dp = 40.dp,
    width: Dp = 80.dp,
    showDots: Boolean = true,
    showFill: Boolean = true,
    lineWidth: Float = 2f
) {
    if (data.isEmpty()) return

    Canvas(modifier = Modifier.size(width, height)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 4f

        val effectiveWidth = canvasWidth - padding * 2
        val effectiveHeight = canvasHeight - padding * 2

        val minVal = data.minOrNull() ?: 0f
        val maxVal = data.maxOrNull() ?: 1f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val points = data.mapIndexed { index, value ->
            val x = if (data.size > 1) {
                padding + (index.toFloat() / (data.size - 1)) * effectiveWidth
            } else {
                canvasWidth / 2f
            }
            val y = padding + effectiveHeight - ((value - minVal) / range) * effectiveHeight
            Offset(x, y)
        }

        // Fill below the line
        if (showFill && points.size >= 2) {
            val fillPath = Path().apply {
                moveTo(points.first().x, canvasHeight)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, canvasHeight)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.3f),
                        color.copy(alpha = 0.0f)
                    )
                )
            )
        }

        // Line
        if (points.size >= 2) {
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = linePath,
                color = color,
                style = Stroke(
                    width = lineWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Dots at data points
        if (showDots) {
            points.forEach { point ->
                drawCircle(
                    color = color,
                    radius = lineWidth * 1.5f,
                    center = point
                )
            }
        }
    }
}
