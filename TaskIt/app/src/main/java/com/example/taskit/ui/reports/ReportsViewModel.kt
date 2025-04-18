package com.example.taskit.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskit.data.model.Habit
import com.example.taskit.data.model.Task
import com.example.taskit.data.model.TaskStatus
import com.example.taskit.data.repository.HabitRepository
import com.example.taskit.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

data class ReportsUiState(
    val isLoading: Boolean = false,
    val timePeriod: TimePeriod = TimePeriod.WEEK,
    val taskStats: TaskStats = TaskStats(),
    val habitStats: HabitStats = HabitStats(),
    val selectedDate: LocalDate = LocalDate.now(),
    val errorMessage: String? = null
)

data class TaskStats(
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val overDueCount: Int = 0,
    val completionRate: Float = 0f,
    val averageCompletionTime: Double = 0.0,
    val tasksByCategory: Map<String?, Int> = emptyMap(),
    val tasksByPriority: Map<String, Int> = emptyMap(),
    val completedTasksByDay: Map<LocalDate, Int> = emptyMap()
)

data class HabitStats(
    val totalHabits: Int = 0,
    val dailyHabits: Int = 0,
    val weeklyHabits: Int = 0,
    val monthlyHabits: Int = 0,
    val averageCompletion: Float = 0f,
    val bestStreak: Int = 0,
    val currentStreak: Int = 0,
    val completionsByDay: Map<LocalDate, Int> = emptyMap()
)

