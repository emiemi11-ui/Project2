package com.vitanova.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cognitive_tests")
data class CognitiveTest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "test_type")
    val testType: String,

    @ColumnInfo(name = "score")
    val score: Int,

    @ColumnInfo(name = "raw_value")
    val rawValue: Float,

    @ColumnInfo(name = "details")
    val details: String
)
