package com.example.taskit.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.taskit.data.model.SortDirection
import com.example.taskit.data.model.TaskSort
import com.example.taskit.data.model.TaskView
import com.example.taskit.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    
    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DAILY_SUMMARY_ENABLED = booleanPreferencesKey("daily_summary_enabled")
        val DAILY_SUMMARY_TIME = stringPreferencesKey("daily_summary_time")
        val DEFAULT_REMINDER_TIME = longPreferencesKey("default_reminder_time")
        val DEFAULT_TASK_DURATION = longPreferencesKey("default_task_duration")
        val DEFAULT_POMODORO_FOCUS_TIME = intPreferencesKey("default_pomodoro_focus_time")
        val DEFAULT_POMODORO_BREAK_TIME = intPreferencesKey("default_pomodoro_break_time")
        val DEFAULT_POMODORO_LONG_BREAK_TIME = intPreferencesKey("default_pomodoro_long_break_time")
        val DEFAULT_POMODORO_SESSIONS_BEFORE_LONG_BREAK = intPreferencesKey("default_pomodoro_sessions_before_long_break")
        val NOTIFICATION_SOUND_ENABLED = booleanPreferencesKey("notification_sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SHOW_COMPLETED_TASKS = booleanPreferencesKey("show_completed_tasks")
        val DEFAULT_TASK_VIEW = intPreferencesKey("default_task_view")
        val DEFAULT_TASK_SORT = intPreferencesKey("default_task_sort")
        val DEFAULT_TASK_SORT_DIRECTION = intPreferencesKey("default_task_sort_direction")
    }
    
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        val dailySummaryTimeStr = preferences[PreferencesKeys.DAILY_SUMMARY_TIME] ?: LocalTime.of(8, 0).format(timeFormatter)
        val dailySummaryTime = LocalTime.parse(dailySummaryTimeStr, timeFormatter)
        
        UserPreferences(
            darkMode = preferences[PreferencesKeys.DARK_MODE] ?: false,
            dailySummaryEnabled = preferences[PreferencesKeys.DAILY_SUMMARY_ENABLED] ?: true,
            dailySummaryTime = dailySummaryTime,
            defaultReminderTime = preferences[PreferencesKeys.DEFAULT_REMINDER_TIME] ?: 30L,
            defaultTaskDuration = preferences[PreferencesKeys.DEFAULT_TASK_DURATION] ?: 60L,
            defaultPomodoroFocusTime = preferences[PreferencesKeys.DEFAULT_POMODORO_FOCUS_TIME] ?: 25,
            defaultPomodoroBreakTime = preferences[PreferencesKeys.DEFAULT_POMODORO_BREAK_TIME] ?: 5,
            defaultPomodoroLongBreakTime = preferences[PreferencesKeys.DEFAULT_POMODORO_LONG_BREAK_TIME] ?: 15,
            defaultPomodoroSessionsBeforeLongBreak = preferences[PreferencesKeys.DEFAULT_POMODORO_SESSIONS_BEFORE_LONG_BREAK] ?: 4,
            notificationSoundEnabled = preferences[PreferencesKeys.NOTIFICATION_SOUND_ENABLED] ?: true,
            vibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
            showCompletedTasks = preferences[PreferencesKeys.SHOW_COMPLETED_TASKS] ?: false,
            defaultTaskView = TaskView.values()[preferences[PreferencesKeys.DEFAULT_TASK_VIEW] ?: 0],
            defaultTaskSort = TaskSort.values()[preferences[PreferencesKeys.DEFAULT_TASK_SORT] ?: 1], // DUE_DATE by default
            defaultTaskSortDirection = SortDirection.values()[preferences[PreferencesKeys.DEFAULT_TASK_SORT_DIRECTION] ?: 0] // ASCENDING by default
        )
    }
    
    suspend fun updateDarkMode(darkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = darkMode
        }
    }
    
    suspend fun updateDailySummaryEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_SUMMARY_ENABLED] = enabled
        }
    }
    
    suspend fun updateDailySummaryTime(time: LocalTime) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_SUMMARY_TIME] = time.format(timeFormatter)
        }
    }
    
    suspend fun updateDefaultReminderTime(minutes: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REMINDER_TIME] = minutes
        }
    }
    
    suspend fun updateDefaultTaskDuration(minutes: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_TASK_DURATION] = minutes
        }
    }
    
    suspend fun updateDefaultPomodoroSettings(
        focusTime: Int,
        breakTime: Int,
        longBreakTime: Int,
        sessionsBeforeLongBreak: Int
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_POMODORO_FOCUS_TIME] = focusTime
            preferences[PreferencesKeys.DEFAULT_POMODORO_BREAK_TIME] = breakTime
            preferences[PreferencesKeys.DEFAULT_POMODORO_LONG_BREAK_TIME] = longBreakTime
            preferences[PreferencesKeys.DEFAULT_POMODORO_SESSIONS_BEFORE_LONG_BREAK] = sessionsBeforeLongBreak
        }
    }
    
    suspend fun updatePomodoroFocusTime(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_POMODORO_FOCUS_TIME] = minutes
        }
    }
    
    suspend fun updatePomodoroBreakTime(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_POMODORO_BREAK_TIME] = minutes
        }
    }
    
    suspend fun updatePomodoroLongBreakTime(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_POMODORO_LONG_BREAK_TIME] = minutes
        }
    }
    
    suspend fun updatePomodoroSessionsBeforeLongBreak(sessions: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_POMODORO_SESSIONS_BEFORE_LONG_BREAK] = sessions
        }
    }
    
    suspend fun updateNotificationSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_SOUND_ENABLED] = enabled
        }
    }
    
    suspend fun updateVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }
    
    suspend fun updateShowCompletedTasks(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_COMPLETED_TASKS] = show
        }
    }
    
    suspend fun updateDefaultTaskView(taskView: TaskView) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_TASK_VIEW] = taskView.ordinal
        }
    }
    
    suspend fun updateDefaultTaskSort(taskSort: TaskSort) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_TASK_SORT] = taskSort.ordinal
        }
    }
    
    suspend fun updateDefaultTaskSortDirection(sortDirection: SortDirection) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_TASK_SORT_DIRECTION] = sortDirection.ordinal
        }
    }
} 