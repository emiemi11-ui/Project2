package com.vitanova.app.data.repository

import com.vitanova.app.data.local.dao.HrvDao
import com.vitanova.app.data.local.entity.HrvReading
import com.vitanova.app.util.HrvCalculator
import com.vitanova.app.util.HrvResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HrvRepository(private val hrvDao: HrvDao) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun saveReading(reading: HrvReading): Long {
        return hrvDao.insert(reading)
    }

    fun getLatest(): Flow<HrvReading?> {
        return hrvDao.getLatest()
    }

    fun getLast7Days(): Flow<List<HrvReading>> {
        val sevenDaysAgo = LocalDate.now().minusDays(7).format(dateFormatter)
        return hrvDao.getLast7Days(sevenDaysAgo)
    }

    fun getLast30Days(): Flow<List<HrvReading>> {
        val thirtyDaysAgo = LocalDate.now().minusDays(30).format(dateFormatter)
        return hrvDao.getLast30Days(thirtyDaysAgo)
    }

    fun calculateFromRrIntervals(intervals: List<Double>): HrvResult? {
        return HrvCalculator.calculate(intervals)
    }
}
