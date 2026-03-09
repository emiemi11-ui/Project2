package com.vitanova.app.ui.profile

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vitanova.app.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateToPrivacyControl: () -> Unit = {},
    onNavigateToWelcome: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("vitanova_prefs", Context.MODE_PRIVATE)
    val userName = prefs.getString("user_name", "User") ?: "User"
    val userAge = prefs.getInt("user_age", 0)
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Avatar & Name
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(VitaGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = VitaGreen
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = VitaTextPrimary
                )
                if (userAge > 0) {
                    Text(
                        text = "$userAge years old",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VitaTextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Settings Section
        SectionHeader("Settings")
        SettingsItem(
            icon = Icons.Filled.Straighten,
            title = "Units",
            subtitle = "Metric",
            onClick = {}
        )
        SettingsItem(
            icon = Icons.Filled.Notifications,
            title = "Notifications",
            subtitle = "Enabled",
            onClick = {}
        )
        SettingsItem(
            icon = Icons.Filled.Flag,
            title = "Goals",
            subtitle = "Steps: 10,000/day • Sleep: 8h/night",
            onClick = {}
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy & About
        SectionHeader("Privacy & Info")
        SettingsItem(
            icon = Icons.Filled.Shield,
            title = "Privacy Control",
            subtitle = "Manage data sharing",
            onClick = onNavigateToPrivacyControl
        )
        SettingsItem(
            icon = Icons.Filled.Info,
            title = "About VitaNova",
            subtitle = "Version 1.0.0",
            onClick = { showAboutDialog = true }
        )
        SettingsItem(
            icon = Icons.Filled.Refresh,
            title = "Reset Onboarding",
            subtitle = "Go through setup again",
            onClick = {
                prefs.edit().putBoolean("onboarding_complete", false).apply()
                onNavigateToWelcome()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Emergency Button
        Button(
            onClick = { /* Emergency action */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VitaError)
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Emergency", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = VitaGreen)
                }
            },
            title = { Text("VitaNova", color = VitaTextPrimary) },
            text = {
                Text(
                    "Version 1.0.0\nYour Complete Health Ecosystem\n\n8 modules • Real sensors • Zero manual input\n\n© 2026 VitaNova Health Technologies",
                    color = VitaTextSecondary
                )
            },
            containerColor = VitaSurface
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = VitaTextTertiary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VitaGreen,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VitaTextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = VitaTextTertiary
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = VitaTextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
