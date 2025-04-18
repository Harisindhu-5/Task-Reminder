package com.example.taskit.data.repository

import com.example.taskit.data.local.dao.PomodoroSessionDao
import com.example.taskit.data.model.PomodoroSession
import com.example.taskit.data.model.PomodoroStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

interface PomodoroRepository {
    suspend fun addSession(session: PomodoroSession): Long
    suspend fun updateSession(session: PomodoroSession)
    suspend fun deleteSession(session: PomodoroSession)
    suspend fun getSessionById(sessionId: String): PomodoroSession?
    fun getSessionByIdFlow(sessionId: String): Flow<PomodoroSession?>
    fun getAllSessionsFlow(): Flow<List<PomodoroSession>>
    fun getSessionsForTaskFlow(taskId: String): Flow<List<PomodoroSession>>
    fun getSessionsByDateRangeFlow(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<PomodoroSession>>
    fun getSessionsByStatusFlow(status: PomodoroStatus): Flow<List<PomodoroSession>>
    fun getTotalFocusTimeForPeriod(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Long?>
    fun getTotalFocusTimeForTask(taskId: String): Flow<Long?>
    suspend fun updateSessionStatus(sessionId: String, status: PomodoroStatus)
    suspend fun incrementFocusTime(sessionId: String, additionalTime: Long)
}

@Singleton
class PomodoroRepositoryImpl @Inject constructor(
    private val pomodoroSessionDao: PomodoroSessionDao
) : PomodoroRepository {
    
    override suspend fun addSession(session: PomodoroSession): Long {
        return pomodoroSessionDao.insertSession(session)
    }
    
    override suspend fun updateSession(session: PomodoroSession) {
        pomodoroSessionDao.updateSession(session)
    }
    
    override suspend fun deleteSession(session: PomodoroSession) {
        pomodoroSessionDao.deleteSession(session)
    }
    
    override suspend fun getSessionById(sessionId: String): PomodoroSession? {
        return pomodoroSessionDao.getSessionById(sessionId)
    }
    
    override fun getSessionByIdFlow(sessionId: String): Flow<PomodoroSession?> {
        return pomodoroSessionDao.getSessionByIdFlow(sessionId)
    }
    
    override fun getAllSessionsFlow(): Flow<List<PomodoroSession>> {
        return pomodoroSessionDao.getAllSessionsFlow()
    }
    
    override fun getSessionsForTaskFlow(taskId: String): Flow<List<PomodoroSession>> {
        return pomodoroSessionDao.getSessionsForTaskFlow(taskId)
    }
    
    override fun getSessionsByDateRangeFlow(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<PomodoroSession>> {
        return pomodoroSessionDao.getSessionsByDateRangeFlow(startDate, endDate)
    }
    
    override fun getSessionsByStatusFlow(status: PomodoroStatus): Flow<List<PomodoroSession>> {
        return pomodoroSessionDao.getSessionsByStatusFlow(status)
    }
    
    override fun getTotalFocusTimeForPeriod(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Long?> {
        return pomodoroSessionDao.getTotalFocusTimeForPeriod(startDate, endDate)
    }
    
    override fun getTotalFocusTimeForTask(taskId: String): Flow<Long?> {
        return pomodoroSessionDao.getTotalFocusTimeForTask(taskId)
    }
    
    override suspend fun updateSessionStatus(sessionId: String, status: PomodoroStatus) {
        pomodoroSessionDao.updateSessionStatus(sessionId, status)
    }
    
    override suspend fun incrementFocusTime(sessionId: String, additionalTime: Long) {
        pomodoroSessionDao.incrementFocusTime(sessionId, additionalTime)
    }
} 