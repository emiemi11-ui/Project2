package com.vitanova.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vitanova.app.data.local.entity.FitnessActivity
import com.vitanova.app.data.local.entity.GpsPoint
import com.vitanova.app.data.local.entity.StepRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: FitnessActivity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGpsPoint(gpsPoint: GpsPoint): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStepRecord(stepRecord: StepRecord): Long

    @Query("SELECT * FROM fitness_activities WHERE id = :activityId")
    fun getActivityById(activityId: Long): Flow<FitnessActivity?>

    @Query("SELECT * FROM fitness_activities ORDER BY start_time DESC LIMIT :limit")
    fun getRecentActivities(limit: Int = 20): Flow<List<FitnessActivity>>

    @Query("SELECT * FROM gps_points WHERE activity_id = :activityId ORDER BY timestamp ASC")
    fun getGpsPointsByActivity(activityId: Long): Flow<List<GpsPoint>>

    @Query("SELECT COALESCE(SUM(step_count), 0) FROM step_records WHERE date = :today")
    fun getTodaySteps(today: String): Flow<Int>

    @Query("SELECT * FROM step_records WHERE date = :date ORDER BY last_updated DESC LIMIT 1")
    fun getStepsByDate(date: String): Flow<StepRecord?>

    @Query(
        """
        SELECT * FROM step_records
        WHERE date >= :sevenDaysAgo
        ORDER BY date DESC
        """
    )
    fun getStepsLast7Days(sevenDaysAgo: String): Flow<List<StepRecord>>
}
