package com.example.taskit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.taskit.data.local.converters.DateConverter
import java.time.LocalDateTime
import java.util.UUID

enum class PomodoroStatus(val value: Int) {
    NOT_STARTED(0),
    FOCUS(1),
    BREAK(2),
    LONG_BREAK(3),
    PAUSED(4),
    COMPLETED(5)
}

@Entity(tableName = "pomodoro_sessions")
@TypeConverters(DateConverter::class)
data class PomodoroSession(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val taskId: String? = null, // Optional associated task
    val label: String? = null, // User-defined label for the session
    val focusDuration: Int = 25, // In minutes
    val shortBreakDuration: Int = 5, // In minutes
    val longBreakDuration: Int = 15, // In minutes
    val sessionsBeforeLongBreak: Int = 4,
    val sessionsCompleted: Int = 0,
    val currentStatus: PomodoroStatus = PomodoroStatus.NOT_STARTED,
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null,
    val totalFocusTime: Long = 0, // In seconds
    val dateCreated: LocalDateTime = LocalDateTime.now()
) 