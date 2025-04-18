package com.example.taskit.data.repository

import com.example.taskit.data.local.dao.HabitCompletionDao
import com.example.taskit.data.local.dao.HabitDao
import com.example.taskit.data.model.Habit
import com.example.taskit.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

interface HabitRepository {
    // Habit operations
    suspend fun addHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun getHabitById(habitId: String): Habit?
    fun getHabitByIdFlow(habitId: String): Flow<Habit?>
    fun getActiveHabitsFlow(): Flow<List<Habit>>
    fun getAllHabitsFlow(): Flow<List<Habit>>
    fun getHabitsByGoalFlow(goalId: String): Flow<List<Habit>>
    fun searchHabitsFlow(query: String): Flow<List<Habit>>
    suspend fun updateStreak(habitId: String, newStreak: Int)
    suspend fun updateBestStreak(habitId: String, newBestStreak: Int)
    suspend fun incrementCompletions(habitId: String, completionDate: LocalDateTime)
    
    // Habit completion operations
    suspend fun addCompletion(completion: HabitCompletion): Long
    suspend fun updateCompletion(completion: HabitCompletion)
    suspend fun deleteCompletion(completion: HabitCompletion)
    suspend fun getCompletionById(completionId: String): HabitCompletion?
    fun getCompletionsForHabitFlow(habitId: String): Flow<List<HabitCompletion>>
    suspend fun getCompletionForDate(habitId: String, date: LocalDate): HabitCompletion?
    fun getCompletionsForDateRangeFlow(habitId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<HabitCompletion>>
    fun getCompletionCountForPeriod(habitId: String, startDate: LocalDate, endDate: LocalDate): Flow<Int>
    suspend fun deleteCompletionForDate(habitId: String, date: LocalDate)
    
    // Combined operations
    fun getAllHabitsWithCompletionsFlow(): Flow<List<HabitWithCompletions>>
    
    data class HabitWithCompletions(
        val habit: Habit,
        val completions: List<HabitCompletion>
    )
}

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitCompletionDao: HabitCompletionDao
) : HabitRepository {
    
    // Habit operations
    override suspend fun addHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit)
    }
    
    override suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }
    
    override suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }
    
    override suspend fun getHabitById(habitId: String): Habit? {
        return habitDao.getHabitById(habitId)
    }
    
    override fun getHabitByIdFlow(habitId: String): Flow<Habit?> {
        return habitDao.getHabitByIdFlow(habitId)
    }
    
    override fun getActiveHabitsFlow(): Flow<List<Habit>> {
        return habitDao.getActiveHabitsFlow()
    }
    
    override fun getAllHabitsFlow(): Flow<List<Habit>> {
        return habitDao.getAllHabitsFlow()
    }
    
    override fun getHabitsByGoalFlow(goalId: String): Flow<List<Habit>> {
        return habitDao.getHabitsByGoalFlow(goalId)
    }
    
    override fun searchHabitsFlow(query: String): Flow<List<Habit>> {
        return habitDao.searchHabitsFlow(query)
    }
    
    override suspend fun updateStreak(habitId: String, newStreak: Int) {
        habitDao.updateStreak(habitId, newStreak)
    }
    
    override suspend fun updateBestStreak(habitId: String, newBestStreak: Int) {
        habitDao.updateBestStreak(habitId, newBestStreak)
    }
    
    override suspend fun incrementCompletions(habitId: String, completionDate: LocalDateTime) {
        habitDao.incrementCompletions(habitId, completionDate)
    }
    
    // Habit completion operations
    override suspend fun addCompletion(completion: HabitCompletion): Long {
        return habitCompletionDao.insertCompletion(completion)
    }
    
    override suspend fun updateCompletion(completion: HabitCompletion) {
        habitCompletionDao.updateCompletion(completion)
    }
    
    override suspend fun deleteCompletion(completion: HabitCompletion) {
        habitCompletionDao.deleteCompletion(completion)
    }
    
    override suspend fun getCompletionById(completionId: String): HabitCompletion? {
        return habitCompletionDao.getCompletionById(completionId)
    }
    
    override fun getCompletionsForHabitFlow(habitId: String): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getCompletionsForHabitFlow(habitId)
    }
    
    override suspend fun getCompletionForDate(habitId: String, date: LocalDate): HabitCompletion? {
        return habitCompletionDao.getCompletionForDate(habitId, date)
    }
    
    override fun getCompletionsForDateRangeFlow(habitId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getCompletionsForDateRangeFlow(habitId, startDate, endDate)
    }
    
    override fun getCompletionCountForPeriod(habitId: String, startDate: LocalDate, endDate: LocalDate): Flow<Int> {
        return habitCompletionDao.getCompletionCountForPeriod(habitId, startDate, endDate)
    }
    
    override suspend fun deleteCompletionForDate(habitId: String, date: LocalDate) {
        habitCompletionDao.deleteCompletionForDate(habitId, date)
    }
    
    // Combined operations
    override fun getAllHabitsWithCompletionsFlow(): Flow<List<HabitRepository.HabitWithCompletions>> {
        return combine(
            habitDao.getAllHabitsFlow(),
            habitCompletionDao.getAllCompletionsFlow()
        ) { habits, allCompletions ->
            habits.map { habit ->
                val completionsForHabit = allCompletions.filter { it.habitId == habit.id }
                HabitRepository.HabitWithCompletions(habit, completionsForHabit)
            }
        }
    }
} 