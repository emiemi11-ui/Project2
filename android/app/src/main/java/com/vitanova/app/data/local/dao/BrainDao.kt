package com.vitanova.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vitanova.app.data.local.entity.BrainTest
import kotlinx.coroutines.flow.Flow

@Dao
interface BrainDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: BrainTest): Long

    @Query(
        """
        SELECT * FROM brain_tests
        WHERE test_type = :testType
        ORDER BY timestamp DESC
        LIMIT 1
        """
    )
    fun getLatestByType(testType: String): Flow<BrainTest?>

    @Query(
        """
        SELECT * FROM brain_tests
        WHERE test_type = :testType AND date >= :thirtyDaysAgo
        ORDER BY timestamp DESC
        """
    )
    fun getLast30DaysByType(testType: String, thirtyDaysAgo: String): Flow<List<BrainTest>>

    @Query(
        """
        SELECT COALESCE(AVG(score), 0.0)
        FROM brain_tests
        WHERE date = (SELECT MAX(date) FROM brain_tests)
        """
    )
    fun getGlobalScore(): Flow<Float>
}
