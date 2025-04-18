package com.example.taskit.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.taskit.data.local.converters.DateConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId"), Index("date")]
)
@TypeConverters(DateConverter::class)
data class HabitCompletion(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val date: LocalDate,
    val completed: Boolean = true,
    val completedAt: LocalDateTime = LocalDateTime.now(),
    val notes: String? = null
) 