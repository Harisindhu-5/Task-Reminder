package com.example.taskit.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskit.data.model.SortDirection
import com.example.taskit.data.model.TaskSort
import com.example.taskit.data.model.TaskView
import com.example.taskit.ui.theme.TaskItTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showTaskViewDialog by remember { mutableStateOf(false) }
    var showTaskSortDialog by remember { mutableStateOf(false) }
    var showSortDirectionDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            SettingsTopBar()
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // App Theme
            item {
                SettingsCategory(title = "App Theme")
                
                SettingsSwitch(
                    title = "Dark Mode",
                    description = "Use dark theme",
                    icon = Icons.Default.DarkMode,
                    checked = uiState.darkMode,
                    onCheckedChange = viewModel::updateDarkMode
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            // Notifications
            item {
                SettingsCategory(title = "Notifications")
                
                SettingsSwitch(
                    title = "Daily Summary",
                    description = "Receive a daily summary of your tasks",
                    icon = Icons.Default.Notifications,
                    checked = uiState.dailySummaryEnabled,
                    onCheckedChange = viewModel::updateDailySummaryEnabled
                )
                
                if (uiState.dailySummaryEnabled) {
                    SettingsClickable(
                        title = "Summary Time",
                        description = "When to receive daily summaries",
                        icon = Icons.Default.AccessTime,
                        value = uiState.dailySummaryTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                        onClick = { showTimePickerDialog = true }
                    )
                }
                
                SettingsSwitch(
                    title = "Notification Sound",
                    description = "Play sound for notifications",
                    icon = Icons.Default.Notifications,
                    checked = uiState.notificationSoundEnabled,
                    onCheckedChange = viewModel::updateNotificationSoundEnabled
                )
                
                SettingsSwitch(
                    title = "Vibration",
                    description = "Vibrate on notifications",
                    icon = Icons.Default.Vibration,
                    checked = uiState.vibrationEnabled,
                    onCheckedChange = viewModel::updateVibrationEnabled
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            // Task Defaults
            item {
                SettingsCategory(title = "Task Defaults")
                
                var reminderMinutes by remember { mutableFloatStateOf(uiState.defaultReminderTime.toFloat()) }
                SettingsSlider(
                    title = "Default Reminder Time",
                    description = "Minutes before task is due",
                    icon = Icons.Default.AccessTime,
                    value = reminderMinutes,
                    onValueChange = { reminderMinutes = it },
                    onValueChangeFinished = { viewModel.updateDefaultReminderTime(reminderMinutes.toLong()) },
                    valueRange = 0f..120f,
                    steps = 24,
                    valueText = "${reminderMinutes.toInt()} minutes"
                )
                
                var taskDuration by remember { mutableFloatStateOf(uiState.defaultTaskDuration.toFloat()) }
                SettingsSlider(
                    title = "Default Task Duration",
                    description = "Duration for new tasks",
                    icon = Icons.Default.Timer,
                    value = taskDuration,
                    onValueChange = { taskDuration = it },
                    onValueChangeFinished = { viewModel.updateDefaultTaskDuration(taskDuration.toLong()) },
                    valueRange = 5f..240f,
                    steps = 47,
                    valueText = "${taskDuration.toInt()} minutes"
                )
                
                SettingsClickable(
                    title = "Default View",
                    description = "How tasks are displayed",
                    icon = Icons.Default.Preview,
                    value = uiState.defaultTaskView.name.lowercase().capitalize(),
                    onClick = { showTaskViewDialog = true }
                )
                
                SettingsClickable(
                    title = "Default Sort",
                    description = "How tasks are sorted",
                    icon = Icons.Default.Sort,
                    value = uiState.defaultTaskSort.name.replace("_", " ").lowercase().capitalize(),
                    onClick = { showTaskSortDialog = true }
                )
                
                SettingsClickable(
                    title = "Sort Direction",
                    description = "Order of sorted tasks",
                    icon = Icons.Default.Sort,
                    value = uiState.defaultTaskSortDirection.name.lowercase().capitalize(),
                    onClick = { showSortDirectionDialog = true }
                )
                
                SettingsSwitch(
                    title = "Show Completed Tasks",
                    description = "Display completed tasks in lists",
                    icon = Icons.Default.List,
                    checked = uiState.showCompletedTasks,
                    onCheckedChange = viewModel::updateShowCompletedTasks
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            
            // Pomodoro Settings
            item {
                SettingsCategory(title = "Pomodoro Timer")
                
                var focusTime by remember { mutableFloatStateOf(uiState.defaultPomodoroFocusTime.toFloat()) }
                SettingsSlider(
                    title = "Focus Time",
                    description = "Duration of focus sessions",
                    icon = Icons.Default.Timer,
                    value = focusTime,
                    onValueChange = { focusTime = it },
                    onValueChangeFinished = { viewModel.updatePomodoroFocusTime(focusTime.toInt()) },
                    valueRange = 5f..60f,
                    steps = 11,
                    valueText = "${focusTime.toInt()} minutes"
                )
                
                var breakTime by remember { mutableFloatStateOf(uiState.defaultPomodoroBreakTime.toFloat()) }
                SettingsSlider(
                    title = "Break Time",
                    description = "Duration of short breaks",
                    icon = Icons.Default.Timer,
                    value = breakTime,
                    onValueChange = { breakTime = it },
                    onValueChangeFinished = { viewModel.updatePomodoroBreakTime(breakTime.toInt()) },
                    valueRange = 1f..15f,
                    steps = 14,
                    valueText = "${breakTime.toInt()} minutes"
                )
                
                var longBreakTime by remember { mutableFloatStateOf(uiState.defaultPomodoroLongBreakTime.toFloat()) }
                SettingsSlider(
                    title = "Long Break Time",
                    description = "Duration of long breaks",
                    icon = Icons.Default.Timer,
                    value = longBreakTime,
                    onValueChange = { longBreakTime = it },
                    onValueChangeFinished = { viewModel.updatePomodoroLongBreakTime(longBreakTime.toInt()) },
                    valueRange = 5f..30f,
                    steps = 5,
                    valueText = "${longBreakTime.toInt()} minutes"
                )
                
                var sessionsBeforeLongBreak by remember { mutableFloatStateOf(uiState.defaultPomodoroSessionsBeforeLongBreak.toFloat()) }
                SettingsSlider(
                    title = "Sessions Before Long Break",
                    description = "Number of focus sessions before a long break",
                    icon = Icons.Default.Timer,
                    value = sessionsBeforeLongBreak,
                    onValueChange = { sessionsBeforeLongBreak = it },
                    onValueChangeFinished = { viewModel.updatePomodoroSessionsBeforeLongBreak(sessionsBeforeLongBreak.toInt()) },
                    valueRange = 2f..6f,
                    steps = 4,
                    valueText = "${sessionsBeforeLongBreak.toInt()} sessions"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Dialogs
    if (showTimePickerDialog) {
        TimePickerDialog(
            initialTime = uiState.dailySummaryTime,
            onTimeSelected = {
                viewModel.updateDailySummaryTime(it)
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }
    
    if (showTaskViewDialog) {
        RadioSelectionDialog(
            title = "Default Task View",
            options = TaskView.values().map { it to it.name.replace("_", " ").lowercase().capitalize() },
            selectedOption = uiState.defaultTaskView,
            onOptionSelected = { viewModel.updateDefaultTaskView(it as TaskView) },
            onDismiss = { showTaskViewDialog = false }
        )
    }
    
    if (showTaskSortDialog) {
        RadioSelectionDialog(
            title = "Default Task Sort",
            options = TaskSort.values().map { it to it.name.replace("_", " ").lowercase().capitalize() },
            selectedOption = uiState.defaultTaskSort,
            onOptionSelected = { viewModel.updateDefaultTaskSort(it as TaskSort) },
            onDismiss = { showTaskSortDialog = false }
        )
    }
    
    if (showSortDirectionDialog) {
        RadioSelectionDialog(
            title = "Sort Direction",
            options = SortDirection.values().map { it to it.name.lowercase().capitalize() },
            selectedOption = uiState.defaultTaskSortDirection,
            onOptionSelected = { viewModel.updateDefaultTaskSortDirection(it as SortDirection) },
            onDismiss = { showSortDirectionDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar() {
    TopAppBar(
        title = { Text("Settings") },
        navigationIcon = {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    )
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsSwitch(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) }
        )
    }
}

@Composable
fun SettingsClickable(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SettingsSlider(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TimePicker(state = timePickerState)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val selectedTime = LocalTime.of(
                                timePickerState.hour,
                                timePickerState.minute
                            )
                            onTimeSelected(selectedTime)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun <T> RadioSelectionDialog(
    title: String,
    options: List<Pair<T, String>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                options.forEach { (option, optionText) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = option == selectedOption,
                                onClick = { onOptionSelected(option) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = null
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// String extension to capitalize only the first letter
private fun String.capitalize(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1)
    } else {
        this
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    TaskItTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                SettingsCategory(title = "App Theme")
                
                SettingsSwitch(
                    title = "Dark Mode",
                    description = "Use dark theme",
                    icon = Icons.Default.DarkMode,
                    checked = true,
                    onCheckedChange = {}
                )
                
                SettingsClickable(
                    title = "Default View",
                    description = "How tasks are displayed",
                    icon = Icons.Default.Preview,
                    value = "List",
                    onClick = {}
                )
                
                SettingsSlider(
                    title = "Focus Time",
                    description = "Duration of focus sessions",
                    icon = Icons.Default.Timer,
                    value = 25f,
                    onValueChange = {},
                    onValueChangeFinished = {},
                    valueRange = 5f..60f,
                    steps = 11,
                    valueText = "25 minutes"
                )
            }
        }
    }
} 