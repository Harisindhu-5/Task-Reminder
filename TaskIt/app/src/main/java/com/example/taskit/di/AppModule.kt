package com.example.taskit.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Removed duplicate WorkManager provider
    // Other app-wide dependencies can be added here
} 