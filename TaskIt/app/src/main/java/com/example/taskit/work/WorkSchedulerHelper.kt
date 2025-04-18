package com.example.taskit.work

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.taskit.data.model.Task
import com.example.taskit.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkSchedulerHelper @Inject constructor(
    private val workManager: WorkManager,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    companion object {
        private const val TAG = "WorkSchedulerHelper"
        private const val DAILY_SUMMARY_WORK_NAME = "daily_summary_work"
        private const val TASK_REMINDER_PREFIX = "task_reminder_"
    }
    
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    suspend fun scheduleDailySummaryWork() {
        val userPreferences = userPreferencesRepository.userPreferencesFlow.first()
        
        Log.d(TAG, "Scheduling daily summary work")
        
        // Cancel existing work if summary is disabled
        if (!userPreferences.dailySummaryEnabled) {
            Log.d(TAG, "Daily summary disabled, canceling existing work")
            workManager.cancelUniqueWork(DAILY_SUMMARY_WORK_NAME)
            return
        }
        
        // Calculate initial delay to target time
        val now = LocalDateTime.now()
        val targetTime = LocalDateTime.of(
            LocalDate.now(),
            userPreferences.dailySummaryTime
        )
        
        // If target time is already passed for today, schedule for tomorrow
        val adjustedTargetTime = if (now.isAfter(targetTime)) {
            targetTime.plusDays(1)
        } else {
            targetTime
        }
        
        val initialDelay = ChronoUnit.MILLIS.between(now, adjustedTargetTime)
        
        Log.d(TAG, "Daily summary scheduled for: $adjustedTargetTime (in $initialDelay ms)")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // Create the work request
        val dailySummaryWorkRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        
        // Enqueue the work
        workManager.enqueueUniquePeriodicWork(
            DAILY_SUMMARY_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailySummaryWorkRequest
        )
        
        Log.d(TAG, "Daily summary work scheduled successfully")
    }
    
    fun scheduleTaskReminder(task: Task) {
        // Only schedule if the task has a due date and is not completed
        if (task.dueDate == null || task.status == com.example.taskit.data.model.TaskStatus.COMPLETED) {
            Log.d(TAG, "Not scheduling reminder for task ${task.id} - no due date or already completed")
            return
        }
        
        // Cancel any existing reminders for this task
        cancelTaskReminder(task.id)
        
        val now = LocalDateTime.now()
        val dueDateTime = task.dueDate
        
        // Calculate reminder time using default reminder time from preferences
        coroutineScope.launch {
            try {
                val userPreferences = userPreferencesRepository.userPreferencesFlow.first()
                val reminderMinutes = userPreferences.defaultReminderTime // Default reminder time from preferences
                val reminderTime = dueDateTime.minusMinutes(reminderMinutes)
                
                // Don't schedule if reminder time is in the past
                if (reminderTime.isBefore(now)) {
                    Log.d(TAG, "Reminder time is in the past for task ${task.id}, not scheduling")
                    return@launch
                }
                
                val initialDelay = ChronoUnit.MILLIS.between(now, reminderTime)
                
                Log.d(TAG, "Scheduling reminder for task ${task.id} at $reminderTime (in $initialDelay ms)")
                
                val inputData = Data.Builder()
                    .putString(TaskReminderWorker.KEY_TASK_ID, task.id)
                    .build()
                
                val taskReminderRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
                    .setInputData(inputData)
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .build()
                
                workManager.enqueueUniqueWork(
                    getTaskReminderWorkName(task.id),
                    ExistingWorkPolicy.REPLACE,
                    taskReminderRequest
                )
                
                Log.d(TAG, "Task reminder scheduled successfully for task ${task.id}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling task reminder: ${e.message}", e)
            }
        }
    }
    
    fun cancelTaskReminder(taskId: String) {
        Log.d(TAG, "Canceling reminder for task $taskId")
        workManager.cancelUniqueWork(getTaskReminderWorkName(taskId))
    }
    
    // For testing - send a daily summary notification immediately
    fun testDailySummaryNotification() {
        Log.d(TAG, "Testing daily summary notification")
        
        val testWorkRequest = OneTimeWorkRequestBuilder<DailySummaryWorker>()
            .build()
        
        workManager.enqueue(testWorkRequest)
    }
    
    // For testing - send a task reminder notification immediately
    fun testTaskReminderNotification(task: Task) {
        Log.d(TAG, "Testing task reminder notification for task ${task.id}")
        
        val inputData = Data.Builder()
            .putString(TaskReminderWorker.KEY_TASK_ID, task.id)
            .build()
        
        val testWorkRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInputData(inputData)
            .build()
        
        workManager.enqueue(testWorkRequest)
    }
    
    private fun getTaskReminderWorkName(taskId: String): String {
        return "$TASK_REMINDER_PREFIX$taskId"
    }
} 