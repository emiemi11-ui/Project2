package com.vitanova.app.data.repository

import com.vitanova.app.data.local.dao.FocusDao
import com.vitanova.app.data.local.entity.AppUsage
import com.vitanova.app.data.local.entity.FocusSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FocusRepository(private val focusDao: FocusDao) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun saveSession(session: FocusSession): Long {
        return focusDao.insertSession(session)
    }

    suspend fun saveAppUsage(usage: AppUsage): Long {
        return focusDao.insertAppUsage(usage)
    }

    fun getTodayScreenTime(): Flow<Int> {
        val today = LocalDate.now().format(dateFormatter)
        return focusDao.getTodayScreenTime(today)
    }

    fun getTopApps(date: String): Flow<List<AppUsage>> {
        return focusDao.getTopAppsByDate(date)
    }

    fun getTodayFocusSessions(): Flow<List<FocusSession>> {
        val today = LocalDate.now().format(dateFormatter)
        return focusDao.getTodaySessions(today)
    }
}
