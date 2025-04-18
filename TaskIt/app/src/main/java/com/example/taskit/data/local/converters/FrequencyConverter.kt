package com.example.taskit.data.local.converters

import androidx.room.TypeConverter
import com.example.taskit.data.model.Habit

/**
 * Type converter for Room database to convert between Habit.Frequency enum
 * and database integer values.
 */
class FrequencyConverter {
    @TypeConverter
    fun fromFrequency(frequency: Habit.Frequency?): String? {
        return frequency?.name
    }

    @TypeConverter
    fun toFrequency(value: String?): Habit.Frequency? {
        return value?.let { Habit.Frequency.valueOf(it) }
    }
} 