package com.example.taskit.ui.habits

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskit.data.model.Habit
import com.example.taskit.ui.theme.TaskItTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
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
            HabitsTopBar(
                selectedDate = uiState.selectedDate,
                onDateClick = { showDatePicker = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddHabitDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filters
            HabitsFilterBar(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = viewModel::setFilter,
                modifier = Modifier.padding(16.dp)
            )
            
            // Habits list
            if (uiState.filteredHabits.isEmpty()) {
                EmptyHabitsMessage(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            } else {
                HabitsList(
                    habits = uiState.filteredHabits,
                    onHabitClick = { habitToEdit = it.habit },
                    onToggleHabit = { viewModel.toggleHabitCompletion(it.habit) },
                    onDeleteHabit = { viewModel.deleteHabit(it.habit) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    
    // Add/Edit Habit Dialog
    if (showAddHabitDialog || habitToEdit != null) {
        HabitDialog(
            habit = habitToEdit,
            onDismiss = { 
                showAddHabitDialog = false
                habitToEdit = null
            },
            onSaveHabit = { habit ->
                if (habitToEdit != null) {
                    viewModel.updateHabit(habit)
                } else {
                    viewModel.addHabit(habit)
                }
                showAddHabitDialog = false
                habitToEdit = null
            }
        )
    }
    
    // Date Picker
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = uiState.selectedDate,
            onDateSelected = { 
                viewModel.setDate(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsTopBar(
    selectedDate: LocalDate,
    onDateClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Habits") },
        actions = {
            IconButton(onClick = onDateClick) {
                Icon(Icons.Default.Today, contentDescription = "Select Date")
            }
            Text(
                text = formatDate(selectedDate),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsFilterBar(
    selectedFilter: HabitFilter,
    onFilterSelected: (HabitFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        SegmentedButton(
            selected = selectedFilter == HabitFilter.ALL,
            onClick = { onFilterSelected(HabitFilter.ALL) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 5)
        ) {
            Text("All")
        }
        
        SegmentedButton(
            selected = selectedFilter == HabitFilter.DAILY,
            onClick = { onFilterSelected(HabitFilter.DAILY) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 5)
        ) {
            Text("Daily")
        }
        
        SegmentedButton(
            selected = selectedFilter == HabitFilter.WEEKLY,
            onClick = { onFilterSelected(HabitFilter.WEEKLY) },
            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 5)
        ) {
            Text("Weekly")
        }
        
        SegmentedButton(
            selected = selectedFilter == HabitFilter.COMPLETED,
            onClick = { onFilterSelected(HabitFilter.COMPLETED) },
            shape = SegmentedButtonDefaults.itemShape(index = 3, count = 5)
        ) {
            Text("Done")
        }
        
        SegmentedButton(
            selected = selectedFilter == HabitFilter.UNCOMPLETED,
            onClick = { onFilterSelected(HabitFilter.UNCOMPLETED) },
            shape = SegmentedButtonDefaults.itemShape(index = 4, count = 5)
        ) {
            Text("Todo")
        }
    }
}

@Composable
fun HabitsList(
    habits: List<HabitWithCompletions>,
    onHabitClick: (HabitWithCompletions) -> Unit,
    onToggleHabit: (HabitWithCompletions) -> Unit,
    onDeleteHabit: (HabitWithCompletions) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = habits,
            key = { it.habit.id }
        ) { habitWithCompletions ->
            HabitItem(
                habitWithCompletions = habitWithCompletions,
                onClick = { onHabitClick(habitWithCompletions) },
                onToggle = { onToggleHabit(habitWithCompletions) },
                onDelete = { onDeleteHabit(habitWithCompletions) }
            )
        }
    }
}

@Composable
fun HabitItem(
    habitWithCompletions: HabitWithCompletions,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithCompletions.habit
    val completed = habitWithCompletions.completedToday
    val progress = habitWithCompletions.completion
    val streak = habitWithCompletions.streak
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "HabitProgress"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${habit.frequency.name.lowercase().capitalize()} habit",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Row {
                    IconButton(
                        onClick = onToggle
                    ) {
                        if (completed) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = "Mark incomplete",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Circle,
                                contentDescription = "Mark complete"
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onDelete
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete habit"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% completion",
                    style = MaterialTheme.typography.bodySmall
                )
                
                if (streak > 0) {
                    Text(
                        text = "$streak day streak 🔥",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDialog(
    habit: Habit? = null,
    onDismiss: () -> Unit,
    onSaveHabit: (Habit) -> Unit
) {
    val isNewHabit = habit == null
    var name by remember { mutableStateOf(habit?.name ?: "") }
    var description by remember { mutableStateOf(habit?.description ?: "") }
    var frequency by remember { mutableStateOf(habit?.frequency ?: Habit.Frequency.DAILY) }
    
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isNewHabit) "Add New Habit" else "Edit Habit",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Frequency selector
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = frequency == Habit.Frequency.DAILY,
                        onClick = { frequency = Habit.Frequency.DAILY },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) {
                        Text("Daily")
                    }
                    
                    SegmentedButton(
                        selected = frequency == Habit.Frequency.WEEKLY,
                        onClick = { frequency = Habit.Frequency.WEEKLY },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) {
                        Text("Weekly")
                    }
                    
                    SegmentedButton(
                        selected = frequency == Habit.Frequency.MONTHLY,
                        onClick = { frequency = Habit.Frequency.MONTHLY },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) {
                        Text("Monthly")
                    }
                }
                
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
                            if (name.isNotEmpty()) {
                                val updatedHabit = (habit ?: Habit(name = "")).copy(
                                    name = name,
                                    description = description,
                                    frequency = frequency
                                )
                                onSaveHabit(updatedHabit)
                            }
                        },
                        enabled = name.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
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

@Composable
fun EmptyHabitsMessage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No habits found",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tap the + button to add your first habit",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Helper functions
private fun formatDate(date: LocalDate): String {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val yesterday = today.minusDays(1)
    
    return when {
        date.isEqual(today) -> "Today"
        date.isEqual(tomorrow) -> "Tomorrow"
        date.isEqual(yesterday) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HabitsScreenPreview() {
    TaskItTheme {
        Surface {
            val habit = Habit(
                name = "Morning Meditation",
                description = "Practice mindfulness for 10 minutes",
                frequency = Habit.Frequency.DAILY
            )
            
            val habitWithCompletions = HabitWithCompletions(
                habit = habit,
                completedToday = true,
                streak = 5,
                completion = 0.8f
            )
            
            Column(modifier = Modifier.padding(16.dp)) {
                HabitItem(
                    habitWithCompletions = habitWithCompletions,
                    onClick = {},
                    onToggle = {},
                    onDelete = {}
                )
            }
        }
    }
} 