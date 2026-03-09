package com.vitanova.app.data.repository

import com.vitanova.app.data.local.dao.SleepDao
import com.vitanova.app.data.local.entity.SleepSample
import com.vitanova.app.data.local.entity.SleepSession
import com.vitanova.app.util.SleepAnalysis
import com.vitanova.app.util.SleepAnalyzer
import com.vitanova.app.util.SleepSampleInput
import kotlinx.coroutines.flow.Flow

class SleepRepository(private val sleepDao: SleepDao) {

    suspend fun saveSleepSession(session: SleepSession): Long {
        return sleepDao.insert(session)
    }

    suspend fun saveSleepSample(sample: SleepSample): Long {
        return sleepDao.insert(sample)
    }

    fun getLatestSession(): Flow<SleepSession?> {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % 86_400_000L)
        val endOfDay = startOfDay + 86_400_000L
        return sleepDao.getSessionByDate(startOfDay, endOfDay)
    }

    fun getLast7Sessions(): Flow<List<SleepSession>> {
        return sleepDao.getLast7Sessions()
    }

    fun getLast30Sessions(): Flow<List<SleepSession>> {
        return sleepDao.getLast30Sessions()
    }

    fun getSamples(sessionId: Long): Flow<List<SleepSample>> {
        return sleepDao.getSamplesBySessionId(sessionId)
    }

    fun analyzeSleep(samples: List<SleepSampleInput>): SleepAnalysis? {
        return SleepAnalyzer.analyze(samples)
    }
}
