package com.vitanova.app.ui.nutrition

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── Ingredient Data ──────────────────────────────────────────────────────────

data class Ingredient(
    val name: String,
    val caloriesPer100g: Int,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float
)

val ingredientDatabase = listOf(
    Ingredient("Piept pui 100g", 165, 31f, 0f, 3.6f),
    Ingredient("Orez alb 100g", 130, 2.7f, 28f, 0.3f),
    Ingredient("Orez brun 100g", 112, 2.6f, 24f, 0.9f),
    Ingredient("Ou intreg", 155, 13f, 1.1f, 11f),
    Ingredient("Banana", 89, 1.1f, 23f, 0.3f),
    Ingredient("Mar", 52, 0.3f, 14f, 0.2f),
    Ingredient("Portocala", 47, 0.9f, 12f, 0.1f),
    Ingredient("Capsuni 100g", 33, 0.7f, 8f, 0.3f),
    Ingredient("Afine 100g", 57, 0.7f, 14f, 0.3f),
    Ingredient("Somon 100g", 208, 20f, 0f, 13f),
    Ingredient("Ton conserva 100g", 116, 26f, 0f, 1f),
    Ingredient("Carne vita 100g", 250, 26f, 0f, 15f),
    Ingredient("Carne porc 100g", 242, 27f, 0f, 14f),
    Ingredient("Curcan 100g", 135, 30f, 0f, 1f),
    Ingredient("Lapte 250ml", 149, 8f, 12f, 8f),
    Ingredient("Iaurt grecesc 100g", 97, 9f, 3.6f, 5f),
    Ingredient("Iaurt normal 100g", 61, 3.5f, 4.7f, 3.3f),
    Ingredient("Branza de vaci 100g", 98, 11f, 3.4f, 4.3f),
    Ingredient("Cascaval 100g", 350, 25f, 1.3f, 27f),
    Ingredient("Mozzarella 100g", 280, 28f, 3.1f, 17f),
    Ingredient("Parmezan 100g", 431, 38f, 4.1f, 29f),
    Ingredient("Paine alba 100g", 265, 9f, 49f, 3.2f),
    Ingredient("Paine integrala 100g", 247, 13f, 41f, 3.4f),
    Ingredient("Paste 100g", 131, 5f, 25f, 1.1f),
    Ingredient("Cartof 100g", 77, 2f, 17f, 0.1f),
    Ingredient("Cartof dulce 100g", 86, 1.6f, 20f, 0.1f),
    Ingredient("Brocoli 100g", 34, 2.8f, 7f, 0.4f),
    Ingredient("Spanac 100g", 23, 2.9f, 3.6f, 0.4f),
    Ingredient("Rosii 100g", 18, 0.9f, 3.9f, 0.2f),
    Ingredient("Castraveti 100g", 15, 0.7f, 3.6f, 0.1f),
    Ingredient("Morcov 100g", 41, 0.9f, 10f, 0.2f),
    Ingredient("Ardei gras 100g", 20, 0.9f, 4.6f, 0.2f),
    Ingredient("Ceapa 100g", 40, 1.1f, 9f, 0.1f),
    Ingredient("Ciuperci 100g", 22, 3.1f, 3.3f, 0.3f),
    Ingredient("Avocado 100g", 160, 2f, 9f, 15f),
    Ingredient("Migdale 100g", 579, 21f, 22f, 50f),
    Ingredient("Nuci 100g", 654, 15f, 14f, 65f),
    Ingredient("Arahide 100g", 567, 26f, 16f, 49f),
    Ingredient("Unt de arahide 100g", 588, 25f, 20f, 50f),
    Ingredient("Seminte floarea soarelui 100g", 584, 21f, 20f, 51f),
    Ingredient("Seminte chia 100g", 486, 17f, 42f, 31f),
    Ingredient("Ulei de masline 1 lingura", 119, 0f, 0f, 14f),
    Ingredient("Unt 10g", 72, 0.1f, 0f, 8.1f),
    Ingredient("Miere 1 lingura", 64, 0.1f, 17f, 0f),
    Ingredient("Ciocolata neagra 100g", 546, 5f, 60f, 31f),
    Ingredient("Fulgi ovaz 100g", 389, 17f, 66f, 7f),
    Ingredient("Quinoa 100g", 120, 4.4f, 21f, 1.9f),
    Ingredient("Linte 100g", 116, 9f, 20f, 0.4f),
    Ingredient("Fasole 100g", 127, 8.7f, 23f, 0.5f),
    Ingredient("Naut 100g", 164, 8.9f, 27f, 2.6f),
    Ingredient("Tofu 100g", 76, 8f, 1.9f, 4.8f),
    Ingredient("Hummus 100g", 166, 7.9f, 14f, 9.6f),
    Ingredient("Suc portocale 250ml", 112, 1.7f, 26f, 0.5f),
    Ingredient("Cafea neagra", 2, 0.3f, 0f, 0f),
    Ingredient("Otet balsamic 1 lingura", 14, 0.1f, 2.7f, 0f)
)

