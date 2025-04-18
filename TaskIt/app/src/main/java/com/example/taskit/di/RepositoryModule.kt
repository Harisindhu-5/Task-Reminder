package com.example.taskit.di

import com.example.taskit.data.repository.CategoryRepository
import com.example.taskit.data.repository.CategoryRepositoryImpl
import com.example.taskit.data.repository.GoalRepository
import com.example.taskit.data.repository.GoalRepositoryImpl
import com.example.taskit.data.repository.HabitRepository
import com.example.taskit.data.repository.HabitRepositoryImpl
import com.example.taskit.data.repository.PomodoroRepository
import com.example.taskit.data.repository.PomodoroRepositoryImpl
import com.example.taskit.data.repository.TaskRepository
import com.example.taskit.data.repository.TaskRepositoryImpl
import com.example.taskit.data.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository
    
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository
    
    @Binds
    @Singleton
    abstract fun bindHabitRepository(
        habitRepositoryImpl: HabitRepositoryImpl
    ): HabitRepository
    
    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        goalRepositoryImpl: GoalRepositoryImpl
    ): GoalRepository
    
    @Binds
    @Singleton
    abstract fun bindPomodoroRepository(
        pomodoroRepositoryImpl: PomodoroRepositoryImpl
    ): PomodoroRepository
    
    // UserPreferencesRepository already has @Singleton and @Inject constructor
    // It doesn't need to be bound since it doesn't implement an interface
} 