package com.vitanova.app.ui.fitness

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitanova.app.sensor.GpsTrackingState
import com.vitanova.app.ui.theme.FitnessAccent
import com.vitanova.app.ui.theme.VitaBackground
import com.vitanova.app.ui.theme.VitaError
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaSurfaceElevated
import com.vitanova.app.ui.theme.VitaSurfaceVariant
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import com.vitanova.app.ui.theme.VitaWarning
import java.util.Locale
import com.vitanova.app.sensor.GpsPoint as SensorGpsPoint

// ── Active Tracking Screen ───────────────────────────────────────────────────

@Composable
fun ActiveTrackingScreen(
    viewModel: FitnessViewModel,
    activityType: String = "running",
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isTracking by viewModel.isTracking.collectAsState()
    val trackingState by viewModel.currentTrackingState.collectAsState()
    val currentActivityType by viewModel.currentActivityType.collectAsState()

    var hasStopped by remember { mutableStateOf(false) }
    var finalState by remember { mutableStateOf<GpsTrackingState?>(null) }

    val activityLabel = when (activityType) {
        "running" -> "Alergare"
        "cycling" -> "Ciclism"
        "walking" -> "Plimbare"
        else -> activityType.replaceFirstChar { it.uppercase() }
    }

    val activityIcon = when (activityType) {
        "running" -> Icons.Filled.DirectionsRun
        "cycling" -> Icons.Filled.DirectionsBike
        "walking" -> Icons.Filled.DirectionsWalk
        else -> Icons.Filled.DirectionsRun
    }

    val accentColor = when (activityType) {
        "running" -> FitnessAccent
        "cycling" -> Color(0xFF3B82F6)
        "walking" -> VitaGreen
        else -> FitnessAccent
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
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (isTracking) {
                    viewModel.stopGpsTracking(context)
                }
                onNavigateBack()
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Inapoi",
                    tint = VitaTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = activityIcon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = activityLabel,
                style = MaterialTheme.typography.titleLarge,
                color = VitaTextPrimary
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isTracking) {
                val pulseTransition = rememberInfiniteTransition(label = "gps_pulse")
                val pulseAlpha by pulseTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "gps_alpha"
                )
                Icon(
                    imageVector = Icons.Filled.GpsFixed,
                    contentDescription = "GPS activ",
                    tint = VitaGreen.copy(alpha = pulseAlpha),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (hasStopped && finalState != null) {
            // Summary view after stopping
            TrackingSummary(
                state = finalState!!,
                activityType = activityType,
                accentColor = accentColor,
                onSave = onNavigateBack
            )
        } else {
            // Active tracking view
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // GPS Map placeholder
                item {
                    GpsMapPlaceholder(
                        points = trackingState.points,
                        accentColor = accentColor,
                        isTracking = isTracking
                    )
                }

                // Live stats
                item {
                    LiveStatsRow(
                        trackingState = trackingState,
                        activityType = activityType,
                        accentColor = accentColor
                    )
                }

                // Duration display
                item {
                    DurationDisplay(
                        durationMs = trackingState.durationMs,
                        isPaused = trackingState.isPaused,
                        accentColor = accentColor
                    )
                }

                // Foreground service notification
                item {
                    if (isTracking) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = VitaGreen.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.GpsFixed,
                                    contentDescription = null,
                                    tint = VitaGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Serviciul de localizare ruleaza in fundal. Poti minimiza aplicatia.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VitaGreen
                                )
                            }
                        }
                    }
                }

                // Control buttons
                item {
                    TrackingControlButtons(
                        isTracking = isTracking,
                        isPaused = trackingState.isPaused,
                        accentColor = accentColor,
                        onStart = {
                            viewModel.startGpsTracking(context, activityType)
                        },
                        onPause = {
                            viewModel.pauseTracking()
                        },
                        onStop = {
                            finalState = trackingState
                            viewModel.stopGpsTracking(context)
                            hasStopped = true
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

// ── GPS Map Placeholder ──────────────────────────────────────────────────────

@Composable
private fun GpsMapPlaceholder(
    points: List<SensorGpsPoint>,
    accentColor: Color,
    isTracking: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F18)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (points.size >= 2) {
                Canvas(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                    val lats = points.map { it.latitude }
                    val lngs = points.map { it.longitude }
                    val minLat = lats.min()
                    val maxLat = lats.max()
                    val minLng = lngs.min()
                    val maxLng = lngs.max()

                    val latRange = (maxLat - minLat).coerceAtLeast(0.0001)
                    val lngRange = (maxLng - minLng).coerceAtLeast(0.0001)

                    val padding = 20f
                    val drawWidth = size.width - padding * 2
                    val drawHeight = size.height - padding * 2

                    fun toScreen(lat: Double, lng: Double): Offset {
                        val x = padding + ((lng - minLng) / lngRange * drawWidth).toFloat()
                        val y = padding + ((maxLat - lat) / latRange * drawHeight).toFloat()
                        return Offset(x, y)
                    }

                    // Draw grid lines
                    val gridColor = Color.White.copy(alpha = 0.05f)
                    for (i in 0..4) {
                        val y = padding + (drawHeight * i / 4)
                        drawLine(gridColor, Offset(padding, y), Offset(size.width - padding, y))
                        val x = padding + (drawWidth * i / 4)
                        drawLine(gridColor, Offset(x, padding), Offset(x, size.height - padding))
                    }

                    // Draw polyline
                    for (i in 1 until points.size) {
                        val from = toScreen(points[i - 1].latitude, points[i - 1].longitude)
                        val to = toScreen(points[i].latitude, points[i].longitude)

                        // Trail shadow
                        drawLine(
                            color = accentColor.copy(alpha = 0.2f),
                            start = from,
                            end = to,
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )

                        // Main line
                        drawLine(
                            color = accentColor,
                            start = from,
                            end = to,
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                    }

                    // Draw start point
                    val startPt = toScreen(points.first().latitude, points.first().longitude)
                    drawCircle(
                        color = VitaGreen,
                        radius = 8f,
                        center = startPt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = startPt
                    )

                    // Draw current point
                    val endPt = toScreen(points.last().latitude, points.last().longitude)
                    drawCircle(
                        color = accentColor,
                        radius = 10f,
                        center = endPt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5f,
                        center = endPt
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.GpsFixed,
                        contentDescription = null,
                        tint = VitaTextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isTracking) "Se asteapta semnal GPS..." else "Porneste pentru a vedea traseul",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VitaTextTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ── Live Stats Row ───────────────────────────────────────────────────────────

@Composable
private fun LiveStatsRow(
    trackingState: GpsTrackingState,
    activityType: String,
    accentColor: Color
) {
    val distanceKm = trackingState.totalDistanceMeters / 1000.0
    val paceMinPerKm = trackingState.currentPaceMinPerKm
    val durationMs = trackingState.durationMs
    val caloriesEstimate = estimateCaloriesFromState(activityType, durationMs, trackingState.totalDistanceMeters)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LiveStatCard(
            modifier = Modifier.weight(1f),
            label = "Distanta",
            value = "%.2f".format(distanceKm),
            unit = "km",
            accentColor = accentColor
        )
        LiveStatCard(
            modifier = Modifier.weight(1f),
            label = "Ritm",
            value = formatPaceValue(paceMinPerKm),
            unit = "min/km",
            accentColor = accentColor
        )
        LiveStatCard(
            modifier = Modifier.weight(1f),
            label = "Calorii",
            value = "$caloriesEstimate",
            unit = "kcal",
            accentColor = Color(0xFFEF4444)
        )
    }
}

@Composable
private fun LiveStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = VitaTextTertiary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = accentColor
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = VitaTextTertiary
            )
        }
    }
}

// ── Duration Display ─────────────────────────────────────────────────────────

@Composable
private fun DurationDisplay(
    durationMs: Long,
    isPaused: Boolean,
    accentColor: Color
) {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val timeStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Durata",
                style = MaterialTheme.typography.labelMedium,
                color = VitaTextTertiary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = timeStr,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = if (isPaused) VitaWarning else accentColor
            )
            if (isPaused) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "PAUZA",
                    style = MaterialTheme.typography.labelLarge,
                    color = VitaWarning
                )
            }
        }
    }
}

