package com.vitanova.app.ui.onboarding

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitanova.app.ui.theme.*

@Composable
fun FirstDayScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToHrvMeasure: () -> Unit = {}
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(VitaGreenSurface, VitaBackground)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = VitaGreen,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Your profile is ready!",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = VitaTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Here's what's active for you",
                style = MaterialTheme.typography.bodyMedium,
                color = VitaTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Active modules
            val modules = listOf(
                Triple(Icons.Filled.Nightlight, "Sleep", SleepAccent),
                Triple(Icons.Filled.FavoriteBorder, "Energy", EnergyAccent),
                Triple(Icons.Filled.Visibility, "Focus", FocusAccent),
                Triple(Icons.Filled.DirectionsRun, "Fitness", FitnessAccent),
                Triple(Icons.Filled.Restaurant, "Nutrition", NutritionAccent),
                Triple(Icons.Filled.Psychology, "Brain", BrainAccent),
                Triple(Icons.Filled.CheckCircle, "Habits", HabitsAccent),
                Triple(Icons.Filled.Mood, "Mood", VitaCyan),
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                modules.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { (icon, label, color) ->
                            ModuleChip(icon = icon, label = label, color = color)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Suggestion
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VitaGreen.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("First action suggestion", style = MaterialTheme.typography.labelSmall, color = VitaGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Measure your HRV now",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VitaTextPrimary
                    )
                    Text(
                        "It takes only 60 seconds with your camera",
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            markOnboardingComplete(context)
                            onNavigateToHrvMeasure()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VitaGreen)
                    ) {
                        Text("Measure HRV", color = VitaBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                markOnboardingComplete(context)
                onNavigateToHome()
            }) {
                Text("Skip to Home →", color = VitaTextTertiary)
            }
        }
    }
}

private fun markOnboardingComplete(context: Context) {
    context.getSharedPreferences("vitanova_prefs", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("onboarding_complete", true)
        .apply()
}

@Composable
private fun ModuleChip(icon: ImageVector, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = VitaTextSecondary)
    }
}
