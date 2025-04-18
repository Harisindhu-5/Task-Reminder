package com.example.taskit.di

import android.content.Context
import androidx.room.Room
import com.example.taskit.data.local.TaskItDatabase
import com.example.taskit.data.local.dao.CategoryDao
import com.example.taskit.data.local.dao.GoalDao
import com.example.taskit.data.local.dao.HabitCompletionDao
import com.example.taskit.data.local.dao.HabitDao
import com.example.taskit.data.local.dao.PomodoroSessionDao
import com.example.taskit.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TaskItDatabase {
        return Room.databaseBuilder(
            context,
            TaskItDatabase::class.java,
            "taskit_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideTaskDao(database: TaskItDatabase): TaskDao {
        return database.taskDao()
    }
    
    @Provides
    fun provideCategoryDao(database: TaskItDatabase): CategoryDao {
        return database.categoryDao()
    }
    
    @Provides
    fun provideHabitDao(database: TaskItDatabase): HabitDao {
        return database.habitDao()
    }
    
    @Provides
    fun provideHabitCompletionDao(database: TaskItDatabase): HabitCompletionDao {
        return database.habitCompletionDao()
    }
    
    @Provides
    fun provideGoalDao(database: TaskItDatabase): GoalDao {
        return database.goalDao()
    }
    
    @Provides
    fun providePomodoroSessionDao(database: TaskItDatabase): PomodoroSessionDao {
        return database.pomodoroSessionDao()
    }
} 