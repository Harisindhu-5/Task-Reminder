package com.example.taskit.ui.reports

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskit.ui.theme.TaskItTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.min

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Show error if needed
    if (uiState.errorMessage != null) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(uiState.errorMessage ?: "An error occurred")
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ReportsTopBar(
                selectedDate = uiState.selectedDate,
                onDateClick = { showDatePicker = true }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Time period selector
            TimePeriodSelector(
                selectedPeriod = uiState.timePeriod,
                onPeriodSelected = viewModel::setTimePeriod,
                modifier = Modifier.padding(16.dp)
            )
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Task Stats
                TaskStatsSection(
                    stats = uiState.taskStats,
                    modifier = Modifier.padding(16.dp)
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                
                // Habit Stats
                HabitStatsSection(
                    stats = uiState.habitStats,
                    modifier = Modifier.padding(16.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Date Picker
    if (showDatePicker) {
        CustomDatePickerDialog(
            selectedDate = uiState.selectedDate,
            onDateSelected = { 
                viewModel.setSelectedDate(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsTopBar(
    selectedDate: LocalDate,
    onDateClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Reports & Analytics") },
        actions = {
            IconButton(onClick = onDateClick) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        SegmentedButton(
            selected = selectedPeriod == TimePeriod.DAY,
            onClick = { onPeriodSelected(TimePeriod.DAY) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4)
        ) {
            Text("Day")
        }
        
        SegmentedButton(
            selected = selectedPeriod == TimePeriod.WEEK,
            onClick = { onPeriodSelected(TimePeriod.WEEK) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4)
        ) {
            Text("Week")
        }
        
        SegmentedButton(
            selected = selectedPeriod == TimePeriod.MONTH,
            onClick = { onPeriodSelected(TimePeriod.MONTH) },
            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4)
        ) {
            Text("Month")
        }
        
        SegmentedButton(
            selected = selectedPeriod == TimePeriod.YEAR,
            onClick = { onPeriodSelected(TimePeriod.YEAR) },
            shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4)
        ) {
            Text("Year")
        }
    }
}

@Composable
fun TaskStatsSection(
    stats: TaskStats,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Task Statistics",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Task completion stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard(
                title = "Completed",
                value = stats.completedCount.toString(),
                icon = Icons.Default.CheckCircle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            StatCard(
                title = "Pending",
                value = stats.pendingCount.toString(),
                icon = Icons.Default.Timer,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            StatCard(
                title = "Overdue",
                value = stats.overDueCount.toString(),
                icon = Icons.Default.FilterList,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Completion rate
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Task Completion Rate",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressChart(
                        progress = stats.completionRate,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "${(stats.completionRate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                Text(
                    text = "Average completion time: ${formatMinutes(stats.averageCompletionTime.toLong())}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Task priority distribution
        if (stats.tasksByPriority.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tasks by Priority",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    val totalTasks = stats.tasksByPriority.values.sum().toFloat()
                    
                    stats.tasksByPriority.forEach { (priority, count) ->
                        val percentage = if (totalTasks > 0) count / totalTasks else 0f
                        
                        val color = when (priority) {
                            "HIGH" -> Color.Red
                            "MEDIUM" -> Color.Blue
                            "LOW" -> Color.Green
                            else -> MaterialTheme.colorScheme.primary
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = priority,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(80.dp)
                            )
                            
                            LinearProgressIndicator(
                                progress = { percentage },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp),
                                color = color
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitStatsSection(
    stats: HabitStats,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Habit Statistics",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Habit summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard(
                title = "Total",
                value = stats.totalHabits.toString(),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            StatCard(
                title = "Daily",
                value = stats.dailyHabits.toString(),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            StatCard(
                title = "Weekly",
                value = stats.weeklyHabits.toString(),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Habit streaks and completion
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Habit Performance",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${(stats.averageCompletion * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Completion",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Divider(
                        modifier = Modifier
                            .height(50.dp)
                            .width(1.dp)
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stats.currentStreak.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Current Streak",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Divider(
                        modifier = Modifier
                            .height(50.dp)
                            .width(1.dp)
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stats.bestStreak.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Best Streak",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // If there are completions data, show a simple bar chart
        if (stats.completionsByDay.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Habit Completions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    SimpleBarChart(
                        data = stats.completionsByDay,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CircularProgressChart(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val sweepAngle = 360f * min(progress, 1f)
    
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
    ) {
        // Background circle
        drawArc(
            color = color.copy(alpha = 0.2f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 16f)
        )
        
        // Progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 16f)
        )
    }
}

@Composable
fun SimpleBarChart(
    data: Map<LocalDate, Int>,
    modifier: Modifier = Modifier
) {
    val sortedData = data.entries.sortedBy { it.key }
    val maxValue = data.values.maxOrNull()?.toFloat() ?: 1f
    
    // Get the color outside of the Canvas drawing scope
    val barColor = MaterialTheme.colorScheme.primary
    
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val barWidth = size.width / sortedData.size.coerceAtLeast(1)
            val maxHeight = size.height * 0.8f
            
            // Draw bars
            sortedData.forEachIndexed { index, (_, value) ->
                val barHeight = (value / maxValue) * maxHeight
                
                // Draw the bar
                drawRect(
                    color = barColor,
                    topLeft = Offset(
                        x = index * barWidth + barWidth * 0.1f,
                        y = size.height - barHeight
                    ),
                    size = Size(
                        width = barWidth * 0.8f,
                        height = barHeight
                    )
                )
            }
        }
        
        // Date labels (only show a few)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dataSize = sortedData.size
            val labelCount = 3
            
            if (dataSize > 0) {
                // First date
                FormattedDateText(
                    date = sortedData.first().key,
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                // Middle date (if enough data)
                if (dataSize > 2) {
                    FormattedDateText(
                        date = sortedData[dataSize / 2].key
                    )
                }
                
                // Last date
                FormattedDateText(
                    date = sortedData.last().key,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// Helper functions
private fun formatDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("MM/dd"))
}

@Composable
private fun FormattedDateText(
    date: LocalDate,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodySmall
) {
    Text(
        text = formatDate(date),
        style = style,
        modifier = modifier
    )
}

private fun formatMinutes(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    
    return if (hours > 0) {
        "$hours h $mins min"
    } else {
        "$mins min"
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ReportsScreenPreview() {
    TaskItTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                StatCard(
                    title = "Completed",
                    value = "24",
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressChart(
                        progress = 0.75f,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "75%",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
} 