package com.example.taskit.ui.focus

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskit.data.model.PomodoroSession
import com.example.taskit.data.model.Task
import com.example.taskit.data.repository.TaskRepository
import com.example.taskit.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class FocusUiState(
    val currentSession: PomodoroSession? = null,
    val isActive: Boolean = false,
    val remainingTimeMillis: Long = 0L,
    val totalTimeMillis: Long = 0L,
    val currentSessionType: SessionType = SessionType.FOCUS,
    val completedFocusSessions: Int = 0,
    val focusTimeMinutes: Int = 25,
    val breakTimeMinutes: Int = 5,
    val longBreakTimeMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val taskSelected: Task? = null,
    val recentTasks: List<Task> = emptyList(),
    val errorMessage: String? = null
)

enum class SessionType {
    FOCUS,
    BREAK,
    LONG_BREAK
}

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()
    
    private var timer: CountDownTimer? = null
    
    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(
                    focusTimeMinutes = prefs.defaultPomodoroFocusTime,
                    breakTimeMinutes = prefs.defaultPomodoroBreakTime,
                    longBreakTimeMinutes = prefs.defaultPomodoroLongBreakTime,
                    sessionsBeforeLongBreak = prefs.defaultPomodoroSessionsBeforeLongBreak
                )
                
                // If timer is not active, update the remaining time based on new preferences
                if (!_uiState.value.isActive) {
                    updateRemainingTimeBasedOnSessionType()
                }
            }
        }
        
        loadRecentTasks()
    }
    
    private fun loadRecentTasks() {
        viewModelScope.launch {
            val today = LocalDateTime.now()
            val oneWeekAgo = today.minusWeeks(1)
            
            taskRepository.getTasksByDateRangeFlow(oneWeekAgo, today)
                .collect { tasks ->
                    _uiState.value = _uiState.value.copy(
                        recentTasks = tasks.sortedByDescending { it.createdAt }
                            .take(5)
                    )
                }
        }
    }
    
    fun startFocusSession(task: Task? = null) {
        // Cancel any existing timer
        timer?.cancel()
        
        // Update task selection
        _uiState.value = _uiState.value.copy(
            taskSelected = task,
            isActive = true
        )
        
        updateRemainingTimeBasedOnSessionType()
        
        val timeToRun = _uiState.value.remainingTimeMillis
        
        timer = object : CountDownTimer(timeToRun, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _uiState.value = _uiState.value.copy(
                    remainingTimeMillis = millisUntilFinished
                )
            }
            
            override fun onFinish() {
                val currentState = _uiState.value
                val newCompletedSessions = if (currentState.currentSessionType == SessionType.FOCUS) {
                    currentState.completedFocusSessions + 1
                } else {
                    currentState.completedFocusSessions
                }
                
                // Determine next session type
                val nextSessionType = when {
                    currentState.currentSessionType == SessionType.FOCUS -> {
                        if (newCompletedSessions % currentState.sessionsBeforeLongBreak == 0) {
                            SessionType.LONG_BREAK
                        } else {
                            SessionType.BREAK
                        }
                    }
                    else -> SessionType.FOCUS
                }
                
                _uiState.value = currentState.copy(
                    currentSessionType = nextSessionType,
                    completedFocusSessions = newCompletedSessions,
                    isActive = false
                )
                
                updateRemainingTimeBasedOnSessionType()
                
                // Optional: Auto-start the next session
                // startFocusSession(currentState.taskSelected)
            }
        }.start()
    }
    
    fun pauseSession() {
        timer?.cancel()
        _uiState.value = _uiState.value.copy(isActive = false)
    }
    
    fun resumeSession() {
        val remainingTime = _uiState.value.remainingTimeMillis
        
        timer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _uiState.value = _uiState.value.copy(
                    remainingTimeMillis = millisUntilFinished
                )
            }
            
            override fun onFinish() {
                val currentState = _uiState.value
                val newCompletedSessions = if (currentState.currentSessionType == SessionType.FOCUS) {
                    currentState.completedFocusSessions + 1
                } else {
                    currentState.completedFocusSessions
                }
                
                val nextSessionType = when {
                    currentState.currentSessionType == SessionType.FOCUS -> {
                        if (newCompletedSessions % currentState.sessionsBeforeLongBreak == 0) {
                            SessionType.LONG_BREAK
                        } else {
                            SessionType.BREAK
                        }
                    }
                    else -> SessionType.FOCUS
                }
                
                _uiState.value = currentState.copy(
                    currentSessionType = nextSessionType,
                    completedFocusSessions = newCompletedSessions,
                    isActive = false
                )
                
                updateRemainingTimeBasedOnSessionType()
            }
        }.start()
        
        _uiState.value = _uiState.value.copy(isActive = true)
    }
    
    fun cancelSession() {
        timer?.cancel()
        _uiState.value = _uiState.value.copy(
            isActive = false,
            currentSessionType = SessionType.FOCUS,
            taskSelected = null
        )
        updateRemainingTimeBasedOnSessionType()
    }
    
    fun skipToNextSession() {
        timer?.cancel()
        
        val currentState = _uiState.value
        val newCompletedSessions = if (currentState.currentSessionType == SessionType.FOCUS) {
            currentState.completedFocusSessions + 1
        } else {
            currentState.completedFocusSessions
        }
        
        val nextSessionType = when {
            currentState.currentSessionType == SessionType.FOCUS -> {
                if (newCompletedSessions % currentState.sessionsBeforeLongBreak == 0) {
                    SessionType.LONG_BREAK
                } else {
                    SessionType.BREAK
                }
            }
            else -> SessionType.FOCUS
        }
        
        _uiState.value = currentState.copy(
            currentSessionType = nextSessionType,
            completedFocusSessions = newCompletedSessions,
            isActive = false
        )
        
        updateRemainingTimeBasedOnSessionType()
    }
    
    fun selectTask(task: Task) {
        _uiState.value = _uiState.value.copy(taskSelected = task)
    }
    
    fun updateFocusTime(minutes: Int) {
        if (!_uiState.value.isActive) {
            _uiState.value = _uiState.value.copy(focusTimeMinutes = minutes)
            if (_uiState.value.currentSessionType == SessionType.FOCUS) {
                updateRemainingTimeBasedOnSessionType()
            }
            
            viewModelScope.launch {
                userPreferencesRepository.updatePomodoroFocusTime(minutes)
            }
        }
    }
    
    fun updateBreakTime(minutes: Int) {
        if (!_uiState.value.isActive) {
            _uiState.value = _uiState.value.copy(breakTimeMinutes = minutes)
            if (_uiState.value.currentSessionType == SessionType.BREAK) {
                updateRemainingTimeBasedOnSessionType()
            }
            
            viewModelScope.launch {
                userPreferencesRepository.updatePomodoroBreakTime(minutes)
            }
        }
    }
    
    fun updateLongBreakTime(minutes: Int) {
        if (!_uiState.value.isActive) {
            _uiState.value = _uiState.value.copy(longBreakTimeMinutes = minutes)
            if (_uiState.value.currentSessionType == SessionType.LONG_BREAK) {
                updateRemainingTimeBasedOnSessionType()
            }
            
            viewModelScope.launch {
                userPreferencesRepository.updatePomodoroLongBreakTime(minutes)
            }
        }
    }
    
    fun updateSessionsBeforeLongBreak(sessions: Int) {
        if (!_uiState.value.isActive) {
            _uiState.value = _uiState.value.copy(sessionsBeforeLongBreak = sessions)
            
            viewModelScope.launch {
                userPreferencesRepository.updatePomodoroSessionsBeforeLongBreak(sessions)
            }
        }
    }
    
    private fun updateRemainingTimeBasedOnSessionType() {
        val currentState = _uiState.value
        val timeInMillis = when (currentState.currentSessionType) {
            SessionType.FOCUS -> currentState.focusTimeMinutes * 60 * 1000L
            SessionType.BREAK -> currentState.breakTimeMinutes * 60 * 1000L
            SessionType.LONG_BREAK -> currentState.longBreakTimeMinutes * 60 * 1000L
        }
        
        _uiState.value = currentState.copy(
            remainingTimeMillis = timeInMillis,
            totalTimeMillis = timeInMillis
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
} 