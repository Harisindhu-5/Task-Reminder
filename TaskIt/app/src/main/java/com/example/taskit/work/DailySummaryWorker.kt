package com.example.taskit.work

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskit.MainActivity
import com.example.taskit.R
import com.example.taskit.TaskItApp
import com.example.taskit.data.model.TaskStatus
import com.example.taskit.data.repository.TaskRepository
import com.example.taskit.data.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class DailySummaryWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val taskRepository: TaskRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, workerParams) {

    private val TAG = "DailySummaryWorker"

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting daily summary generation")
        
        val userPreferences = userPreferencesRepository.userPreferencesFlow.first()
        
        if (!userPreferences.dailySummaryEnabled) {
            Log.d(TAG, "Daily summary disabled in preferences, exiting")
            return Result.success()
        }
        
        val today = LocalDate.now()
        Log.d(TAG, "Getting tasks for today: $today")
        
        val tasksForToday = taskRepository.getTasksForDay(today).first()
        Log.d(TAG, "Found ${tasksForToday.size} tasks for today")
        
        val completedTasks = tasksForToday.filter { it.status == TaskStatus.COMPLETED }
        val pendingTasks = tasksForToday.filter { it.status != TaskStatus.COMPLETED }
        
        Log.d(TAG, "Completed: ${completedTasks.size}, Pending: ${pendingTasks.size}")
        
        // Create a notification even if there are no tasks
        val notificationText = if (tasksForToday.isEmpty()) {
            "You have no tasks scheduled for today"
        } else {
            buildString {
                append("Today's summary: ")
                append("${completedTasks.size}/${tasksForToday.size} tasks completed")
                if (pendingTasks.isNotEmpty()) {
                    append(", ${pendingTasks.size} pending")
                }
            }
        }
        
        // Get the notification manager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create a pending intent for the notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, TaskItApp.DAILY_SUMMARY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Daily Task Summary")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .build()
        
        // Send the notification
        try {
            notificationManager.notify(DAILY_SUMMARY_NOTIFICATION_ID, notification)
            Log.d(TAG, "Daily summary notification sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification: ${e.message}", e)
            return Result.failure()
        }
        
        return Result.success()
    }
    
    companion object {
        private const val DAILY_SUMMARY_NOTIFICATION_ID = 1001
    }
} 