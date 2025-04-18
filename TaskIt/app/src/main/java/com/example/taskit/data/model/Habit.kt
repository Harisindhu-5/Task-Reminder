package com.example.taskit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.taskit.data.local.converters.DateConverter
import com.example.taskit.data.local.converters.FrequencyConverter
import com.example.taskit.data.local.converters.IntListConverter
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "habits")
@TypeConverters(DateConverter::class, IntListConverter::class, FrequencyConverter::class)
data class Habit(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val color: Int = 0,
    val icon: String? = null, // Icon identifier from Material icons
    val frequency: Frequency = Frequency.DAILY, // How often the habit should be performed
    val frequencyDays: List<Int>? = null, // Days of week (1-7, 1 = Monday) for specific days
    val reminder: LocalDateTime? = null, // Daily reminder time
    val daysCompleted: Int = 0, // Total completed days
    val currentStreak: Int = 0, // Current streak
    val bestStreak: Int = 0, // Best streak
    val lastCompletedDate: LocalDateTime? = null,
    val dateCreated: LocalDateTime = LocalDateTime.now(),
    val dateModified: LocalDateTime = LocalDateTime.now(),
    val goalId: String? = null, // Associated with a goal
    val isActive: Boolean = true
) {
    enum class Frequency {
        DAILY, WEEKLY, MONTHLY, CUSTOM
    }
} 