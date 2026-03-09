package com.vitanova.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hrv_readings")
data class HrvReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "date")
    val date: String, // "yyyy-MM-dd"

    @ColumnInfo(name = "rmssd")
    val rmssd: Float,

    @ColumnInfo(name = "sdnn")
    val sdnn: Float,

    @ColumnInfo(name = "lf_hf_ratio")
    val lfHfRatio: Float? = null,

    @ColumnInfo(name = "stress_index")
    val stressIndex: Float,

    @ColumnInfo(name = "recovery_score")
    val recoveryScore: Int,

    @ColumnInfo(name = "measurement_duration_seconds")
    val measurementDurationSeconds: Int
)
