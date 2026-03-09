package com.vitanova.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vitanova.app.data.local.entity.SleepSample
import com.vitanova.app.data.local.entity.SleepSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SleepSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sample: SleepSample): Long

    @Query("SELECT * FROM sleep_sessions WHERE id = :sessionId")
    fun getSessionById(sessionId: Long): Flow<SleepSession?>

    @Query(
        """
        SELECT * FROM sleep_sessions
        WHERE start_time >= :startOfDay AND start_time < :endOfDay
        ORDER BY start_time DESC
        LIMIT 1
        """
    )
    fun getSessionByDate(startOfDay: Long, endOfDay: Long): Flow<SleepSession?>

    @Query("SELECT * FROM sleep_sessions ORDER BY start_time DESC LIMIT 7")
    fun getLast7Sessions(): Flow<List<SleepSession>>

    @Query("SELECT * FROM sleep_sessions ORDER BY start_time DESC LIMIT 30")
    fun getLast30Sessions(): Flow<List<SleepSession>>

    @Query("SELECT * FROM sleep_samples WHERE session_id = :sessionId ORDER BY timestamp ASC")
    fun getSamplesBySessionId(sessionId: Long): Flow<List<SleepSample>>
}
