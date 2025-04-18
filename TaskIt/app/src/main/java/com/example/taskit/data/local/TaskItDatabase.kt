package com.example.taskit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.taskit.data.local.converters.DateConverter
import com.example.taskit.data.local.converters.IntListConverter
import com.example.taskit.data.local.converters.StringListConverter
import com.example.taskit.data.local.dao.CategoryDao
import com.example.taskit.data.local.dao.GoalDao
import com.example.taskit.data.local.dao.HabitCompletionDao
import com.example.taskit.data.local.dao.HabitDao
import com.example.taskit.data.local.dao.PomodoroSessionDao
import com.example.taskit.data.local.dao.TaskDao
import com.example.taskit.data.model.Category
import com.example.taskit.data.model.Goal
import com.example.taskit.data.model.Habit
import com.example.taskit.data.model.HabitCompletion
import com.example.taskit.data.model.PomodoroSession
import com.example.taskit.data.model.Task

@Database(
    entities = [
        Task::class,
        Category::class,
        Habit::class,
        HabitCompletion::class,
        Goal::class,
        PomodoroSession::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(
    DateConverter::class,
    StringListConverter::class,
    IntListConverter::class
)
abstract class TaskItDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun goalDao(): GoalDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao
} 