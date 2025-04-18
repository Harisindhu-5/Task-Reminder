package com.example.taskit.data.repository

import com.example.taskit.data.local.dao.GoalDao
import com.example.taskit.data.model.Goal
import com.example.taskit.data.model.GoalStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

interface GoalRepository {
    suspend fun addGoal(goal: Goal): Long
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(goal: Goal)
    suspend fun getGoalById(goalId: String): Goal?
    fun getGoalByIdFlow(goalId: String): Flow<Goal?>
    fun getGoalsByStatusFlow(status: GoalStatus): Flow<List<Goal>>
    fun getAllGoalsFlow(): Flow<List<Goal>>
    fun getGoalsByDateRangeFlow(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Goal>>
    fun searchGoalsFlow(query: String): Flow<List<Goal>>
    suspend fun updateGoalProgress(goalId: String, progress: Int)
    suspend fun updateGoalStatus(goalId: String, status: GoalStatus, completionDate: LocalDateTime?)
}

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
) : GoalRepository {
    
    override suspend fun addGoal(goal: Goal): Long {
        return goalDao.insertGoal(goal)
    }
    
    override suspend fun updateGoal(goal: Goal) {
        goalDao.updateGoal(goal)
    }
    
    override suspend fun deleteGoal(goal: Goal) {
        goalDao.deleteGoal(goal)
    }
    
    override suspend fun getGoalById(goalId: String): Goal? {
        return goalDao.getGoalById(goalId)
    }
    
    override fun getGoalByIdFlow(goalId: String): Flow<Goal?> {
        return goalDao.getGoalByIdFlow(goalId)
    }
    
    override fun getGoalsByStatusFlow(status: GoalStatus): Flow<List<Goal>> {
        return goalDao.getGoalsByStatusFlow(status)
    }
    
    override fun getAllGoalsFlow(): Flow<List<Goal>> {
        return goalDao.getAllGoalsFlow()
    }
    
    override fun getGoalsByDateRangeFlow(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Goal>> {
        return goalDao.getGoalsByDateRangeFlow(startDate, endDate)
    }
    
    override fun searchGoalsFlow(query: String): Flow<List<Goal>> {
        return goalDao.searchGoalsFlow(query)
    }
    
    override suspend fun updateGoalProgress(goalId: String, progress: Int) {
        goalDao.updateGoalProgress(goalId, progress)
    }
    
    override suspend fun updateGoalStatus(goalId: String, status: GoalStatus, completionDate: LocalDateTime?) {
        goalDao.updateGoalStatus(goalId, status, completionDate)
    }
} 