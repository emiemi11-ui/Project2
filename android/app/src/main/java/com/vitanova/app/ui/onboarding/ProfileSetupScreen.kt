package com.vitanova.app.ui.onboarding

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vitanova.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onNavigateToFirstDay: () -> Unit = {}
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(1) }

    // Step 1
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    // Step 2
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("Moderate") }

    // Step 3
    val goals = listOf("Better Sleep", "Less Stress", "Fitness", "Nutrition", "Focus", "Brain Training")
    var selectedGoals by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Step indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            (1..3).forEach { s ->
                Box(
                    modifier = Modifier
                        .size(if (s == step) 32.dp else 24.dp, 8.dp)
                        .padding(horizontal = 2.dp)
                        .background(
                            if (s <= step) VitaGreen else VitaTextTertiary.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Step $step of 3",
            style = MaterialTheme.typography.labelMedium,
            color = VitaTextTertiary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        when (step) {
            1 -> {
                Text("About You", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = VitaTextPrimary)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VitaGreen,
                        unfocusedBorderColor = VitaOutline,
                        focusedTextColor = VitaTextPrimary,
                        unfocusedTextColor = VitaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = age, onValueChange = { age = it.filter { c -> c.isDigit() } },
                    label = { Text("Age") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VitaGreen,
                        unfocusedBorderColor = VitaOutline,
                        focusedTextColor = VitaTextPrimary,
                        unfocusedTextColor = VitaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Gender", style = MaterialTheme.typography.labelMedium, color = VitaTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Male", "Female", "Other").forEach { g ->
                        FilterChip(
                            selected = gender == g,
                            onClick = { gender = g },
                            label = { Text(g) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VitaGreen.copy(alpha = 0.2f),
                                selectedLabelColor = VitaGreen
                            )
                        )
                    }
                }
            }
            2 -> {
                Text("Body Metrics", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = VitaTextPrimary)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VitaGreen,
                        unfocusedBorderColor = VitaOutline,
                        focusedTextColor = VitaTextPrimary,
                        unfocusedTextColor = VitaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = height, onValueChange = { height = it.filter { c -> c.isDigit() } },
                    label = { Text("Height (cm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VitaGreen,
                        unfocusedBorderColor = VitaOutline,
                        focusedTextColor = VitaTextPrimary,
                        unfocusedTextColor = VitaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Activity Level", style = MaterialTheme.typography.labelMedium, color = VitaTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Moderate", "High", "Very High").forEach { level ->
                        FilterChip(
                            selected = activityLevel == level,
                            onClick = { activityLevel = level },
                            label = { Text(level) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VitaGreen.copy(alpha = 0.2f),
                                selectedLabelColor = VitaGreen
                            )
                        )
                    }
                }
            }
            3 -> {
                Text("Your Goals", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = VitaTextPrimary)
                Text("Select what matters most to you", style = MaterialTheme.typography.bodyMedium, color = VitaTextTertiary)
                Spacer(modifier = Modifier.height(24.dp))
                goals.forEach { goal ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedGoals.contains(goal))
                                VitaGreen.copy(alpha = 0.12f) else VitaSurfaceCard
                        ),
                        onClick = {
                            selectedGoals = if (selectedGoals.contains(goal))
                                selectedGoals - goal else selectedGoals + goal
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedGoals.contains(goal),
                                onCheckedChange = {
                                    selectedGoals = if (it) selectedGoals + goal else selectedGoals - goal
                                },
                                colors = CheckboxDefaults.colors(checkedColor = VitaGreen)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(goal, color = VitaTextPrimary, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step > 1) {
                OutlinedButton(
                    onClick = { step-- },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Back", color = VitaTextSecondary)
                }
            }
            Button(
                onClick = {
                    if (step < 3) {
                        step++
                    } else {
                        // Save to prefs
                        val prefs = context.getSharedPreferences("vitanova_prefs", Context.MODE_PRIVATE)
                        prefs.edit()
                            .putString("user_name", name.ifBlank { "User" })
                            .putInt("user_age", age.toIntOrNull() ?: 0)
                            .putString("user_gender", gender)
                            .putString("user_weight", weight)
                            .putString("user_height", height)
                            .putString("user_activity", activityLevel)
                            .putStringSet("user_goals", selectedGoals)
                            .apply()
                        onNavigateToFirstDay()
                    }
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VitaGreen)
            ) {
                Text(
                    if (step < 3) "Continue" else "Finish",
                    fontWeight = FontWeight.Bold,
                    color = VitaBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
