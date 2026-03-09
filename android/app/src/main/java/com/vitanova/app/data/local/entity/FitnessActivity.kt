package com.vitanova.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fitness_activities")
data class FitnessActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "type")
    val type: String, // "running", "cycling", "swimming", "walking", "gym"

    @ColumnInfo(name = "start_time")
    val startTime: Long,

    @ColumnInfo(name = "end_time")
    val endTime: Long,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,

    @ColumnInfo(name = "calories_burned")
    val caloriesBurned: Int,

    @ColumnInfo(name = "distance_meters")
    val distanceMeters: Float? = null,

    @ColumnInfo(name = "avg_heart_rate")
    val avgHeartRate: Int? = null,

    @ColumnInfo(name = "max_heart_rate")
    val maxHeartRate: Int? = null,

    @ColumnInfo(name = "avg_pace")
    val avgPace: Float? = null,

    @ColumnInfo(name = "elevation_gain_meters")
    val elevationGainMeters: Float? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
