package com.example.taskit.data.repository

import com.example.taskit.data.local.dao.TaskDao
import com.example.taskit.data.model.Task
import com.example.taskit.data.model.TaskPriority
import com.example.taskit.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

interface TaskRepository {
    suspend fun addTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun getTaskById(taskId: String): Task?
    fun getTaskByIdFlow(taskId: String): Flow<Task?>
    fun getAllTasksFlow(): Flow<List<Task>>
    fun getActiveTasksFlow(excludeStatus: TaskStatus = TaskStatus.COMPLETED): Flow<List<Task>>
    fun getTasksByStatusFlow(status: TaskStatus): Flow<List<Task>>
    fun getTasksByPriorityFlow(priority: TaskPriority): Flow<List<Task>>
    fun getTasksByCategoryFlow(categoryId: String): Flow<List<Task>>
    fun getTasksByDateRangeFlow(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Task>>
    fun getTodayTasksFlow(todayStart: LocalDateTime, todayEnd: LocalDateTime): Flow<List<Task>>
    fun searchTasksFlow(query: String): Flow<List<Task>>
    fun getTaskCountForPeriod(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Int>
    fun getTaskCountByStatusForPeriod(status: TaskStatus, startDate: LocalDateTime, endDate: LocalDateTime): Flow<Int>
    fun getTasksForDay(date: LocalDate): Flow<List<Task>>
}

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    
    override suspend fun addTask(task: Task): Long {
        return taskDao.insertTask(task)
    }
    
    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }
    
    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
    
    override suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)
    }
    
    override fun getTaskByIdFlow(taskId: String): Flow<Task?> {
        return taskDao.getTaskByIdFlow(taskId)
    }
    
    override fun getAllTasksFlow(): Flow<List<Task>> {
        return taskDao.getAllTasksFlow()
    }
    
    override fun getActiveTasksFlow(excludeStatus: TaskStatus): Flow<List<Task>> {
        return taskDao.getActiveTasksFlow(excludeStatus)
    }
    
    override fun getTasksByStatusFlow(status: TaskStatus): Flow<List<Task>> {
        return taskDao.getTasksByStatusFlow(status)
    }
    
    override fun getTasksByPriorityFlow(priority: TaskPriority): Flow<List<Task>> {
        return taskDao.getTasksByPriorityFlow(priority)
    }
    
    override fun getTasksByCategoryFlow(categoryId: String): Flow<List<Task>> {
        return taskDao.getTasksByCategoryFlow(categoryId)
    }
    
    override fun getTasksByDateRangeFlow(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Task>> {
        return taskDao.getTasksByDateRangeFlow(startDate, endDate)
    }
    
    override fun getTodayTasksFlow(todayStart: LocalDateTime, todayEnd: LocalDateTime): Flow<List<Task>> {
        return taskDao.getTodayTasksFlow(todayStart, todayEnd)
    }
    
    override fun searchTasksFlow(query: String): Flow<List<Task>> {
        return taskDao.searchTasksFlow(query)
    }
    
    override fun getTaskCountForPeriod(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Int> {
        return taskDao.getTaskCountForPeriod(startDate, endDate)
    }
    
    override fun getTaskCountByStatusForPeriod(status: TaskStatus, startDate: LocalDateTime, endDate: LocalDateTime): Flow<Int> {
        return taskDao.getTaskCountByStatusForPeriod(status, startDate, endDate)
    }
    
    override fun getTasksForDay(date: LocalDate): Flow<List<Task>> {
        val startOfDay = date.atStartOfDay()
        val endOfDay = date.atTime(LocalTime.MAX)
        return taskDao.getTasksByDateRangeFlow(startOfDay, endOfDay)
    }
} 