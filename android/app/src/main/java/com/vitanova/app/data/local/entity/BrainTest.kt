package com.vitanova.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brain_tests")
data class BrainTest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "date")
    val date: String, // "yyyy-MM-dd"

    @ColumnInfo(name = "test_type")
    val testType: String, // "reaction_time", "memory", "attention", "stroop"

    @ColumnInfo(name = "score")
    val score: Float,

    @ColumnInfo(name = "reaction_time_ms")
    val reactionTimeMs: Int? = null,

    @ColumnInfo(name = "accuracy_percent")
    val accuracyPercent: Float? = null,

    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int
)
