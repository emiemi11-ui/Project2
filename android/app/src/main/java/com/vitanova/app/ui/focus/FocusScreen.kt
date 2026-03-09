package com.vitanova.app.ui.focus

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitanova.app.ui.theme.FocusAccent
import com.vitanova.app.ui.theme.VitaBackground
import com.vitanova.app.ui.theme.VitaCyan
import com.vitanova.app.ui.theme.VitaError
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaOutline
import com.vitanova.app.ui.theme.VitaSuccess
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaSurfaceElevated
import com.vitanova.app.ui.theme.VitaSurfaceVariant
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import com.vitanova.app.ui.theme.VitaWarning
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FocusScreen(
    onNavigateToTimer: () -> Unit = {},
    onNavigateToDetox: () -> Unit = {},
    viewModel: FocusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.hasUsageStatsPermission) {
        if (uiState.hasUsageStatsPermission) {
            viewModel.loadUsageStats(context)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VitaBackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            FocusHeader()
        }

        // Permission request card
        if (!uiState.hasUsageStatsPermission) {
            item {
                UsageStatsPermissionCard(
                    onOpenSettings = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                )
            }
        }

        // Screen time card
        item {
            ScreenTimeCard(
                totalMinutes = uiState.totalScreenTimeMinutes,
                yesterdayMinutes = uiState.yesterdayScreenTimeMinutes
            )
        }

        // Pickups card
        item {
            PickupsCard(pickupsCount = uiState.pickupsCount)
        }

        // Top 5 Apps bar chart
        if (uiState.topApps.isNotEmpty()) {
            item {
                TopAppsChart(apps = uiState.topApps)
            }
        }

        // Focus sessions today
        item {
            FocusSessionsCard(
                completedSessions = uiState.focusSessionsToday.count { it.completed },
                goalSessions = uiState.focusGoalPerDay,
                streak = uiState.focusStreak
            )
        }

        // Vulnerable hours insight
        if (uiState.vulnerableHourMessage.isNotBlank()) {
            item {
                VulnerableHoursCard(message = uiState.vulnerableHourMessage)
            }
        }

        // Category breakdown donut
        if (uiState.categoryBreakdown.isNotEmpty()) {
            item {
                CategoryBreakdownCard(categories = uiState.categoryBreakdown)
            }
        }

        // Quick action buttons
        item {
            QuickActionButtons(
                onStartTimer = onNavigateToTimer,
                onDetoxMode = onNavigateToDetox
            )
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FocusHeader() {
    val today = LocalDate.now()
    val dateText = today.format(
        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("ro"))
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Focus Digital",
            style = MaterialTheme.typography.headlineLarge,
            color = VitaTextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = VitaTextSecondary
        )
    }
}

@Composable
private fun UsageStatsPermissionCard(onOpenSettings: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = VitaWarning,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Permisiune necesara",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pentru a monitoriza timpul petrecut pe ecran, VitaNova are nevoie de acces la statisticile de utilizare. Datele raman pe dispozitivul tau.",
                style = MaterialTheme.typography.bodyMedium,
                color = VitaTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VitaGreen,
                    contentColor = Color(0xFF003321)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Deschide Setarile",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ScreenTimeCard(totalMinutes: Int, yesterdayMinutes: Int) {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val timeText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    val diffMinutes = totalMinutes - yesterdayMinutes
    val diffText = when {
        diffMinutes > 0 -> "+${diffMinutes} min"
        diffMinutes < 0 -> "${diffMinutes} min"
        else -> "egal"
    }
    val diffColor = when {
        diffMinutes > 0 -> VitaError
        diffMinutes < 0 -> VitaSuccess
        else -> VitaTextSecondary
    }
    val diffIcon = when {
        diffMinutes > 0 -> Icons.Filled.ArrowUpward
        diffMinutes < 0 -> Icons.Filled.ArrowDownward
        else -> null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.PhoneAndroid,
                    contentDescription = null,
                    tint = FocusAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Timp pe ecran azi",
                    style = MaterialTheme.typography.titleSmall,
                    color = VitaTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = VitaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (diffIcon != null) {
                        Icon(
                            imageVector = diffIcon,
                            contentDescription = null,
                            tint = diffColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    Text(
                        text = diffText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = diffColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (yesterdayMinutes > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "fata de ieri",
                    style = MaterialTheme.typography.bodySmall,
                    color = VitaTextTertiary
                )
            }
        }
    }
}

@Composable
private fun PickupsCard(pickupsCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ScreenLockPortrait,
                contentDescription = null,
                tint = VitaCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ridicari telefon",
                    style = MaterialTheme.typography.titleSmall,
                    color = VitaTextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$pickupsCount",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = VitaTextPrimary
                    )
                )
            }
            Text(
                text = "azi",
                style = MaterialTheme.typography.bodySmall,
                color = VitaTextTertiary
            )
        }
    }
}

