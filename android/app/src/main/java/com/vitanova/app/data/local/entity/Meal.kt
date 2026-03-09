package com.vitanova.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "date")
    val date: String, // "yyyy-MM-dd"

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "meal_type")
    val mealType: String, // "breakfast", "lunch", "dinner", "snack"

    @ColumnInfo(name = "calories")
    val calories: Int,

    @ColumnInfo(name = "protein_grams")
    val proteinGrams: Float,

    @ColumnInfo(name = "carbs_grams")
    val carbsGrams: Float,

    @ColumnInfo(name = "fat_grams")
    val fatGrams: Float,

    @ColumnInfo(name = "fiber_grams")
    val fiberGrams: Float? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
