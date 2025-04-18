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
import com.example.taskit.data.model.Task
import com.example.taskit.data.repository.TaskRepository
import com.example.taskit.data.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val taskRepository: TaskRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, workerParams) {

    private val TAG = "TaskReminderWorker"

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting task reminder worker")
        
        // Get task ID from input data
        val taskId = workerParams.inputData.getString(KEY_TASK_ID)
        
        if (taskId == null) {
            Log.e(TAG, "No task ID provided in input data")
            return Result.failure()
        }
        
        Log.d(TAG, "Looking up task with ID: $taskId")
        
        // Get task from repository
        val task = taskRepository.getTaskById(taskId)
        
        if (task == null) {
            Log.e(TAG, "Task not found with ID: $taskId")
            return Result.failure()
        }
        
        Log.d(TAG, "Found task: ${task.title}")
        
        // Don't send reminder if task is already completed
        if (task.isCompleted()) {
            Log.d(TAG, "Task is already completed, skipping reminder")
            return Result.success()
        }
        
        // Check user preferences
        val userPreferences = userPreferencesRepository.userPreferencesFlow.first()
        
        try {
            sendTaskReminder(task, userPreferences.notificationSoundEnabled, userPreferences.vibrationEnabled)
            Log.d(TAG, "Task reminder sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending task reminder: ${e.message}", e)
            return Result.failure()
        }
        
        return Result.success()
    }
    
    private fun sendTaskReminder(task: Task, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create a pending intent to open the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, task.id.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        // Format the time until due or overdue message
        val timeMessage = if (task.dueDate != null) {
            val now = LocalDateTime.now()
            val minutes = ChronoUnit.MINUTES.between(now, task.dueDate)
            
            if (minutes < 0) {
                "is overdue by ${Math.abs(minutes)} minutes"
            } else if (minutes == 0L) {
                "is due now"
            } else {
                "is due in $minutes minutes"
            }
        } else {
            ""
        }
        
        // Create notification content
        val notificationTitle = "Task Reminder"
        val notificationText = if (timeMessage.isNotEmpty()) {
            "${task.title} $timeMessage"
        } else {
            task.title
        }
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, TaskItApp.TASK_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .apply {
                if (!soundEnabled) {
                    setSilent(true)
                }
                if (vibrationEnabled) {
                    setVibrate(longArrayOf(0, 250, 250, 250))
                }
            }
            .build()
        
        // Send notification
        notificationManager.notify(task.id.hashCode(), notification)
    }
    
    companion object {
        const val KEY_TASK_ID = "task_id"
    }
}

private fun Task.isCompleted(): Boolean {
    return this.status == com.example.taskit.data.model.TaskStatus.COMPLETED
} 