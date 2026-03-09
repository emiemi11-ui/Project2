package com.vitanova.app.ui.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vitanova.app.ui.theme.SleepAccent
import com.vitanova.app.ui.theme.VitaBackground
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaOutline
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaSurfaceElevated
import com.vitanova.app.ui.theme.VitaSurfaceVariant
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import java.util.Calendar

private val smartWindowOptions = listOf(15, 20, 30)
private val alarmSounds = listOf(
    "Rasarit lin",
    "Padure dimineata",
    "Ploaie usoara",
    "Clopote de vant",
    "Melodie clasica",
    "Vibratii progresive"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAlarmScreen(
    viewModel: SleepViewModel,
    onNavigateBack: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = 7,
        initialMinute = 0,
        is24Hour = true
    )

    var selectedWindowMinutes by remember { mutableIntStateOf(20) }
    var selectedSoundIndex by remember { mutableIntStateOf(0) }
    var progressiveVibration by remember { mutableStateOf(true) }
    var soundDropdownExpanded by remember { mutableStateOf(false) }

    val alarmStartFormatted by remember {
        derivedStateOf {
            val targetMinutes = timePickerState.hour * 60 + timePickerState.minute
            val windowStart = targetMinutes - selectedWindowMinutes
            val startHour = ((windowStart / 60) + 24) % 24
            val startMin = ((windowStart % 60) + 60) % 60
            String.format("%02d:%02d", startHour, startMin)
        }
    }

    val alarmEndFormatted by remember {
        derivedStateOf {
            String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground)
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
                text = "Alarma inteligenta",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Header icon
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Alarm,
                contentDescription = null,
                tint = SleepAccent,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Alege ora la care vrei sa te trezesti",
            style = MaterialTheme.typography.bodyMedium,
            color = VitaTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Time Picker
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ora trezire",
                    style = MaterialTheme.typography.titleSmall,
                    color = VitaTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = VitaSurfaceVariant,
                        clockDialSelectedContentColor = VitaBackground,
                        clockDialUnselectedContentColor = VitaTextSecondary,
                        selectorColor = SleepAccent,
                        containerColor = VitaSurfaceCard,
                        periodSelectorBorderColor = VitaOutline,
                        periodSelectorSelectedContainerColor = SleepAccent,
                        periodSelectorUnselectedContainerColor = VitaSurfaceVariant,
                        periodSelectorSelectedContentColor = VitaBackground,
                        periodSelectorUnselectedContentColor = VitaTextSecondary,
                        timeSelectorSelectedContainerColor = SleepAccent.copy(alpha = 0.2f),
                        timeSelectorUnselectedContainerColor = VitaSurfaceVariant,
                        timeSelectorSelectedContentColor = SleepAccent,
                        timeSelectorUnselectedContentColor = VitaTextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Smart Window Selector
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
                    text = "Fereastra inteligenta",
                    style = MaterialTheme.typography.titleSmall,
                    color = VitaTextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Alarma va suna in aceasta fereastra cand detecteaza somn usor",
                    style = MaterialTheme.typography.bodySmall,
                    color = VitaTextTertiary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    smartWindowOptions.forEach { minutes ->
                        val isSelected = selectedWindowMinutes == minutes
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) SleepAccent.copy(alpha = 0.2f)
                                    else VitaSurfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) SleepAccent else VitaSurfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedWindowMinutes = minutes },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${minutes} min",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) SleepAccent else VitaTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sound selector
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = SleepAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sunet alarma",
                        style = MaterialTheme.typography.titleSmall,
                        color = VitaTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(VitaSurfaceVariant)
                            .clickable { soundDropdownExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = alarmSounds[selectedSoundIndex],
                            style = MaterialTheme.typography.bodyLarge,
                            color = VitaTextPrimary
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = VitaTextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = soundDropdownExpanded,
                        onDismissRequest = { soundDropdownExpanded = false },
                        modifier = Modifier.background(VitaSurfaceElevated)
                    ) {
                        alarmSounds.forEachIndexed { index, sound ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = sound,
                                        color = if (index == selectedSoundIndex) SleepAccent
                                        else VitaTextPrimary
                                    )
                                },
                                onClick = {
                                    selectedSoundIndex = index
                                    soundDropdownExpanded = false
                                },
                                trailingIcon = {
                                    if (index == selectedSoundIndex) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = SleepAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Progressive vibration toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Vibration,
                        contentDescription = null,
                        tint = SleepAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Vibratie progresiva",
                            style = MaterialTheme.typography.titleSmall,
                            color = VitaTextPrimary
                        )
                        Text(
                            text = "Creste treptat intensitatea vibratiei",
                            style = MaterialTheme.typography.bodySmall,
                            color = VitaTextTertiary
                        )
                    }
                }
                Switch(
                    checked = progressiveVibration,
                    onCheckedChange = { progressiveVibration = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SleepAccent,
                        checkedTrackColor = SleepAccent.copy(alpha = 0.3f),
                        uncheckedThumbColor = VitaTextTertiary,
                        uncheckedTrackColor = VitaSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Preview card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = SleepAccent.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Alarm,
                    contentDescription = null,
                    tint = SleepAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Alarma va suna intre $alarmStartFormatted si $alarmEndFormatted, cand detectam somn usor.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SleepAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Save button
        Button(
            onClick = {
                val calendar = Calendar.getInstance()
                val now = System.currentTimeMillis()

                // Calculate target wake time in epoch millis for today/tomorrow
                calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                calendar.set(Calendar.MINUTE, timePickerState.minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // If the time is already past, set it for tomorrow
                if (calendar.timeInMillis <= now) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                val alarmEnd = calendar.timeInMillis
                val alarmStart = alarmEnd - selectedWindowMinutes * 60 * 1000L

                viewModel.setSmartAlarm(alarmStart, alarmEnd)
                onNavigateBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SleepAccent
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Salveaza alarma",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
