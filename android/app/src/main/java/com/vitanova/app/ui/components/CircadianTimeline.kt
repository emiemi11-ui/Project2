package com.vitanova.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.vitanova.app.ui.theme.VitaTextTertiary

private val NightColor = Color(0xFF312E81)
private val MorningColor = Color(0xFFFFA502)
private val MiddayColor = Color(0xFFD1D5DB)
private val AfternoonColor = Color(0xFF0D9488)
private val EveningColor = Color(0xFF4338CA)

/**
 * A horizontal 24-hour circadian timeline bar.
 *
 * Colored zones represent different circadian phases:
 *   - Night (00:00-04:59): indigo
 *   - Morning (05:00-11:59): amber
 *   - Midday (12:00-13:59): light gray
 *   - Afternoon (14:00-17:59): teal
 *   - Evening (18:00-21:59): indigo/purple
 *   - Night (22:00-23:59): indigo
 *
 * A triangle marker indicates the current hour position.
 *
 * @param currentHour Current hour of the day (0-23).
 */
@Composable
fun CircadianTimeline(currentHour: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                val barHeight = 20.dp.toPx()
                val barY = 16.dp.toPx()
                val totalWidth = size.width

                // Each hour occupies 1/24 of the total width
                val hourWidth = totalWidth / 24f

                // Phase definitions: startHour, endHour (exclusive), color
                val phases = listOf(
                    Triple(0, 5, NightColor),
                    Triple(5, 12, MorningColor),
                    Triple(12, 14, MiddayColor),
                    Triple(14, 18, AfternoonColor),
                    Triple(18, 22, EveningColor),
                    Triple(22, 24, NightColor)
                )

                // Draw colored segments
                phases.forEach { (start, end, color) ->
                    val x = start * hourWidth
                    val w = (end - start) * hourWidth
                    val cornerRadius = CornerRadius(4.dp.toPx())

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, barY),
                        size = Size(w, barHeight),
                        cornerRadius = when {
                            start == 0 -> cornerRadius
                            end == 24 -> cornerRadius
                            else -> CornerRadius.Zero
                        }
                    )
                    // Overdraw with rect to flatten inner corners where segments meet
                    if (start > 0) {
                        drawRect(
                            color = color,
                            topLeft = Offset(x, barY),
                            size = Size(4.dp.toPx(), barHeight)
                        )
                    }
                    if (end < 24) {
                        drawRect(
                            color = color,
                            topLeft = Offset(x + w - 4.dp.toPx(), barY),
                            size = Size(4.dp.toPx(), barHeight)
                        )
                    }
                }

                // Current time triangle marker
                val markerX = (currentHour.coerceIn(0, 23) * hourWidth) + hourWidth / 2f
                val triangleSize = 8.dp.toPx()
                val trianglePath = Path().apply {
                    moveTo(markerX, barY - 2.dp.toPx())
                    lineTo(markerX - triangleSize / 2f, barY - 2.dp.toPx() - triangleSize)
                    lineTo(markerX + triangleSize / 2f, barY - 2.dp.toPx() - triangleSize)
                    close()
                }
                drawPath(
                    path = trianglePath,
                    color = Color.White
                )

                // Small dot on the bar at current position
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(markerX, barY + barHeight / 2f)
                )
            }
        }

        // Hour labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        ) {
            val labels = listOf(
                0 to "0",
                6 to "6",
                12 to "12",
                18 to "18",
                23 to "24"
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                labels.forEach { (hour, label) ->
                    val fraction = hour / 24f
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = VitaTextTertiary,
                        modifier = Modifier.padding(
                            start = (fraction * 100).dp.coerceAtMost(280.dp)
                        )
                    )
                }
            }
        }
    }
}
