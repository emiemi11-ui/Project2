package com.vitanova.app.data.repository

import com.vitanova.app.data.local.dao.FitnessDao
import com.vitanova.app.data.local.entity.FitnessActivity
import com.vitanova.app.data.local.entity.GpsPoint
import com.vitanova.app.data.local.entity.StepRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FitnessRepository(private val fitnessDao: FitnessDao) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun saveActivity(activity: FitnessActivity): Long {
        return fitnessDao.insertActivity(activity)
    }

    suspend fun saveGpsPoint(point: GpsPoint): Long {
        return fitnessDao.insertGpsPoint(point)
    }

    fun getRecentActivities(): Flow<List<FitnessActivity>> {
        return fitnessDao.getRecentActivities()
    }

    fun getActivityWithPoints(id: Long): Flow<Pair<FitnessActivity?, List<GpsPoint>>> {
        return combine(
            fitnessDao.getActivityById(id),
            fitnessDao.getGpsPointsByActivity(id)
        ) { activity, points ->
            Pair(activity, points)
        }
    }

    suspend fun saveSteps(record: StepRecord): Long {
        return fitnessDao.insertStepRecord(record)
    }

    fun getTodaySteps(): Flow<Int> {
        val today = LocalDate.now().format(dateFormatter)
        return fitnessDao.getTodaySteps(today)
    }

    fun getStepsLast7Days(): Flow<List<StepRecord>> {
        val sevenDaysAgo = LocalDate.now().minusDays(7).format(dateFormatter)
        return fitnessDao.getStepsLast7Days(sevenDaysAgo)
    }
}
