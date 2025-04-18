package com.example.taskit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.example.taskit.work.WorkSchedulerHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TaskItApp : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var workSchedulerHelper: WorkSchedulerHelper
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // Create notification channels before scheduling any work
        createNotificationChannels()
        
        // Schedule work after notification channels are created
        applicationScope.launch(Dispatchers.IO) {
            try {
                // Cancel any existing work to ensure fresh scheduling
                WorkManager.getInstance(applicationContext).cancelAllWork()
                
                // Schedule work
                workSchedulerHelper.scheduleDailySummaryWork()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Task reminder channel
            val taskReminderChannel = NotificationChannel(
                TASK_REMINDER_CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you before tasks are due"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Daily summary channel
            val dailySummaryChannel = NotificationChannel(
                DAILY_SUMMARY_CHANNEL_ID,
                "Daily Task Summaries",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Provides a daily summary of your tasks"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Focus mode channel
            val focusModeChannel = NotificationChannel(
                FOCUS_MODE_CHANNEL_ID,
                "Focus Mode",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications related to Focus Mode and Pomodoro Timer"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Clear existing channels to ensure fresh configuration
            notificationManager.deleteNotificationChannel(TASK_REMINDER_CHANNEL_ID)
            notificationManager.deleteNotificationChannel(DAILY_SUMMARY_CHANNEL_ID)
            notificationManager.deleteNotificationChannel(FOCUS_MODE_CHANNEL_ID)
            
            // Create new channels
            notificationManager.createNotificationChannels(
                listOf(taskReminderChannel, dailySummaryChannel, focusModeChannel)
            )
        }
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }
    
    companion object {
        const val TASK_REMINDER_CHANNEL_ID = "task_reminders"
        const val DAILY_SUMMARY_CHANNEL_ID = "daily_summaries" 
        const val FOCUS_MODE_CHANNEL_ID = "focus_mode"
    }
} 