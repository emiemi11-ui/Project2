package com.vitanova.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vitanova.app.data.local.entity.HrvReading
import kotlinx.coroutines.flow.Flow

@Dao
interface HrvDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: HrvReading): Long

    @Query("SELECT * FROM hrv_readings ORDER BY timestamp DESC LIMIT 1")
    fun getLatest(): Flow<HrvReading?>

    @Query(
        """
        SELECT * FROM hrv_readings
        WHERE date >= :sevenDaysAgo
        ORDER BY timestamp DESC
        """
    )
    fun getLast7Days(sevenDaysAgo: String): Flow<List<HrvReading>>

    @Query(
        """
        SELECT * FROM hrv_readings
        WHERE date >= :thirtyDaysAgo
        ORDER BY timestamp DESC
        """
    )
    fun getLast30Days(thirtyDaysAgo: String): Flow<List<HrvReading>>

    @Query(
        """
        SELECT * FROM hrv_readings
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY timestamp ASC
        """
    )
    fun getByDateRange(startDate: String, endDate: String): Flow<List<HrvReading>>
}
