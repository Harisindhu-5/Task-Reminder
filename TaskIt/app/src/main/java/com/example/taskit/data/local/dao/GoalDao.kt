package com.example.taskit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taskit.data.model.Goal
import com.example.taskit.data.model.GoalStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long
    
    @Update
    suspend fun updateGoal(goal: Goal)
    
    @Delete
    suspend fun deleteGoal(goal: Goal)
    
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: String): Goal?
    
    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalByIdFlow(goalId: String): Flow<Goal?>
    
    @Query("SELECT * FROM goals WHERE status = :status ORDER BY targetDate ASC")
    fun getGoalsByStatusFlow(status: GoalStatus): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals ORDER BY targetDate ASC")
    fun getAllGoalsFlow(): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals WHERE targetDate BETWEEN :startDate AND :endDate ORDER BY targetDate ASC")
    fun getGoalsByDateRangeFlow(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Goal>>
    
    @Query("""
        SELECT * FROM goals 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY targetDate ASC
    """)
    fun searchGoalsFlow(query: String): Flow<List<Goal>>
    
    @Query("UPDATE goals SET progress = :progress WHERE id = :goalId")
    suspend fun updateGoalProgress(goalId: String, progress: Int)
    
    @Query("UPDATE goals SET status = :status, dateCompleted = :completionDate WHERE id = :goalId")
    suspend fun updateGoalStatus(goalId: String, status: GoalStatus, completionDate: LocalDateTime?)
} 