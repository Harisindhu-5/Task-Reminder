package com.example.taskit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taskit.data.model.PomodoroSession
import com.example.taskit.data.model.PomodoroStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface PomodoroSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSession): Long
    
    @Update
    suspend fun updateSession(session: PomodoroSession)
    
    @Delete
    suspend fun deleteSession(session: PomodoroSession)
    
    @Query("SELECT * FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): PomodoroSession?
    
    @Query("SELECT * FROM pomodoro_sessions WHERE id = :sessionId")
    fun getSessionByIdFlow(sessionId: String): Flow<PomodoroSession?>
    
    @Query("SELECT * FROM pomodoro_sessions ORDER BY dateCreated DESC")
    fun getAllSessionsFlow(): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId ORDER BY dateCreated DESC")
    fun getSessionsForTaskFlow(taskId: String): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getSessionsByDateRangeFlow(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE currentStatus = :status ORDER BY dateCreated DESC")
    fun getSessionsByStatusFlow(status: PomodoroStatus): Flow<List<PomodoroSession>>
    
    @Query("SELECT SUM(totalFocusTime) FROM pomodoro_sessions WHERE startTime BETWEEN :startDate AND :endDate")
    fun getTotalFocusTimeForPeriod(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Long?>
    
    @Query("SELECT SUM(totalFocusTime) FROM pomodoro_sessions WHERE taskId = :taskId")
    fun getTotalFocusTimeForTask(taskId: String): Flow<Long?>
    
    @Query("UPDATE pomodoro_sessions SET currentStatus = :status WHERE id = :sessionId")
    suspend fun updateSessionStatus(sessionId: String, status: PomodoroStatus)
    
    @Query("UPDATE pomodoro_sessions SET totalFocusTime = totalFocusTime + :additionalTime WHERE id = :sessionId")
    suspend fun incrementFocusTime(sessionId: String, additionalTime: Long)
} 