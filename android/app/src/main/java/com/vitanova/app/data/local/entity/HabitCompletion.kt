package com.vitanova.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "habit_id", index = true)
    val habitId: Long,

    @ColumnInfo(name = "date")
    val date: String, // "yyyy-MM-dd"

    @ColumnInfo(name = "completed_at")
    val completedAt: Long,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
