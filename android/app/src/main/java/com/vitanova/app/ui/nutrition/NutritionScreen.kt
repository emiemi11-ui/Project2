package com.vitanova.app.ui.nutrition

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitanova.app.data.local.entity.Meal
import com.vitanova.app.ui.theme.NutritionAccent
import com.vitanova.app.ui.theme.VitaBackground
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaOutline
import com.vitanova.app.ui.theme.VitaSurfaceCard
import com.vitanova.app.ui.theme.VitaSurfaceElevated
import com.vitanova.app.ui.theme.VitaSurfaceVariant
import com.vitanova.app.ui.theme.VitaTextOnPrimary
import com.vitanova.app.ui.theme.VitaTextPrimary
import com.vitanova.app.ui.theme.VitaTextSecondary
import com.vitanova.app.ui.theme.VitaTextTertiary
import com.vitanova.app.ui.theme.VitaWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Macro colors ────────────────────────────────────────────────────────────
private val ProteinColor = Color(0xFF00BCD4) // cyan
private val CarbsColor = Color(0xFF4CAF50)   // green
private val FatColor = Color(0xFFFF9800)      // orange
private val WaterBlue = Color(0xFF42A5F5)
private val WaterEmpty = Color(0xFF1E2A42)

// ── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun NutritionScreen(
    onNavigateToAddMeal: () -> Unit,
    viewModel: NutritionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = VitaBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddMeal,
                containerColor = NutritionAccent,
                contentColor = VitaTextOnPrimary,
                icon = {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                },
                text = {
                    Text(
                        text = "Adauga masa",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Nutritie",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VitaTextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Calories progress
            item {
                CaloriesProgressCard(
                    consumed = uiState.todayCalories,
                    goal = uiState.calorieGoal
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Macro donut chart
            item {
                MacroDonutCard(
                    protein = uiState.todayMacros.totalProtein,
                    carbs = uiState.todayMacros.totalCarbs,
                    fat = uiState.todayMacros.totalFat
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Hydration tracker
            item {
                HydrationCard(
                    glasses = uiState.waterGlasses,
                    onAddWater = { viewModel.addWater() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Weekly trend sparkline
            item {
                if (uiState.weeklyCalories.isNotEmpty()) {
                    WeeklyTrendCard(
                        dailyCalories = uiState.weeklyCalories.map { it.totalCalories.toFloat() },
                        dates = uiState.weeklyCalories.map { it.date }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Today's meals header
            item {
                if (uiState.todayMeals.isNotEmpty()) {
                    Text(
                        text = "Mesele de azi",
                        style = MaterialTheme.typography.titleMedium,
                        color = VitaTextPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }

            // Today's meals list
            items(uiState.todayMeals) { meal ->
                MealCard(meal = meal)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

// ── Calories Progress Card ───────────────────────────────────────────────────

@Composable
private fun CaloriesProgressCard(consumed: Int, goal: Int) {
    val fraction = (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1.5f)
    val remaining = (goal - consumed).coerceAtLeast(0)
    val progressColor = when {
        fraction > 1f -> Color(0xFFFF4757)
        fraction > 0.85f -> VitaWarning
        else -> VitaGreen
    }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(consumed) {
        animatedProgress.animateTo(
            targetValue = fraction.coerceAtMost(1f),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Calorii",
                        style = MaterialTheme.typography.titleSmall,
                        color = VitaTextPrimary
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$consumed",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = progressColor
                        )
                        Text(
                            text = " / $goal kcal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VitaTextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Ramase",
                        style = MaterialTheme.typography.labelSmall,
                        color = VitaTextTertiary
                    )
                    Text(
                        text = "$remaining",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (remaining > 0) VitaGreen else Color(0xFFFF4757)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { animatedProgress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = progressColor,
                trackColor = VitaSurfaceElevated
            )
        }
    }
}

// ── Macro Donut Chart Card ───────────────────────────────────────────────────

@Composable
private fun MacroDonutCard(protein: Float, carbs: Float, fat: Float) {
    val total = protein + carbs + fat
    val proteinPct = if (total > 0) protein / total else 0f
    val carbsPct = if (total > 0) carbs / total else 0f
    val fatPct = if (total > 0) fat / total else 0f

    val animatedSweep = remember { Animatable(0f) }
    LaunchedEffect(total) {
        animatedSweep.snapTo(0f)
        animatedSweep.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Macronutrienti",
                style = MaterialTheme.typography.titleSmall,
                color = VitaTextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut chart
                val textMeasurer = rememberTextMeasurer()

                Canvas(modifier = Modifier.size(140.dp)) {
                    val strokeWidth = 28f
                    val padding = strokeWidth / 2f + 4f
                    val arcSize = Size(
                        size.width - padding * 2,
                        size.height - padding * 2
                    )
                    val topLeft = Offset(padding, padding)
                    val totalSweep = 360f * animatedSweep.value

                    if (total <= 0) {
                        drawArc(
                            color = VitaOutline,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                    } else {
                        val proteinSweep = proteinPct * totalSweep
                        val carbsSweep = carbsPct * totalSweep
                        val fatSweep = fatPct * totalSweep

                        var startAngle = -90f

                        // Protein arc
                        drawArc(
                            color = ProteinColor,
                            startAngle = startAngle,
                            sweepAngle = proteinSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += proteinSweep

                        // Carbs arc
                        drawArc(
                            color = CarbsColor,
                            startAngle = startAngle,
                            sweepAngle = carbsSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += carbsSweep

                        // Fat arc
                        drawArc(
                            color = FatColor,
                            startAngle = startAngle,
                            sweepAngle = fatSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                    }

                    // Center text: total grams
                    val centerText = "${total.toInt()}g"
                    val centerStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VitaTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    val centerLayout = textMeasurer.measure(centerText, centerStyle)
                    drawText(
                        textLayoutResult = centerLayout,
                        topLeft = Offset(
                            (size.width - centerLayout.size.width) / 2f,
                            (size.height - centerLayout.size.height) / 2f
                        )
                    )
                }

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroLegendItem(
                        color = ProteinColor,
                        label = "Proteine",
                        value = "${protein.toInt()}g",
                        percentage = "${(proteinPct * 100).toInt()}%"
                    )
                    MacroLegendItem(
                        color = CarbsColor,
                        label = "Carbohidrati",
                        value = "${carbs.toInt()}g",
                        percentage = "${(carbsPct * 100).toInt()}%"
                    )
                    MacroLegendItem(
                        color = FatColor,
                        label = "Grasimi",
                        value = "${fat.toInt()}g",
                        percentage = "${(fatPct * 100).toInt()}%"
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroLegendItem(
    color: Color,
    label: String,
    value: String,
    percentage: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = VitaTextSecondary
            )
            Text(
                text = "$value ($percentage)",
                style = MaterialTheme.typography.bodySmall,
                color = VitaTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Hydration Tracker Card ───────────────────────────────────────────────────

@Composable
private fun HydrationCard(glasses: Int, onAddWater: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hidratare",
                    style = MaterialTheme.typography.titleSmall,
                    color = VitaTextPrimary
                )
                Text(
                    text = "$glasses / 8 pahare",
                    style = MaterialTheme.typography.bodySmall,
                    color = VitaTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..8) {
                    Icon(
                        imageVector = if (i <= glasses) Icons.Filled.LocalDrink
                        else Icons.Outlined.LocalDrink,
                        contentDescription = "Pahar $i",
                        tint = if (i <= glasses) WaterBlue else WaterEmpty,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = onAddWater,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WaterBlue.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Adauga pahar",
                        tint = WaterBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Weekly Trend Sparkline Card ──────────────────────────────────────────────

@Composable
private fun WeeklyTrendCard(dailyCalories: List<Float>, dates: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Trend saptamanal calorii",
                style = MaterialTheme.typography.titleSmall,
                color = VitaTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                if (dailyCalories.isEmpty()) return@Canvas

                val minVal = (dailyCalories.minOrNull() ?: 0f) * 0.8f
                val maxVal = (dailyCalories.maxOrNull() ?: 2000f) * 1.1f
                val range = (maxVal - minVal).coerceAtLeast(1f)

                val hPad = 16f
                val vPad = 8f
                val drawWidth = size.width - hPad * 2
                val drawHeight = size.height - vPad * 2

                val points = dailyCalories.mapIndexed { index, value ->
                    val x = if (dailyCalories.size == 1) drawWidth / 2f + hPad
                    else hPad + (index.toFloat() / (dailyCalories.size - 1)) * drawWidth
                    val y = vPad + drawHeight - ((value - minVal) / range) * drawHeight
                    Offset(x, y)
                }

                // Fill
                if (points.size >= 2) {
                    val fillPath = Path().apply {
                        moveTo(points.first().x, size.height)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, size.height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                NutritionAccent.copy(alpha = 0.3f),
                                NutritionAccent.copy(alpha = 0.0f)
                            )
                        )
                    )

                    // Line
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = linePath,
                        color = NutritionAccent,
                        style = Stroke(
                            width = 3f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Dots
                points.forEach { point ->
                    drawCircle(
                        color = NutritionAccent,
                        radius = 5f,
                        center = point
                    )
                    drawCircle(
                        color = VitaSurfaceCard,
                        radius = 2.5f,
                        center = point
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Date labels
            if (dates.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val formatDate = { dateStr: String ->
                        val parts = dateStr.split("-")
                        if (parts.size == 3) "${parts[2]}/${parts[1]}" else dateStr
                    }
                    Text(
                        text = formatDate(dates.last()),
                        style = MaterialTheme.typography.labelSmall,
                        color = VitaTextTertiary
                    )
                    Text(
                        text = formatDate(dates.first()),
                        style = MaterialTheme.typography.labelSmall,
                        color = VitaTextTertiary
                    )
                }
            }
        }
    }
}

// ── Meal Card ────────────────────────────────────────────────────────────────

@Composable
private fun MealCard(meal: Meal) {
    val timeFormatted = remember(meal.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(meal.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VitaSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = VitaTextTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VitaTextPrimary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextTertiary
                    )
                    Text(
                        text = "  |  P:${meal.proteinGrams.toInt()}g  C:${meal.carbsGrams.toInt()}g  F:${meal.fatGrams.toInt()}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = VitaTextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${meal.calories}",
                    style = MaterialTheme.typography.titleMedium,
                    color = NutritionAccent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "kcal",
                    style = MaterialTheme.typography.labelSmall,
                    color = VitaTextTertiary
                )
            }
        }
    }
}
