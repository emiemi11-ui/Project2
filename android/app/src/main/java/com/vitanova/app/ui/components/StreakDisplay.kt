package com.vitanova.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vitanova.app.ui.theme.VitaSuccess
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaWarning

/**
 * Displays a 30-day streak grid in rows of 7 small colored squares.
 *
 * @param completions List of 30 nullable booleans: true=done, false=missed, null=future/elastic miss.
 * @param streakCount Current elastic streak count to display.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StreakDisplay(
    completions: List<Boolean?>,
    streakCount: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            maxItemsInEachRow = 7
        ) {
            val displayList = if (completions.size < 30) {
                completions + List(30 - completions.size) { null }
            } else {
                completions.take(30)
            }

            displayList.forEach { status ->
                val color = when (status) {
                    true -> VitaSuccess
                    false -> Color(0xFFFF4757)
                    null -> Color(0xFF2A2A3E)
                }
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
            }
        }

        Text(
            text = "$streakCount zile streak",
            style = MaterialTheme.typography.labelMedium.copy(
                color = VitaTextSecondary,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
