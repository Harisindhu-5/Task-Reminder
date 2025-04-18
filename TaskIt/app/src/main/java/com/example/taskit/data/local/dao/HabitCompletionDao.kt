package com.example.taskit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taskit.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitCompletionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion): Long
    
    @Update
    suspend fun updateCompletion(completion: HabitCompletion)
    
    @Delete
    suspend fun deleteCompletion(completion: HabitCompletion)
    
    @Query("SELECT * FROM habit_completions WHERE id = :completionId")
    suspend fun getCompletionById(completionId: String): HabitCompletion?
    
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    fun getCompletionsForHabitFlow(habitId: String): Flow<List<HabitCompletion>>
    
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCompletionForDate(habitId: String, date: LocalDate): HabitCompletion?
    
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getCompletionsForDateRangeFlow(habitId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<HabitCompletion>>
    
    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate")
    fun getCompletionCountForPeriod(habitId: String, startDate: LocalDate, endDate: LocalDate): Flow<Int>
    
    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletionForDate(habitId: String, date: LocalDate)
    
    @Query("SELECT * FROM habit_completions ORDER BY date DESC")
    fun getAllCompletionsFlow(): Flow<List<HabitCompletion>>
} 