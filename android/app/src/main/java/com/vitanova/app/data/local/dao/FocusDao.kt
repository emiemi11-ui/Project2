package com.vitanova.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vitanova.app.data.local.entity.AppUsage
import com.vitanova.app.data.local.entity.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsage(appUsage: AppUsage): Long

    @Query("SELECT * FROM focus_sessions WHERE date = :today ORDER BY start_time DESC")
    fun getTodaySessions(today: String): Flow<List<FocusSession>>

    @Query("SELECT * FROM app_usage WHERE date = :date ORDER BY usage_minutes DESC")
    fun getAppUsageByDate(date: String): Flow<List<AppUsage>>

    @Query("SELECT COALESCE(SUM(usage_minutes), 0) FROM app_usage WHERE date = :today")
    fun getTodayScreenTime(today: String): Flow<Int>

    @Query(
        """
        SELECT * FROM app_usage
        WHERE date = :date
        ORDER BY usage_minutes DESC
        LIMIT :limit
        """
    )
    fun getTopAppsByDate(date: String, limit: Int = 10): Flow<List<AppUsage>>
}
