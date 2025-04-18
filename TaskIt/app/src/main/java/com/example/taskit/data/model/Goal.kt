package com.example.taskit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.taskit.data.local.converters.DateConverter
import java.time.LocalDateTime
import java.util.UUID

enum class GoalPeriod(val value: Int) {
    WEEKLY(0),
    MONTHLY(1),
    QUARTERLY(2),
    YEARLY(3),
    CUSTOM(4)
}

enum class GoalStatus(val value: Int) {
    ACTIVE(0),
    COMPLETED(1),
    ABANDONED(2)
}

@Entity(tableName = "goals")
@TypeConverters(DateConverter::class)
data class Goal(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val period: GoalPeriod = GoalPeriod.WEEKLY,
    val startDate: LocalDateTime = LocalDateTime.now(),
    val targetDate: LocalDateTime,
    val status: GoalStatus = GoalStatus.ACTIVE,
    val progress: Int = 0, // 0-100 percentage
    val color: Int,
    val dateCreated: LocalDateTime = LocalDateTime.now(),
    val dateModified: LocalDateTime = LocalDateTime.now(),
    val dateCompleted: LocalDateTime? = null
) 