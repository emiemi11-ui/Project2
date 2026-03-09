package com.vitanova.app.data.repository

import com.vitanova.app.data.local.dao.BrainDao
import com.vitanova.app.data.local.entity.BrainTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BrainRepository(private val brainDao: BrainDao) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    companion object {
        val TEST_TYPES = listOf("reaction_time", "memory", "attention", "stroop")
    }

    suspend fun saveTestResult(test: BrainTest): Long {
        return brainDao.insertTest(test)
    }

    fun getLatestScores(): Flow<Map<String, Float>> {
        val flows = TEST_TYPES.map { type ->
            brainDao.getLatestByType(type)
        }
        return combine(flows) { results ->
            val scoreMap = mutableMapOf<String, Float>()
            results.forEachIndexed { index, test ->
                if (test != null) {
                    scoreMap[TEST_TYPES[index]] = test.score
                }
            }
            scoreMap.toMap()
        }
    }

    fun getScoreHistory(type: String, days: Int): Flow<List<BrainTest>> {
        val startDate = LocalDate.now().minusDays(days.toLong()).format(dateFormatter)
        return brainDao.getLast30DaysByType(type, startDate)
    }
}
