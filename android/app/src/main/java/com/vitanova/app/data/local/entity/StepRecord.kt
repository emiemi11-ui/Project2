package com.vitanova.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_records")
data class StepRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "date")
    val date: String, // "yyyy-MM-dd"

    @ColumnInfo(name = "step_count")
    val stepCount: Int,

    @ColumnInfo(name = "distance_meters")
    val distanceMeters: Float? = null,

    @ColumnInfo(name = "calories_burned")
    val caloriesBurned: Int? = null,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long
)
