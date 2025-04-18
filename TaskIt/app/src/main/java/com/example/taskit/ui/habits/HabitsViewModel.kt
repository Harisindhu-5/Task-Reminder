package com.example.taskit.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskit.data.model.Habit
import com.example.taskit.data.model.HabitCompletion
import com.example.taskit.data.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class HabitsUiState(
    val habits: List<HabitWithCompletions> = emptyList(),
    val filteredHabits: List<HabitWithCompletions> = emptyList(),
    val isLoading: Boolean = false,
    val selectedFilter: HabitFilter = HabitFilter.ALL,
    val selectedDate: LocalDate = LocalDate.now(),
    val errorMessage: String? = null
)

data class HabitWithCompletions(
    val habit: Habit,
    val completionsForDate: List<HabitCompletion> = emptyList(),
    val completedToday: Boolean = false,
    val streak: Int = 0,
    val completion: Float = 0f
)

enum class HabitFilter {
    ALL, DAILY, WEEKLY, COMPLETED, UNCOMPLETED
}

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(HabitFilter.ALL)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val habitsWithCompletions = habitRepository.getAllHabitsWithCompletionsFlow()
        .map { repoHabits ->
            repoHabits.map { repoHabit ->
                val date = _selectedDate.value
                val completionsForDate = repoHabit.completions.filter { 
                    it.date.isEqual(date)
                }
                val completedToday = completionsForDate.isNotEmpty()
                val streak = calculateStreak(repoHabit)
                val completion = calculateCompletion(repoHabit)
                
                HabitWithCompletions(
                    habit = repoHabit.habit,
                    completionsForDate = completionsForDate,
                    completedToday = completedToday,
                    streak = streak,
                    completion = completion
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val filteredHabits = combine(
        habitsWithCompletions,
        _selectedFilter
    ) { habits, filter ->
        filterHabits(habits, filter)
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val uiState = combine(
        habitsWithCompletions,
        filteredHabits,
        _selectedFilter,
        _selectedDate,
        _isLoading,
        _errorMessage
    ) { flowValues ->
        val habits = flowValues[0] as List<HabitWithCompletions>
        val filtered = flowValues[1] as List<HabitWithCompletions>
        val filter = flowValues[2] as HabitFilter
        val date = flowValues[3] as LocalDate
        val isLoading = flowValues[4] as Boolean
        val errorMessage = flowValues[5] as String?
        
        HabitsUiState(
            habits = habits,
            filteredHabits = filtered,
            isLoading = isLoading,
            selectedFilter = filter,
            selectedDate = date,
            errorMessage = errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitsUiState(isLoading = true)
    )
    
    fun setFilter(filter: HabitFilter) {
        _selectedFilter.value = filter
    }
    
    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }
    
    fun toggleHabitCompletion(habit: Habit) {
        viewModelScope.launch {
            try {
                val date = _selectedDate.value
                val habitWithCompletions = habitsWithCompletions.value.find { it.habit.id == habit.id }
                
                if (habitWithCompletions != null) {
                    if (habitWithCompletions.completedToday) {
                        // Remove completion for today
                        val completionToDelete = habitWithCompletions.completionsForDate.firstOrNull()
                        if (completionToDelete != null) {
                            habitRepository.deleteCompletion(completionToDelete)
                        }
                    } else {
                        // Add completion for today
                        val completion = HabitCompletion(
                            habitId = habit.id,
                            date = date
                        )
                        habitRepository.addCompletion(completion)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update habit completion: ${e.localizedMessage}"
            }
        }
    }
    
    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                habitRepository.addHabit(habit)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add habit: ${e.localizedMessage}"
            }
        }
    }
    
    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                habitRepository.updateHabit(habit)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update habit: ${e.localizedMessage}"
            }
        }
    }
    
    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            try {
                habitRepository.deleteHabit(habit)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete habit: ${e.localizedMessage}"
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    private fun calculateStreak(habitWithCompletions: HabitRepository.HabitWithCompletions): Int {
        var currentStreak = 0
        var currentDate = LocalDate.now()
        
        // Sort completions by date
        val sortedCompletions = habitWithCompletions.completions
            .map { it.date }
            .sorted()
        
        while (true) {
            val completionsForDate = sortedCompletions.filter { 
                it.isEqual(currentDate) 
            }
            
            if (completionsForDate.isEmpty()) {
                break
            }
            
            currentStreak++
            currentDate = currentDate.minusDays(1)
        }
        
        return currentStreak
    }
    
    private fun calculateCompletion(habitWithCompletions: HabitRepository.HabitWithCompletions): Float {
        val lastMonth = LocalDate.now().minusMonths(1)
        val today = LocalDate.now()
        
        // Days to track based on habit frequency
        val daysToTrack = when (habitWithCompletions.habit.frequency) {
            Habit.Frequency.DAILY -> 30 // Last 30 days
            Habit.Frequency.WEEKLY -> 12 // Last 12 weeks (approx 3 months)
            Habit.Frequency.MONTHLY -> 6 // Last 6 months
            Habit.Frequency.CUSTOM -> 30 // Default to 30 days
        }
        
        val completions = habitWithCompletions.completions.filter { completion ->
            val date = completion.date
            date.isAfter(lastMonth) && !date.isAfter(today)
        }
        
        return if (daysToTrack > 0) {
            completions.size.toFloat() / daysToTrack.toFloat()
        } else {
            0f
        }
    }
    
    private fun filterHabits(
        habits: List<HabitWithCompletions>,
        filter: HabitFilter
    ): List<HabitWithCompletions> {
        return when (filter) {
            HabitFilter.ALL -> habits
            HabitFilter.DAILY -> habits.filter { it.habit.frequency == Habit.Frequency.DAILY }
            HabitFilter.WEEKLY -> habits.filter { it.habit.frequency == Habit.Frequency.WEEKLY }
            HabitFilter.COMPLETED -> habits.filter { it.completedToday }
            HabitFilter.UNCOMPLETED -> habits.filter { !it.completedToday }
        }
    }
} 