enum class TimePeriod {
    DAY, WEEK, MONTH, YEAR
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _selectedTimePeriod = MutableStateFlow(TimePeriod.WEEK)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val taskStats = combine(
        taskRepository.getAllTasksFlow(),
        _selectedTimePeriod,
        _selectedDate
    ) { tasks, period, date ->
        calculateTaskStats(tasks, period, date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskStats()
    )
    
    private val habitStats = combine(
        habitRepository.getAllHabitsWithCompletionsFlow(),
        _selectedTimePeriod,
        _selectedDate
    ) { habitsWithCompletions, period, date ->
        calculateHabitStats(habitsWithCompletions, period, date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitStats()
    )
    
    val uiState = combine(
        taskStats,
        habitStats,
        _selectedTimePeriod,
        _selectedDate,
        _isLoading,
        _errorMessage
    ) { flowValues ->
        val taskStats = flowValues[0] as TaskStats
        val habitStats = flowValues[1] as HabitStats
        val period = flowValues[2] as TimePeriod
        val date = flowValues[3] as LocalDate
        val isLoading = flowValues[4] as Boolean
        val errorMessage = flowValues[5] as String?
        
        ReportsUiState(
            isLoading = isLoading,
            timePeriod = period,
            taskStats = taskStats,
            habitStats = habitStats,
            selectedDate = date,
            errorMessage = errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState(isLoading = true)
    )
    
    init {
        viewModelScope.launch {
            _isLoading.value = false
        }
    }
    
    fun setTimePeriod(period: TimePeriod) {
        _selectedTimePeriod.value = period
    }
    
    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    private fun calculateTaskStats(tasks: List<Task>, period: TimePeriod, selectedDate: LocalDate): TaskStats {
        val filteredTasks = filterTasksByPeriod(tasks, period, selectedDate)
        
        val completed = filteredTasks.filter { it.status == TaskStatus.COMPLETED }
        val pending = filteredTasks.filter { it.status == TaskStatus.PENDING }
        val overdue = pending.filter { it.dueDate?.isBefore(LocalDateTime.now()) == true }
        
        val completionRate = if (filteredTasks.isNotEmpty()) {
            completed.size.toFloat() / filteredTasks.size.toFloat()
        } else {
            0f
        }
        
        val avgCompletionTime = completed
            .filter { it.completedDate != null }
            .map { ChronoUnit.MINUTES.between(it.createdAt, it.completedDate).toDouble() }
            .takeIf { it.isNotEmpty() }
            ?.average() ?: 0.0
        
        val tasksByCategory = filteredTasks.groupBy { it.categoryId }.mapValues { it.value.size }
        val tasksByPriority = filteredTasks.groupBy { it.priority.name }.mapValues { it.value.size }
        
        val completedByDay = completed
            .filter { it.completedDate != null }
            .mapNotNull { task -> task.completedDate?.let { date -> date.toLocalDate() to task } }
            .groupBy { it.first }
            .mapValues { it.value.size }
        
        return TaskStats(
            completedCount = completed.size,
            pendingCount = pending.size,
            overDueCount = overdue.size,
            completionRate = completionRate,
            averageCompletionTime = avgCompletionTime,
            tasksByCategory = tasksByCategory,
            tasksByPriority = tasksByPriority,
            completedTasksByDay = completedByDay
        )
    }
    
    private fun calculateHabitStats(
        habitsWithCompletions: List<HabitRepository.HabitWithCompletions>,
        period: TimePeriod,
        selectedDate: LocalDate
    ): HabitStats {
        val dateRange = getDateRangeForPeriod(period, selectedDate)
        val startDate = dateRange.first
        val endDate = dateRange.second
        
        val habits = habitsWithCompletions.map { it.habit }
        val allCompletions = habitsWithCompletions.flatMap { it.completions }
        
        // Filter completions within the date range
        val completions = allCompletions.filter { 
            val completionDate = it.date
            completionDate in startDate..endDate 
        }
        
        val dailyHabits = habits.count { it.frequency == Habit.Frequency.DAILY }
        val weeklyHabits = habits.count { it.frequency == Habit.Frequency.WEEKLY }
        val monthlyHabits = habits.count { it.frequency == Habit.Frequency.MONTHLY }
        
        val completionsByDay = completions
            .groupBy { it.date }
            .mapValues { it.value.size }
        
        // Calculate average completion rate
        val daysInPeriod = ChronoUnit.DAYS.between(startDate, endDate) + 1
        val expectedCompletions = when {
            habits.isEmpty() -> 0 // No habits means no expected completions
            else -> dailyHabits * daysInPeriod +
                weeklyHabits * (daysInPeriod / 7).coerceAtLeast(1) +
                monthlyHabits * (daysInPeriod / 30).coerceAtLeast(1)
        }
        
        val avgCompletion = if (expectedCompletions > 0) {
            (completions.size.toFloat() / expectedCompletions.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        
        // Calculate streaks
        val streaks = habitsWithCompletions.map { calculateStreak(it) }
        val bestStreak = streaks.maxOrNull() ?: 0
        val currentStreak = calculateCurrentStreak(habitsWithCompletions)
        
        return HabitStats(
            totalHabits = habits.size,
            dailyHabits = dailyHabits,
            weeklyHabits = weeklyHabits,
            monthlyHabits = monthlyHabits,
            averageCompletion = avgCompletion,
            bestStreak = bestStreak,
            currentStreak = currentStreak,
            completionsByDay = completionsByDay
        )
    }
    
    private fun filterTasksByPeriod(tasks: List<Task>, period: TimePeriod, selectedDate: LocalDate): List<Task> {
        val (startDate, endDate) = getDateRangeForPeriod(period, selectedDate)
        val startDateTime = startDate.atStartOfDay()
        val endDateTime = endDate.atTime(23, 59, 59)
        
        return tasks.filter { task ->
            // Include tasks that have a due date in the period
            val dueDateInRange = task.dueDate?.let { it in startDateTime..endDateTime } ?: false
            
            // Include tasks that were created in the period
            val createdInRange = task.createdAt in startDateTime..endDateTime
            
            // Include tasks that were completed in the period
            val completedInRange = task.completedDate?.let { it in startDateTime..endDateTime } ?: false
            
            // Include tasks that were updated in the period
            val updatedInRange = task.updatedAt in startDateTime..endDateTime
            
            dueDateInRange || createdInRange || completedInRange || updatedInRange
        }
    }
    
    private fun getDateRangeForPeriod(period: TimePeriod, selectedDate: LocalDate): Pair<LocalDate, LocalDate> {
        return when (period) {
            TimePeriod.DAY -> {
                Pair(selectedDate, selectedDate)
            }
            TimePeriod.WEEK -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                val firstDayOfWeek = selectedDate.with(weekFields.dayOfWeek(), 1)
                val lastDayOfWeek = firstDayOfWeek.plusDays(6)
                Pair(firstDayOfWeek, lastDayOfWeek)
            }
            TimePeriod.MONTH -> {
                val firstDayOfMonth = selectedDate.withDayOfMonth(1)
                val lastDayOfMonth = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth())
                Pair(firstDayOfMonth, lastDayOfMonth)
            }
            TimePeriod.YEAR -> {
                val firstDayOfYear = selectedDate.withDayOfYear(1)
                val lastDayOfYear = selectedDate.withDayOfYear(selectedDate.lengthOfYear())
                Pair(firstDayOfYear, lastDayOfYear)
            }
        }
    }
    
    private fun calculateStreak(habitWithCompletions: HabitRepository.HabitWithCompletions): Int {
        val completions = habitWithCompletions.completions
            .map { it.date }
            .sorted()
        
        if (completions.isEmpty()) {
            return 0
        }
        
        var maxStreak = 1
        var currentStreak = 1
        
        for (i in 1 until completions.size) {
            val previous = completions[i-1]
            val current = completions[i]
            
            when (habitWithCompletions.habit.frequency) {
                Habit.Frequency.DAILY -> {
                    if (current.isEqual(previous.plusDays(1))) {
                        currentStreak++
                    } else {
                        currentStreak = 1
                    }
                }
                Habit.Frequency.WEEKLY -> {
                    if (current.isEqual(previous.plusWeeks(1))) {
                        currentStreak++
                    } else {
                        currentStreak = 1
                    }
                }
                Habit.Frequency.MONTHLY -> {
                    if (current.isEqual(previous.plusMonths(1))) {
                        currentStreak++
                    } else {
                        currentStreak = 1
                    }
                }
                Habit.Frequency.CUSTOM -> {
                    currentStreak = 1 // For custom frequency, streaks don't make much sense
                }
            }
            
            maxStreak = maxOf(maxStreak, currentStreak)
        }
        
        return maxStreak
    }
    
    private fun calculateCurrentStreak(habitsWithCompletions: List<HabitRepository.HabitWithCompletions>): Int {
        if (habitsWithCompletions.isEmpty()) {
            return 0
        }
        
        // For simplicity, focus on daily habits for current streak
        val dailyHabits = habitsWithCompletions.filter { it.habit.frequency == Habit.Frequency.DAILY }
        if (dailyHabits.isEmpty()) {
            return 0
        }
        
        var currentDate = LocalDate.now()
        var streakCount = 0
        
        while (true) {
            val allCompletedForDay = dailyHabits.all { habit ->
                habit.completions.any { it.date.isEqual(currentDate) }
            }
            
            if (!allCompletedForDay) {
                break
            }
            
            streakCount++
            currentDate = currentDate.minusDays(1)
        }
        
        return streakCount
    }
} 