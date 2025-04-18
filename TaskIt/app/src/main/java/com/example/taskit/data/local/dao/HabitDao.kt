package com.example.taskit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.taskit.data.model.Habit
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long
    
    @Update
    suspend fun updateHabit(habit: Habit)
    
    @Delete
    suspend fun deleteHabit(habit: Habit)
    
    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: String): Habit?
    
    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitByIdFlow(habitId: String): Flow<Habit?>
    
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveHabitsFlow(): Flow<List<Habit>>
    
    @Query("SELECT * FROM habits ORDER BY name ASC")
    fun getAllHabitsFlow(): Flow<List<Habit>>
    
    @Query("SELECT * FROM habits WHERE goalId = :goalId ORDER BY name ASC")
    fun getHabitsByGoalFlow(goalId: String): Flow<List<Habit>>
    
    @Query("""
        SELECT * FROM habits 
        WHERE name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchHabitsFlow(query: String): Flow<List<Habit>>
    
    @Query("UPDATE habits SET currentStreak = :newStreak WHERE id = :habitId")
    suspend fun updateStreak(habitId: String, newStreak: Int)
    
    @Query("UPDATE habits SET bestStreak = :newBestStreak WHERE id = :habitId AND bestStreak < :newBestStreak")
    suspend fun updateBestStreak(habitId: String, newBestStreak: Int)
    
    @Query("UPDATE habits SET daysCompleted = daysCompleted + 1, lastCompletedDate = :completionDate WHERE id = :habitId")
    suspend fun incrementCompletions(habitId: String, completionDate: LocalDateTime)
} 