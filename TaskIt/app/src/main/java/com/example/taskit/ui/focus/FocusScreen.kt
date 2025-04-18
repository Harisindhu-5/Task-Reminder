package com.example.taskit.ui.focus

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskit.data.model.Task
import com.example.taskit.data.model.TaskPriority
import com.example.taskit.data.model.TaskStatus
import com.example.taskit.ui.theme.TaskItTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@Composable
fun FocusScreen(
    viewModel: FocusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showTaskSelectionDialog by remember { mutableStateOf(false) }
    
    // Show error if needed
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMessage ->
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            FocusTopBar(
                onSettingsClick = { showSettingsDialog = true }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer display
            PomodoroTimer(
                remainingTimeMillis = uiState.remainingTimeMillis,
                totalTimeMillis = uiState.totalTimeMillis,
                sessionType = uiState.currentSessionType,
                completedSessions = uiState.completedFocusSessions,
                sessionsBeforeLongBreak = uiState.sessionsBeforeLongBreak,
                modifier = Modifier.padding(vertical = 32.dp)
            )
            
            // Task card
            val selectedTask = uiState.taskSelected
            if (selectedTask != null) {
                CurrentTaskCard(
                    task = selectedTask,
                    onSelectDifferentTask = { showTaskSelectionDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            } else {
                NoTaskSelectedCard(
                    onSelectTask = { showTaskSelectionDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
            
            // Session controls
            PomodoroControls(
                isActive = uiState.isActive,
                onStart = { viewModel.startFocusSession(selectedTask) },
                onPause = { viewModel.pauseSession() },
                onResume = { viewModel.resumeSession() },
                onCancel = { viewModel.cancelSession() },
                onSkip = { viewModel.skipToNextSession() },
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Session info
            SessionInfo(
                currentSessionType = uiState.currentSessionType,
                completedSessions = uiState.completedFocusSessions,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
    
    // Settings Dialog
    if (showSettingsDialog) {
        PomodoroSettingsDialog(
            focusTimeMinutes = uiState.focusTimeMinutes,
            breakTimeMinutes = uiState.breakTimeMinutes,
            longBreakTimeMinutes = uiState.longBreakTimeMinutes,
            sessionsBeforeLongBreak = uiState.sessionsBeforeLongBreak,
            onFocusTimeChange = { viewModel.updateFocusTime(it) },
            onBreakTimeChange = { viewModel.updateBreakTime(it) },
            onLongBreakTimeChange = { viewModel.updateLongBreakTime(it) },
            onSessionsBeforeLongBreakChange = { viewModel.updateSessionsBeforeLongBreak(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }
    
    // Task Selection Dialog
    if (showTaskSelectionDialog) {
        TaskSelectionDialog(
            tasks = uiState.recentTasks,
            onTaskSelected = { 
                viewModel.selectTask(it)
                showTaskSelectionDialog = false
            },
            onDismiss = { showTaskSelectionDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTopBar(
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Focus Timer") },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}

@Composable
fun PomodoroTimer(
    remainingTimeMillis: Long,
    totalTimeMillis: Long,
    sessionType: SessionType,
    completedSessions: Int,
    sessionsBeforeLongBreak: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalTimeMillis > 0) {
        remainingTimeMillis.toFloat() / totalTimeMillis.toFloat()
    } else {
        0f
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "TimerProgress"
    )
    
    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMillis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTimeMillis) % 60
    
    val color = when (sessionType) {
        SessionType.FOCUS -> MaterialTheme.colorScheme.primary
        SessionType.BREAK -> MaterialTheme.colorScheme.secondary
        SessionType.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (sessionType) {
                SessionType.FOCUS -> "Focus Time"
                SessionType.BREAK -> "Break Time"
                SessionType.LONG_BREAK -> "Long Break"
            },
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
        
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = color,
                strokeWidth = 8.dp
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = color
                )
                
                Text(
                    text = "Session ${completedSessions + 1}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Row(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    repeat(sessionsBeforeLongBreak) { index ->
                        val isCompleted = index < completedSessions % sessionsBeforeLongBreak
                        val isCurrent = index == completedSessions % sessionsBeforeLongBreak && 
                                       sessionType == SessionType.FOCUS
                        
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> color
                                        isCurrent -> color.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PomodoroControls(
    isActive: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stop button
        FilledIconButton(
            onClick = onCancel,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.Stop,
                contentDescription = "Stop",
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Main action button (Start/Pause/Resume)
        FilledIconButton(
            onClick = if (!isActive) onStart else onPause,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                if (!isActive) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (!isActive) "Start" else "Pause",
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Skip button
        FilledIconButton(
            onClick = onSkip,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = "Skip",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CurrentTaskCard(
    task: Task,
    onSelectDifferentTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Task",
                    style = MaterialTheme.typography.titleMedium
                )
                
                IconButton(
                    onClick = onSelectDifferentTask
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Change Task"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val priorityColor = getTaskPriorityColor(task.priority)
                
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(priorityColor)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = task.priority.name,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun NoTaskSelectedCard(
    onSelectTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Text(
                text = "No Task Selected",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onSelectTask
            ) {
                Text("Select a Task")
            }
        }
    }
}

@Composable
fun SessionInfo(
    currentSessionType: SessionType,
    completedSessions: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (currentSessionType) {
                SessionType.FOCUS -> "Focus Session"
                SessionType.BREAK -> "Short Break"
                SessionType.LONG_BREAK -> "Long Break"
            },
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "$completedSessions focus sessions completed",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun PomodoroSettingsDialog(
    focusTimeMinutes: Int,
    breakTimeMinutes: Int,
    longBreakTimeMinutes: Int,
    sessionsBeforeLongBreak: Int,
    onFocusTimeChange: (Int) -> Unit,
    onBreakTimeChange: (Int) -> Unit,
    onLongBreakTimeChange: (Int) -> Unit,
    onSessionsBeforeLongBreakChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var focusTime by remember { mutableFloatStateOf(focusTimeMinutes.toFloat()) }
    var breakTime by remember { mutableFloatStateOf(breakTimeMinutes.toFloat()) }
    var longBreakTime by remember { mutableFloatStateOf(longBreakTimeMinutes.toFloat()) }
    var sessions by remember { mutableFloatStateOf(sessionsBeforeLongBreak.toFloat()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Pomodoro Settings",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Focus time
                Text(
                    text = "Focus Time: ${focusTime.toInt()} minutes",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = focusTime,
                    onValueChange = { focusTime = it },
                    valueRange = 5f..60f,
                    steps = 11,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Break time
                Text(
                    text = "Break Time: ${breakTime.toInt()} minutes",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = breakTime,
                    onValueChange = { breakTime = it },
                    valueRange = 1f..15f,
                    steps = 14,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Long break time
                Text(
                    text = "Long Break: ${longBreakTime.toInt()} minutes",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = longBreakTime,
                    onValueChange = { longBreakTime = it },
                    valueRange = 5f..30f,
                    steps = 5,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // Sessions before long break
                Text(
                    text = "Sessions before long break: ${sessions.toInt()}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = sessions,
                    onValueChange = { sessions = it },
                    valueRange = 2f..6f,
                    steps = 4,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onFocusTimeChange(focusTime.toInt())
                            onBreakTimeChange(breakTime.toInt())
                            onLongBreakTimeChange(longBreakTime.toInt())
                            onSessionsBeforeLongBreakChange(sessions.toInt())
                            onDismiss()
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun TaskSelectionDialog(
    tasks: List<Task>,
    onTaskSelected: (Task) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select a Task",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No tasks available",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Create tasks in the Tasks tab",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tasks) { task ->
                            TaskSelectionItem(
                                task = task,
                                onClick = { onTaskSelected(task) }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun TaskSelectionItem(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.dueDate != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = getTaskDueDateColor(task.dueDate),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatTaskDueDate(task.dueDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = getTaskDueDateColor(task.dueDate)
                            )
                        }
                    }
                }
            }
            
            val priorityColor = getTaskPriorityColor(task.priority)
            
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(priorityColor)
            )
        }
    }
}

// Helper functions for formatting dates and determining color
private fun formatTaskDueDate(dueDate: LocalDateTime): String {
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
    
    return when {
        dueDate.toLocalDate().isEqual(now.toLocalDate()) -> "Today, ${dueDate.format(DateTimeFormatter.ofPattern("h:mm a"))}"
        dueDate.toLocalDate().isEqual(now.plusDays(1).toLocalDate()) -> "Tomorrow, ${dueDate.format(DateTimeFormatter.ofPattern("h:mm a"))}"
        dueDate.year == now.year -> dueDate.format(formatter)
        else -> dueDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a"))
    }
}

private fun getTaskDueDateColor(dueDate: LocalDateTime): Color {
    val now = LocalDateTime.now()
    return when {
        dueDate.isBefore(now) -> Color.Red
        dueDate.isBefore(now.plusDays(1)) -> Color(0xFFFFA000) // Orange
        else -> Color.Gray
    }
}

// Updated function to get color for task priority
private fun getTaskPriorityColor(priority: TaskPriority): Color {
    return when (priority) {
        TaskPriority.HIGH -> Color.Red
        TaskPriority.MEDIUM -> Color(0xFF1976D2) // More vibrant blue
        TaskPriority.LOW -> Color.Green
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FocusScreenPreview() {
    TaskItTheme {
        Surface {
            val task = Task(
                title = "Finish TaskIt app development",
                description = "Implement all features and make sure they work correctly",
                priority = TaskPriority.HIGH
            )
            
            Column(modifier = Modifier.padding(16.dp)) {
                PomodoroTimer(
                    remainingTimeMillis = 15 * 60 * 1000L,
                    totalTimeMillis = 25 * 60 * 1000L,
                    sessionType = SessionType.FOCUS,
                    completedSessions = 2,
                    sessionsBeforeLongBreak = 4
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                CurrentTaskCard(
                    task = task,
                    onSelectDifferentTask = {}
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                PomodoroControls(
                    isActive = false,
                    onStart = {},
                    onPause = {},
                    onResume = {},
                    onCancel = {},
                    onSkip = {}
                )
            }
        }
    }
} 