// ── Control Buttons ──────────────────────────────────────────────────────────

@Composable
private fun TrackingControlButtons(
    isTracking: Boolean,
    isPaused: Boolean,
    accentColor: Color,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isTracking) {
            // Start button
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
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
                    text = "START",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
            }
        } else {
            // Pause/Resume + Stop
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pause / Resume
                FilledTonalButton(
                    onClick = onPause,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isPaused) accentColor else VitaSurfaceElevated
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = null,
                        tint = if (isPaused) Color.White else VitaTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPaused) "CONTINUA" else "PAUZA",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isPaused) Color.White else VitaTextPrimary
                    )
                }

                // Stop
                Button(
                    onClick = onStop,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VitaError
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STOP",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

// ── Tracking Summary ─────────────────────────────────────────────────────────

@Composable
private fun TrackingSummary(
    state: GpsTrackingState,
    activityType: String,
    accentColor: Color,
    onSave: () -> Unit
) {
    val distanceKm = state.totalDistanceMeters / 1000.0
    val durationMs = state.durationMs
    val calories = estimateCaloriesFromState(activityType, durationMs, state.totalDistanceMeters)
    val avgPace = if (state.totalDistanceMeters > 0) {
        (durationMs / 60_000.0) / (state.totalDistanceMeters / 1000.0)
    } else 0.0
    val avgSpeed = state.averageSpeedKmh
    val elevation = state.elevationGainMeters

    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val durationStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

    val activityLabel = when (activityType) {
        "running" -> "Alergare"
        "cycling" -> "Ciclism"
        "walking" -> "Plimbare"
        else -> activityType.replaceFirstChar { it.uppercase() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Sumar $activityLabel",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = accentColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Map with final route
        item {
            GpsMapPlaceholder(
                points = state.points,
                accentColor = accentColor,
                isTracking = false
            )
        }

        // Summary stats grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Timer,
                        label = "Durata",
                        value = durationStr,
                        color = accentColor
                    )
                    SummaryStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.DirectionsRun,
                        label = "Distanta",
                        value = "%.2f km".format(distanceKm),
                        color = accentColor
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Speed,
                        label = "Ritm mediu",
                        value = formatPaceValue(avgPace),
                        color = accentColor
                    )
                    SummaryStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.LocalFireDepartment,
                        label = "Calorii",
                        value = "$calories kcal",
                        color = Color(0xFFEF4444)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Speed,
                        label = "Viteza medie",
                        value = "%.1f km/h".format(avgSpeed),
                        color = accentColor
                    )
                    SummaryStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Terrain,
                        label = "Altitudine +",
                        value = "%.0f m".format(elevation),
                        color = VitaGreen
                    )
                }
            }
        }

        // Save button
        item {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Salvat! Intoarce-te",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun SummaryStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = VitaTextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = VitaTextTertiary
            )
        }
    }
}

// ── Utility ──────────────────────────────────────────────────────────────────

private fun formatPaceValue(paceMinPerKm: Double): String {
    if (paceMinPerKm <= 0 || paceMinPerKm > 30) return "--:--"
    val wholeMinutes = paceMinPerKm.toInt()
    val seconds = ((paceMinPerKm - wholeMinutes) * 60).toInt()
    return "%d:%02d".format(wholeMinutes, seconds)
}

private fun estimateCaloriesFromState(type: String, durationMs: Long, distanceMeters: Double): Int {
    val durationMinutes = durationMs / 60_000.0
    val metValue = when (type) {
        "running" -> 9.8
        "cycling" -> 7.5
        "walking" -> 3.8
        else -> 5.0
    }
    return (metValue * 3.5 * 70.0 / 200.0 * durationMinutes).toInt()
}
