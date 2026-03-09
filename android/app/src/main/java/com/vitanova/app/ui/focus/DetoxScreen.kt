package com.vitanova.app.ui.focus

import android.app.TimePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitanova.app.ui.theme.FocusAccent
import com.vitanova.app.ui.theme.VitaBackground
import com.vitanova.app.ui.theme.VitaCyan
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaGreenSurface
import com.vitanova.app.ui.theme.VitaOutline
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaSurfaceElevated
import com.vitanova.app.ui.theme.VitaSurfaceVariant
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import com.vitanova.app.ui.theme.VitaWarning

private data class AnalogActivity(
    val name: String,
    val description: String,
    val icon: ImageVector
)

private val analogActivities = listOf(
    AnalogActivity(
        name = "Citeste o carte",
        description = "30 min de lectura inainte de culcare",
        icon = Icons.Filled.AutoStories
    ),
    AnalogActivity(
        name = "Plimbare in natura",
        description = "Lasa telefonul acasa, mergi 20 min",
        icon = Icons.Filled.Hiking
    ),
    AnalogActivity(
        name = "Gateste ceva nou",
        description = "Incearca o reteta fara a folosi telefonul",
        icon = Icons.Filled.Restaurant
    ),
    AnalogActivity(
        name = "Meditatie",
        description = "10 min de mindfulness fara ecrane",
        icon = Icons.Filled.SelfImprovement
    ),
    AnalogActivity(
        name = "Exercitii fizice",
        description = "Stretching sau yoga fara tutoriale video",
        icon = Icons.Filled.FitnessCenter
    ),
    AnalogActivity(
        name = "Deseneaza sau picteaza",
        description = "Activitate creativa cu hartie si creion",
        icon = Icons.Filled.Palette
    ),
    AnalogActivity(
        name = "Asculta muzica",
        description = "Doar asculta, fara a naviga pe telefon",
        icon = Icons.Filled.MusicNote
    ),
    AnalogActivity(
        name = "Iesi la soare",
        description = "15 min de lumina naturala dimineata",
        icon = Icons.Filled.WbSunny
    )
)

@Composable
fun DetoxScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: FocusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Inapoi",
                        tint = VitaTextPrimary
                    )
                }
                Text(
                    text = "Digital Detox",
                    style = MaterialTheme.typography.titleLarge,
                    color = VitaTextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Micro-detox toggle
        item {
            MicroDetoxCard(
                isEnabled = uiState.isDetoxEnabled,
                hour = uiState.detoxStartHour,
                minute = uiState.detoxStartMinute,
                onToggle = { viewModel.toggleDetox(it) },
                onTimeSelected = { h, m -> viewModel.setDetoxTime(h, m) }
            )
        }

        // Weekend detox planner
        item {
            WeekendDetoxCard(
                goalHours = uiState.weekendDetoxGoalHours,
                onGoalChanged = { viewModel.setWeekendDetoxGoal(it) }
            )
        }

        // Analog activity suggestions
        item {
            Text(
                text = "Activitati offline sugerate",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(analogActivities) { activity ->
            AnalogActivityItem(activity = activity)
        }

        // Detox history
        item {
            DetoxHistoryCard(history = uiState.detoxHistory)
        }

        // DND mode integration info
        item {
            DndInfoCard()
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MicroDetoxCard(
    isEnabled: Boolean,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    val timeText = String.format("%02d:%02d", hour, minute)

    val cardColor by animateColorAsState(
        targetValue = if (isEnabled) VitaGreen.copy(alpha = 0.08f) else VitaSurfaceCard,
        label = "detoxCardColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Bedtime,
                        contentDescription = null,
                        tint = if (isEnabled) VitaGreen else VitaTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Micro-Detox",
                        style = MaterialTheme.typography.titleMedium,
                        color = VitaTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = VitaGreen,
                        uncheckedThumbColor = VitaTextTertiary,
                        uncheckedTrackColor = VitaSurfaceVariant,
                        uncheckedBorderColor = VitaOutline
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Fara ecrane dupa ora:",
                style = MaterialTheme.typography.bodyMedium,
                color = VitaTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isEnabled) VitaGreenSurface else VitaSurfaceVariant)
                    .clickable {
                        TimePickerDialog(
                            context,
                            android.R.style.Theme_DeviceDefault_Dialog,
                            { _, selectedHour, selectedMinute ->
                                onTimeSelected(selectedHour, selectedMinute)
                            },
                            hour,
                            minute,
                            true
                        ).show()
                    }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = if (isEnabled) VitaGreen else VitaTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) VitaGreen else VitaTextPrimary
                    )
                )
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Vei primi un reminder la $timeText sa pui telefonul deoparte.",
                    style = MaterialTheme.typography.bodySmall,
                    color = VitaTextTertiary
                )
            }
        }
    }
}

@Composable
private fun WeekendDetoxCard(
    goalHours: Int,
    onGoalChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = VitaCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Plan Detox Weekend",
                    style = MaterialTheme.typography.titleMedium,
                    color = VitaTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Obiectiv ore offline:",
                style = MaterialTheme.typography.bodyMedium,
                color = VitaTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$goalHours ore",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = VitaCyan
                    ),
                    modifier = Modifier.width(80.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = goalHours.toFloat(),
                    onValueChange = { onGoalChanged(it.toInt()) },
                    valueRange = 1f..12f,
                    steps = 10,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = VitaCyan,
                        activeTrackColor = VitaCyan,
                        inactiveTrackColor = VitaOutline,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Planifica activitati offline pentru weekend si urmareste-ti progresul.",
                style = MaterialTheme.typography.bodySmall,
                color = VitaTextTertiary
            )
        }
    }
}

@Composable
private fun AnalogActivityItem(activity: AnalogActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(FocusAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activity.icon,
                    contentDescription = null,
                    tint = FocusAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = VitaTextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = VitaTextSecondary
                )
            }
        }
    }
}

@Composable
private fun DetoxHistoryCard(history: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = VitaWarning,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Istoric Detox",
                    style = MaterialTheme.typography.titleMedium,
                    color = VitaTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (history.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Forest,
                        contentDescription = null,
                        tint = VitaTextTertiary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Inca nu ai sesiuni de detox.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VitaTextTertiary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Activeaza micro-detox pentru a incepe!",
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                history.forEachIndexed { index, entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (entry.contains("record"))
                                        VitaWarning else VitaGreen
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = entry,
                            style = MaterialTheme.typography.bodyMedium,
                            color = VitaTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DndInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = VitaSurfaceElevated
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.DoNotDisturb,
                contentDescription = null,
                tint = VitaCyan,
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Integrare mod Nu deranja",
                    style = MaterialTheme.typography.titleSmall,
                    color = VitaTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Combina detox-ul digital cu modul Nu deranja (DND) al telefonului. " +
                            "Activeaza DND din setarile rapide pentru a bloca notificarile " +
                            "in timpul sesiunilor de detox.",
                    style = MaterialTheme.typography.bodySmall,
                    color = VitaTextSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Setari > Sunet > Nu deranja",
                    style = MaterialTheme.typography.labelMedium,
                    color = VitaCyan,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
