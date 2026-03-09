package com.vitanova.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "date")
    val date: String, // "yyyy-MM-dd"

    @ColumnInfo(name = "start_time")
    val startTime: Long,

    @ColumnInfo(name = "end_time")
    val endTime: Long,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,

    @ColumnInfo(name = "session_type")
    val sessionType: String, // "pomodoro", "deep_work", "flow"

    @ColumnInfo(name = "completed")
    val completed: Boolean,

    @ColumnInfo(name = "distractions_count")
    val distractionsCount: Int = 0,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