@Composable
private fun TopAppsChart(apps: List<AppUsageUiModel>) {
    val maxMinutes = apps.maxOfOrNull { it.usageMinutes } ?: 1

    val barColors = listOf(
        FocusAccent,
        VitaCyan,
        VitaWarning,
        Color(0xFFA78BFA),
        Color(0xFFFB7185)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Top 5 Aplicatii",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            apps.take(5).forEachIndexed { index, app ->
                val fraction = app.usageMinutes.toFloat() / maxMinutes.coerceAtLeast(1)
                val color = barColors[index % barColors.size]
                val timeText = if (app.usageMinutes >= 60) {
                    "${app.usageMinutes / 60}h ${app.usageMinutes % 60}m"
                } else {
                    "${app.usageMinutes}m"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App icon placeholder
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.appName.take(1).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = VitaTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = timeText,
                                style = MaterialTheme.typography.bodySmall,
                                color = VitaTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Horizontal bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(VitaSurfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                                    .animateContentSize()
                            )
                        }
                    }
                }
                if (index < apps.size - 1 && index < 4) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun FocusSessionsCard(
    completedSessions: Int,
    goalSessions: Int,
    streak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sesiuni focus azi",
                    style = MaterialTheme.typography.titleMedium,
                    color = VitaTextPrimary
                )
                if (streak > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "\uD83D\uDD25",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$streak zile",
                            style = MaterialTheme.typography.labelMedium,
                            color = VitaWarning,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "$completedSessions/$goalSessions completate",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = VitaTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Mini progress dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until goalSessions) {
                    val dotColor = if (i < completedSessions) VitaGreen else VitaOutline
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun VulnerableHoursCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = VitaWarning.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = VitaWarning,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Interval vulnerabil",
                    style = MaterialTheme.typography.labelMedium,
                    color = VitaWarning,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VitaTextPrimary
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(categories: List<CategoryBreakdown>) {
    val categoryColors = mapOf(
        "Social" to Color(0xFFFB7185),
        "Productive" to VitaGreen,
        "Entertainment" to VitaCyan,
        "Other" to VitaTextTertiary
    )

    val totalMinutes = categories.sumOf { it.minutes }.coerceAtLeast(1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Categorii utilizare",
                style = MaterialTheme.typography.titleMedium,
                color = VitaTextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Donut chart
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(140.dp)) {
                        val strokeWidth = 24.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val topLeft = Offset(
                            (size.width - 2 * radius) / 2,
                            (size.height - 2 * radius) / 2
                        )
                        val arcSize = Size(radius * 2, radius * 2)

                        var startAngle = -90f
                        categories.forEach { cat ->
                            val sweep = cat.percentage * 360f
                            val color = categoryColors[cat.category]
                                ?: VitaTextTertiary
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweep.coerceAtLeast(1f),
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )
                            startAngle += sweep
                        }
                    }
                    // Center text
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val totalHours = totalMinutes / 60
                        val totalMins = totalMinutes % 60
                        Text(
                            text = if (totalHours > 0) "${totalHours}h" else "${totalMins}m",
                            style = MaterialTheme.typography.titleLarge,
                            color = VitaTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "total",
                            style = MaterialTheme.typography.bodySmall,
                            color = VitaTextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category legend
            categories.forEach { cat ->
                val color = categoryColors[cat.category] ?: VitaTextTertiary
                val timeText = if (cat.minutes >= 60) {
                    "${cat.minutes / 60}h ${cat.minutes % 60}m"
                } else {
                    "${cat.minutes}m"
                }
                val pct = (cat.percentage * 100).toInt()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cat.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VitaTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$timeText ($pct%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionButtons(
    onStartTimer: () -> Unit,
    onDetoxMode: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onStartTimer,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VitaGreen,
                contentColor = Color(0xFF003321)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Start Focus Timer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedButton(
            onClick = onDetoxMode,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.5.dp
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = VitaGreen
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Detox Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
