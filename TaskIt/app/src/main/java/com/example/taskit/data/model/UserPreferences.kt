package com.example.taskit.data.model

import androidx.room.TypeConverters
import com.example.taskit.data.local.converters.DateConverter
import java.time.LocalTime

data class UserPreferences(
    val darkMode: Boolean = false,
    val dailySummaryEnabled: Boolean = true,
    val dailySummaryTime: LocalTime = LocalTime.of(8, 0), // Default 8 AM
    val defaultReminderTime: Long = 30, // Minutes before task due
    val defaultTaskDuration: Long = 60, // Minutes
    val defaultPomodoroFocusTime: Int = 25, // Minutes
    val defaultPomodoroBreakTime: Int = 5, // Minutes
    val defaultPomodoroLongBreakTime: Int = 15, // Minutes
    val defaultPomodoroSessionsBeforeLongBreak: Int = 4,
    val notificationSoundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val showCompletedTasks: Boolean = false,
    val defaultTaskView: TaskView = TaskView.LIST,
    val defaultTaskSort: TaskSort = TaskSort.DUE_DATE,
    val defaultTaskSortDirection: SortDirection = SortDirection.ASCENDING
)

enum class TaskView {
    LIST,
    GRID,
    CALENDAR
}

enum class TaskSort {
    TITLE,
    DUE_DATE,
    PRIORITY,
    CREATION_DATE,
    CATEGORY
}

enum class SortDirection {
    ASCENDING,
    DESCENDING
} 