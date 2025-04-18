package com.example.taskit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val categoryId: String? = null,
    val dueDate: LocalDateTime? = null,
    val reminderTime: LocalDateTime? = null,
    val isRepeating: Boolean = false,
    val repeatInterval: Int? = null, // days between repeats
    val completedDate: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)