package com.vitanova.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A row of 10 circles numbered 1-10 for mood selection.
 * Colors range from red (1) through yellow (5) to green (10).
 * The selected circle is larger and highlighted with a border effect.
 *
 * @param selectedMood Currently selected mood (1-10), or 0 for none.
 * @param onMoodSelected Callback when a mood circle is tapped.
 */
@Composable
fun MoodSelector(
    selectedMood: Int,
    onMoodSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (mood in 1..10) {
            val isSelected = mood == selectedMood
            val moodColor = getMoodColor(mood)

            val targetSize by animateDpAsState(
                targetValue = if (isSelected) 40.dp else 30.dp,
                animationSpec = tween(200),
                label = "moodSize"
            )

            val targetAlpha by animateColorAsState(
                targetValue = if (isSelected) moodColor else moodColor.copy(alpha = 0.5f),
                animationSpec = tween(200),
                label = "moodColor"
            )

            Box(
                modifier = Modifier
                    .size(targetSize)
                    .clip(CircleShape)
                    .background(targetAlpha)
                    .clickable { onMoodSelected(mood) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$mood",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = if (isSelected) 14.sp else 11.sp
                    )
                )
            }
        }
    }
}

private fun getMoodColor(mood: Int): Color {
    return when (mood) {
        1 -> Color(0xFFFF4757)
        2 -> Color(0xFFFF6348)
        3 -> Color(0xFFFF7F50)
        4 -> Color(0xFFFFA502)
        5 -> Color(0xFFFFBE0B)
        6 -> Color(0xFFECCC68)
        7 -> Color(0xFFA8E06C)
        8 -> Color(0xFF7BED9F)
        9 -> Color(0xFF2ED573)
        10 -> Color(0xFF00E5A0)
        else -> Color.Gray
    }
}
