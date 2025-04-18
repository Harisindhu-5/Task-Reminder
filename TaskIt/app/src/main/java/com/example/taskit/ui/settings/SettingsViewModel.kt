package com.example.taskit.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskit.data.model.SortDirection
import com.example.taskit.data.model.TaskSort
import com.example.taskit.data.model.TaskView
import com.example.taskit.data.model.UserPreferences
import com.example.taskit.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = userPreferencesRepository.userPreferencesFlow
        .map { preferences ->
            SettingsUiState(
                darkMode = preferences.darkMode,
                dailySummaryEnabled = preferences.dailySummaryEnabled,
                dailySummaryTime = preferences.dailySummaryTime,
                defaultReminderTime = preferences.defaultReminderTime,
                defaultTaskDuration = preferences.defaultTaskDuration,
                defaultPomodoroFocusTime = preferences.defaultPomodoroFocusTime,
                defaultPomodoroBreakTime = preferences.defaultPomodoroBreakTime,
                defaultPomodoroLongBreakTime = preferences.defaultPomodoroLongBreakTime,
                defaultPomodoroSessionsBeforeLongBreak = preferences.defaultPomodoroSessionsBeforeLongBreak,
                notificationSoundEnabled = preferences.notificationSoundEnabled,
                vibrationEnabled = preferences.vibrationEnabled,
                showCompletedTasks = preferences.showCompletedTasks,
                defaultTaskView = preferences.defaultTaskView,
                defaultTaskSort = preferences.defaultTaskSort,
                defaultTaskSortDirection = preferences.defaultTaskSortDirection
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )
    
    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateDarkMode(enabled)
        }
    }
    
    fun updateDailySummaryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateDailySummaryEnabled(enabled)
        }
    }
    
    fun updateDailySummaryTime(time: LocalTime) {
        viewModelScope.launch {
            userPreferencesRepository.updateDailySummaryTime(time)
        }
    }
    
    fun updateDefaultReminderTime(minutes: Long) {
        viewModelScope.launch {
            userPreferencesRepository.updateDefaultReminderTime(minutes)
        }
    }
    
    fun updateDefaultTaskDuration(minutes: Long) {
        viewModelScope.launch {
            userPreferencesRepository.updateDefaultTaskDuration(minutes)
        }
    }
    
    fun updatePomodoroFocusTime(minutes: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updatePomodoroFocusTime(minutes)
        }
    }
    
    fun updatePomodoroBreakTime(minutes: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updatePomodoroBreakTime(minutes)
        }
    }
    
    fun updatePomodoroLongBreakTime(minutes: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updatePomodoroLongBreakTime(minutes)
        }
    }
    
    fun updatePomodoroSessionsBeforeLongBreak(sessions: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updatePomodoroSessionsBeforeLongBreak(sessions)
        }
    }
    
    fun updateNotificationSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateNotificationSoundEnabled(enabled)
        }
    }
    
    fun updateVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateVibrationEnabled(enabled)
        }
    }
    
    fun updateShowCompletedTasks(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateShowCompletedTasks(show)
        }
    }
    
    fun updateDefaultTaskView(view: TaskView) {
        viewModelScope.launch {
            userPreferencesRepository.updateDefaultTaskView(view)
        }
    }
    
    fun updateDefaultTaskSort(sort: TaskSort) {
        viewModelScope.launch {
            userPreferencesRepository.updateDefaultTaskSort(sort)
        }
    }
    
    fun updateDefaultTaskSortDirection(direction: SortDirection) {
        viewModelScope.launch {
            userPreferencesRepository.updateDefaultTaskSortDirection(direction)
        }
    }
}

data class SettingsUiState(
    val darkMode: Boolean = false,
    val dailySummaryEnabled: Boolean = true,
    val dailySummaryTime: LocalTime = LocalTime.of(8, 0),
    val defaultReminderTime: Long = 30,
    val defaultTaskDuration: Long = 60,
    val defaultPomodoroFocusTime: Int = 25,
    val defaultPomodoroBreakTime: Int = 5,
    val defaultPomodoroLongBreakTime: Int = 15,
    val defaultPomodoroSessionsBeforeLongBreak: Int = 4,
    val notificationSoundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val showCompletedTasks: Boolean = false,
    val defaultTaskView: TaskView = TaskView.LIST,
    val defaultTaskSort: TaskSort = TaskSort.DUE_DATE,
    val defaultTaskSortDirection: SortDirection = SortDirection.ASCENDING
) 