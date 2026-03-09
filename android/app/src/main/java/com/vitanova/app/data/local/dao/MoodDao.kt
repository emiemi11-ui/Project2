package com.vitanova.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vitanova.app.data.local.entity.MoodEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(moodEntry: MoodEntry): Long

    @Query("SELECT * FROM mood_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getByDate(date: String): Flow<List<MoodEntry>>

    @Query(
        """
        SELECT * FROM mood_entries
        WHERE date >= :sevenDaysAgo
        ORDER BY timestamp DESC
        """
    )
    fun getLast7Days(sevenDaysAgo: String): Flow<List<MoodEntry>>

    @Query(
        """
        SELECT * FROM mood_entries
        WHERE date >= :thirtyDaysAgo
        ORDER BY timestamp DESC
        """
    )
    fun getLast30Days(thirtyDaysAgo: String): Flow<List<MoodEntry>>
}
