package com.example.taskit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taskit.data.model.Task
import com.example.taskit.data.model.TaskPriority
import com.example.taskit.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long
    
    @Update
    suspend fun updateTask(task: Task)
    
    @Delete
    suspend fun deleteTask(task: Task)
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): Task?
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTaskByIdFlow(taskId: String): Flow<Task?>
    
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasksFlow(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE status != :excludeStatus ORDER BY dueDate ASC")
    fun getActiveTasksFlow(excludeStatus: TaskStatus = TaskStatus.COMPLETED): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY dueDate ASC")
    fun getTasksByStatusFlow(status: TaskStatus): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY dueDate ASC")
    fun getTasksByPriorityFlow(priority: TaskPriority): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId ORDER BY dueDate ASC")
    fun getTasksByCategoryFlow(categoryId: String): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    fun getTasksByDateRangeFlow(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE dueDate >= :todayStart AND dueDate <= :todayEnd ORDER BY dueDate ASC")
    fun getTodayTasksFlow(todayStart: LocalDateTime, todayEnd: LocalDateTime): Flow<List<Task>>
    
    @Query("""
        SELECT * FROM tasks 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY dueDate ASC
    """)
    fun searchTasksFlow(query: String): Flow<List<Task>>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE dueDate BETWEEN :startDate AND :endDate")
    fun getTaskCountForPeriod(startDate: LocalDateTime, endDate: LocalDateTime): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE status = :status AND dueDate BETWEEN :startDate AND :endDate")
    fun getTaskCountByStatusForPeriod(status: TaskStatus, startDate: LocalDateTime, endDate: LocalDateTime): Flow<Int>
} 