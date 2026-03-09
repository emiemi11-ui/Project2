package com.vitanova.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_sessions")
data class SleepSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "start_time")
    val startTime: Long,

    @ColumnInfo(name = "end_time")
    val endTime: Long,

    @ColumnInfo(name = "total_duration_minutes")
    val totalDurationMinutes: Int,

    @ColumnInfo(name = "efficiency_percent")
    val efficiencyPercent: Float,

    @ColumnInfo(name = "deep_minutes")
    val deepMinutes: Int,

    @ColumnInfo(name = "light_minutes")
    val lightMinutes: Int,

    @ColumnInfo(name = "rem_minutes")
    val remMinutes: Int,

    @ColumnInfo(name = "awake_minutes")
    val awakeMinutes: Int,

    @ColumnInfo(name = "cycles_count")
    val cyclesCount: Int,

    @ColumnInfo(name = "sleep_score")
    val sleepScore: Int
)
