package com.vitanova.app.ui.habits

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitanova.app.VitaNovaApp
import com.vitanova.app.data.local.dao.HabitWithStatus
import com.vitanova.app.data.local.entity.Habit
import com.vitanova.app.data.local.entity.HabitCompletion
import com.vitanova.app.data.repository.HabitRepository
import com.vitanova.app.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = VitaNovaApp.getInstance().database
    private val repository = HabitRepository(db.habitDao())
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val today = LocalDate.now().format(dateFormatter)

    val habits: StateFlow<List<HabitWithStatus>> = db.habitDao()
        .getAllHabitsWithTodayStatus(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleCompletion(habitId: Long) {
        viewModelScope.launch {
            repository.toggleCompletion(habitId, today)
        }
    }

    fun addHabit(name: String) {
        viewModelScope.launch {
            val habit = Habit(
                name = name,
                description = null,
                icon = null,
                color = "#00E5A0",
                frequency = "daily",
                targetDaysPerWeek = 7,
                currentStreak = 0,
                bestStreak = 0,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
            repository.saveHabit(habit)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel = viewModel()
) {
    val habits by viewModel.habits.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }

    val suggestedHabits = listOf("Drink 2L water", "Meditate 10 min", "Walk 30 min", "Read 20 pages", "Stretch 5 min", "Journal")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Habits",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = VitaTextPrimary
            )
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add habit", tint = VitaGreen)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Momentum Score
        val completedToday = habits.count { it.completedToday }
        val total = habits.size
        if (total > 0) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Momentum Score", style = MaterialTheme.typography.labelMedium, color = VitaTextTertiary)
                        Text(
                            "$completedToday/$total today",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = VitaGreen
                        )
                    }
                    // Recovery Mode
                    val hasRecovery = habits.any { it.currentStreak == 0 && !it.completedToday }
                    if (hasRecovery) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = VitaWarning.copy(alpha = 0.15f))
                        ) {
                            Text(
                                "Recovery Mode",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = VitaWarning
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (habits.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = VitaTextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No habits yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = VitaTextSecondary
                    )
                    Text(
                        "Add your first habit to start building streaks!",
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextTertiary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Try these:", style = MaterialTheme.typography.labelSmall, color = VitaTextTertiary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(suggestedHabits) { suggestion ->
                            SuggestionChip(
                                onClick = { viewModel.addHabit(suggestion) },
                                label = { Text(suggestion, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(habits, key = { it.id }) { habit ->
                    HabitRow(
                        habit = habit,
                        onToggle = { viewModel.toggleCompletion(habit.id) }
                    )
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; newHabitName = "" },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newHabitName.isNotBlank()) {
                            viewModel.addHabit(newHabitName.trim())
                            newHabitName = ""
                            showAddDialog = false
                        }
                    }
                ) { Text("Add", color = VitaGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; newHabitName = "" }) {
                    Text("Cancel", color = VitaTextTertiary)
                }
            },
            title = { Text("Add Habit", color = VitaTextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        label = { Text("Habit name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Suggestions:", style = MaterialTheme.typography.labelSmall, color = VitaTextTertiary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(suggestedHabits) { suggestion ->
                            SuggestionChip(
                                onClick = { newHabitName = suggestion },
                                label = { Text(suggestion, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            containerColor = VitaSurface
        )
    }
}

@Composable
private fun HabitRow(
    habit: HabitWithStatus,
    onToggle: () -> Unit
) {
    val habitColor = try {
        Color(android.graphics.Color.parseColor(habit.color ?: "#00E5A0"))
    } catch (_: Exception) {
        VitaGreen
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (habit.completedToday) habitColor else Color.Transparent)
                    .then(
                        if (!habit.completedToday)
                            Modifier.border(2.dp, VitaTextTertiary, CircleShape)
                        else
                            Modifier
                    )
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                if (habit.completedToday) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Done",
                        tint = VitaBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (habit.completedToday) VitaTextTertiary else VitaTextPrimary
                )
                if (habit.currentStreak > 0) {
                    Text(
                        text = "${habit.currentStreak} day streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = habitColor
                    )
                }
            }
            // Streak indicator
            Text(
                text = "🔥${habit.currentStreak}",
                style = MaterialTheme.typography.labelMedium,
                color = if (habit.currentStreak > 0) habitColor else VitaTextTertiary
            )
        }
    }
}
