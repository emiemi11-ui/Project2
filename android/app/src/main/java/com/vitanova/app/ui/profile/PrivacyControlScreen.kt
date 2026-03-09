package com.vitanova.app.ui.profile

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitanova.app.ui.theme.*

private val dataTypes = listOf("Vital Signs", "Sleep", "Mood", "Fitness", "Nutrition")
private val specialists = listOf("Physician", "Psychologist", "Trainer", "Commander")

@Composable
fun PrivacyControlScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("vitanova_privacy", Context.MODE_PRIVATE)

    // Initialize matrix state
    val matrix = remember {
        mutableStateMapOf<String, Boolean>().apply {
            dataTypes.forEach { data ->
                specialists.forEach { spec ->
                    val key = "${data}_$spec"
                    put(key, prefs.getBoolean(key, false))
                }
            }
        }
    }

    fun togglePermission(data: String, specialist: String) {
        val key = "${data}_$specialist"
        val newValue = !(matrix[key] ?: false)
        matrix[key] = newValue
        prefs.edit().putBoolean(key, newValue).apply()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = VitaTextPrimary)
            }
            Text(
                "Privacy Control",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = VitaTextPrimary
            )
        }

        Text(
            "Control exactly what data you share and with whom.",
            style = MaterialTheme.typography.bodySmall,
            color = VitaTextTertiary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Matrix
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                // Empty corner
                Box(modifier = Modifier.width(100.dp))
                specialists.forEach { spec ->
                    Box(
                        modifier = Modifier.width(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            spec,
                            style = MaterialTheme.typography.labelSmall,
                            color = VitaGreen,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Data rows
            dataTypes.forEach { data ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(100.dp)) {
                            Text(
                                data,
                                style = MaterialTheme.typography.bodySmall,
                                color = VitaTextPrimary,
                                maxLines = 1
                            )
                        }
                        specialists.forEach { spec ->
                            Box(
                                modifier = Modifier.width(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Switch(
                                    checked = matrix["${data}_$spec"] ?: false,
                                    onCheckedChange = { togglePermission(data, spec) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = VitaGreen,
                                        checkedTrackColor = VitaGreen.copy(alpha = 0.3f),
                                        uncheckedThumbColor = VitaTextTertiary,
                                        uncheckedTrackColor = VitaSurfaceVariant
                                    ),
                                    modifier = Modifier.height(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VitaInfo.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Your data stays on your device",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = VitaInfo
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "These toggles control what data specialists can see when you connect with them. No data is sent automatically — sharing requires your active consent for each session.",
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