// ── Add Meal Screen ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    onNavigateBack: () -> Unit,
    onSaveMeal: (Meal) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("100") }
    var searchQuery by remember { mutableStateOf("") }
    var showIngredients by remember { mutableStateOf(false) }
    var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }

    val filteredIngredients = remember(searchQuery) {
        if (searchQuery.isBlank()) ingredientDatabase
        else ingredientDatabase.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NutritionAccent,
        unfocusedBorderColor = VitaOutline,
        focusedLabelColor = NutritionAccent,
        unfocusedLabelColor = VitaTextTertiary,
        cursorColor = NutritionAccent,
        focusedTextColor = VitaTextPrimary,
        unfocusedTextColor = VitaTextPrimary
    )

    Scaffold(
        containerColor = VitaBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Adauga masa",
                        color = VitaTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Inapoi",
                            tint = VitaTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VitaBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Camera button
            item {
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(VitaSurfaceCard)
                        .clickable { /* Would launch camera intent */ },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Fotografiaza masa",
                            tint = VitaTextTertiary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fotografiaza masa",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VitaTextTertiary
                        )
                    }
                }
            }

            // Title field
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Denumire") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true
                )
            }

            // Ingredient search
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        showIngredients = it.isNotBlank()
                    },
                    label = { Text("Cauta ingredient") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = VitaTextTertiary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    singleLine = true
                )
            }

            // Ingredient dropdown
            if (showIngredients && filteredIngredients.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = VitaSurfaceCard)
                    ) {
                        Column {
                            val displayList = filteredIngredients.take(8)
                            displayList.forEachIndexed { index, ingredient ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedIngredient = ingredient
                                            val qty = quantityText.toFloatOrNull() ?: 100f
                                            val multiplier = qty / 100f
                                            title = ingredient.name
                                            caloriesText = (ingredient.caloriesPer100g * multiplier).toInt().toString()
                                            proteinText = String.format("%.1f", ingredient.proteinPer100g * multiplier)
                                            carbsText = String.format("%.1f", ingredient.carbsPer100g * multiplier)
                                            fatText = String.format("%.1f", ingredient.fatPer100g * multiplier)
                                            searchQuery = ingredient.name
                                            showIngredients = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ingredient.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = VitaTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "P:${ingredient.proteinPer100g}g  C:${ingredient.carbsPer100g}g  F:${ingredient.fatPer100g}g",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = VitaTextTertiary
                                        )
                                    }
                                    Text(
                                        text = "${ingredient.caloriesPer100g} kcal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = NutritionAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (index < displayList.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .padding(horizontal = 16.dp)
                                            .background(VitaOutline)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quantity multiplier (visible when ingredient is selected)
            if (selectedIngredient != null) {
                item {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { newQty ->
                            quantityText = newQty
                            val qty = newQty.toFloatOrNull() ?: 100f
                            val multiplier = qty / 100f
                            selectedIngredient?.let { ing ->
                                caloriesText = (ing.caloriesPer100g * multiplier).toInt().toString()
                                proteinText = String.format("%.1f", ing.proteinPer100g * multiplier)
                                carbsText = String.format("%.1f", ing.carbsPer100g * multiplier)
                                fatText = String.format("%.1f", ing.fatPer100g * multiplier)
                            }
                        },
                        label = { Text("Cantitate (g)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }

            // Macro fields
            item {
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { caloriesText = it },
                    label = { Text("Calorii (kcal)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = proteinText,
                        onValueChange = { proteinText = it },
                        label = { Text("Proteine (g)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = carbsText,
                        onValueChange = { carbsText = it },
                        label = { Text("Carbohidrati (g)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fatText,
                        onValueChange = { fatText = it },
                        label = { Text("Grasimi (g)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            }

            // Save button
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val meal = Meal(
                            date = today,
                            timestamp = now,
                            name = title.ifBlank { "Masa" },
                            mealType = "snack",
                            calories = caloriesText.toIntOrNull() ?: 0,
                            proteinGrams = proteinText.toFloatOrNull() ?: 0f,
                            carbsGrams = carbsText.toFloatOrNull() ?: 0f,
                            fatGrams = fatText.toFloatOrNull() ?: 0f
                        )
                        onSaveMeal(meal)
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NutritionAccent,
                        contentColor = VitaTextOnPrimary
                    ),
                    enabled = title.isNotBlank() || caloriesText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Salveaza",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
