package com.vitanova.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "date")
    val date: String, // "yyyy-MM-dd"

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "mood_score")
    val moodScore: Int, // 1-10

    @ColumnInfo(name = "energy_level")
    val energyLevel: Int, // 1-10

    @ColumnInfo(name = "stress_level")
    val stressLevel: Int, // 1-10

    @ColumnInfo(name = "anxiety_level")
    val anxietyLevel: Int? = null, // 1-10

    @ColumnInfo(name = "tags")
    val tags: String? = null, // comma-separated: "happy,productive,social"

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
