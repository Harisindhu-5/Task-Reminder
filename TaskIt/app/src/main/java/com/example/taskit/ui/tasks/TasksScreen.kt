package com.example.taskit.ui.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskit.data.model.SortDirection
import com.example.taskit.data.model.Task
import com.example.taskit.data.model.TaskPriority
import com.example.taskit.data.model.TaskSort
import com.example.taskit.data.model.TaskStatus
import com.example.taskit.data.model.TaskView
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.border

@Composable
fun TasksScreen(
    viewModel: TasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    
    // Current view type
    val currentView by viewModel.taskView.collectAsStateWithLifecycle()
    var showViewPicker by remember { mutableStateOf(false) }
    
    // Show error snackbar if needed
    if (uiState.errorMessage != null) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = uiState.errorMessage ?: "An error occurred"
            )
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TasksTopBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::setSearchQuery,
                currentView = currentView,
                onViewClick = { showViewPicker = true }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddTaskDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Add Task") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TasksFilterBar(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = viewModel::setFilter,
                showCompleted = uiState.showCompleted,
                onToggleShowCompleted = viewModel::toggleShowCompleted,
                sortBy = uiState.sortBy,
                sortDirection = uiState.sortDirection,
                onSortSelected = viewModel::setSort,
                onToggleSortDirection = viewModel::toggleSortDirection
            )
            
            // Show different views based on selection
            when (currentView) {
                TaskView.LIST -> TasksListView(
                    tasks = uiState.tasks,
                    onTaskClick = { taskToEdit = it },
                    onTaskCheckToggle = viewModel::toggleTaskCompletion,
                    onDeleteTask = viewModel::deleteTask
                )
                TaskView.GRID -> TasksGridView(
                    tasks = uiState.tasks,
                    onTaskClick = { taskToEdit = it },
                    onTaskCheckToggle = viewModel::toggleTaskCompletion,
                    onDeleteTask = viewModel::deleteTask
                )
                TaskView.CALENDAR -> TasksCalendarView(
                    tasks = uiState.tasks,
                    onTaskClick = { taskToEdit = it },
                    onTaskCheckToggle = viewModel::toggleTaskCompletion,
                    onDeleteTask = viewModel::deleteTask
                )
            }
        }
    }
    
    // View selection dialog
    if (showViewPicker) {
        Dialog(onDismissRequest = { showViewPicker = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select View",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // List view option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setTaskView(TaskView.LIST)
                                showViewPicker = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (currentView == TaskView.LIST) 
                                   MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("List View")
                    }
                    
                    // Grid view option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setTaskView(TaskView.GRID)
                                showViewPicker = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (currentView == TaskView.GRID) 
                                   MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Grid View")
                    }
                    
                    // Calendar view option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setTaskView(TaskView.CALENDAR)
                                showViewPicker = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (currentView == TaskView.CALENDAR) 
                                   MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Calendar View")
                    }
                }
            }
        }
    }
    
    // Add/Edit Task Dialog
    if (showAddTaskDialog || taskToEdit != null) {
        TaskDialog(
            task = taskToEdit,
            onDismiss = { 
                showAddTaskDialog = false
                taskToEdit = null
            },
            onSaveTask = { task ->
                if (taskToEdit != null) {
                    viewModel.updateTask(task)
                } else {
                    viewModel.addTask(task)
                }
                showAddTaskDialog = false
                taskToEdit = null
            },
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    currentView: TaskView,
    onViewClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Tasks") },
        actions = {
            // View selection icon
            IconButton(onClick = onViewClick) {
                when (currentView) {
                    TaskView.LIST -> Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "List View"
                    )
                    TaskView.GRID -> Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "Grid View"
                    )
                    TaskView.CALENDAR -> Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar View"
                    )
                }
            }
            
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search tasks") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(end = 8.dp),
                singleLine = true
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksFilterBar(
    selectedFilter: TaskFilter,
    onFilterSelected: (TaskFilter) -> Unit,
    showCompleted: Boolean,
    onToggleShowCompleted: () -> Unit,
    sortBy: TaskSort,
    sortDirection: SortDirection,
    onSortSelected: (TaskSort) -> Unit,
    onToggleSortDirection: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == TaskFilter.ALL,
                    onClick = { onFilterSelected(TaskFilter.ALL) },
                    label = { Text("All") },
                    leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) }
                )
            }
            
            item {
                FilterChip(
                    selected = selectedFilter == TaskFilter.TODAY,
                    onClick = { onFilterSelected(TaskFilter.TODAY) },
                    label = { Text("Today") },
                    leadingIcon = { Icon(Icons.Default.Today, contentDescription = null) }
                )
            }
            
            item {
                FilterChip(
                    selected = selectedFilter == TaskFilter.UPCOMING,
                    onClick = { onFilterSelected(TaskFilter.UPCOMING) },
                    label = { Text("Upcoming") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                )
            }
            
            item {
                FilterChip(
                    selected = selectedFilter == TaskFilter.PRIORITY,
                    onClick = { onFilterSelected(TaskFilter.PRIORITY) },
                    label = { Text("Priority") },
                    leadingIcon = { Icon(Icons.Default.PriorityHigh, contentDescription = null) }
                )
            }
            
            item {
                FilterChip(
                    selected = selectedFilter == TaskFilter.CATEGORIES,
                    onClick = { onFilterSelected(TaskFilter.CATEGORIES) },
                    label = { Text("Categories") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
                )
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = showCompleted,
                    onCheckedChange = { onToggleShowCompleted() }
                )
                Text("Show completed")
            }
            
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort")
                }
                
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Title") },
                        onClick = { 
                            onSortSelected(TaskSort.TITLE)
                            showSortMenu = false
                        },
                        leadingIcon = { 
                            if (sortBy == TaskSort.TITLE) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                            }
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Due Date") },
                        onClick = { 
                            onSortSelected(TaskSort.DUE_DATE)
                            showSortMenu = false
                        },
                        leadingIcon = { 
                            if (sortBy == TaskSort.DUE_DATE) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                            }
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Priority") },
                        onClick = { 
                            onSortSelected(TaskSort.PRIORITY)
                            showSortMenu = false
                        },
                        leadingIcon = { 
                            if (sortBy == TaskSort.PRIORITY) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                            }
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Creation Date") },
                        onClick = { 
                            onSortSelected(TaskSort.CREATION_DATE)
                            showSortMenu = false
                        },
                        leadingIcon = { 
                            if (sortBy == TaskSort.CREATION_DATE) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                            }
                        }
                    )
                    
                    HorizontalDivider()
                    
                    DropdownMenuItem(
                        text = { 
                            Text(
                                if (sortDirection == SortDirection.ASCENDING) 
                                    "Ascending" else "Descending"
                            )
                        },
                        onClick = { 
                            onToggleSortDirection()
                            showSortMenu = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TasksListView(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onTaskCheckToggle: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No tasks found",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = tasks,
                key = { it.id }
            ) { task ->
                TaskItem(
                    task = task,
                    onClick = { onTaskClick(task) },
                    onCheckToggle = { onTaskCheckToggle(task) },
                    onDelete = { onDeleteTask(task) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TasksGridView(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onTaskCheckToggle: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No tasks found",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        // Grid with 2 columns
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = tasks,
                key = { it.id }
            ) { task ->
                TaskGridItem(
                    task = task,
                    onClick = { onTaskClick(task) },
                    onCheckToggle = { onTaskCheckToggle(task) },
                    onDelete = { onDeleteTask(task) }
                )
            }
        }
    }
}

@Composable
fun TaskGridItem(
    task: Task,
    onClick: () -> Unit,
    onCheckToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Title and checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.status == TaskStatus.COMPLETED) 
                        TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.status == TaskStatus.COMPLETED)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onCheckToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    if (task.status == TaskStatus.COMPLETED) {
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
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            if (task.description.isNotEmpty()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bottom row with date and priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Due date
                if (task.dueDate != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
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
                            color = getTaskDueDateColor(task.dueDate),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                // Priority star
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Priority: ${task.priority}",
                    tint = getTaskPriorityColor(task.priority),
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete task",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onCheckToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCheckToggle,
                modifier = Modifier.size(24.dp)
            ) {
                if (task.status == TaskStatus.COMPLETED) {
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
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.status == TaskStatus.COMPLETED) 
                        TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.status == TaskStatus.COMPLETED)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurface
                )
                
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
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
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Always show a priority indicator, with different colors based on priority
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Priority: ${task.priority}",
                        tint = getTaskPriorityColor(task.priority),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete task"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    task: Task? = null,
    onDismiss: () -> Unit,
    onSaveTask: (Task) -> Unit,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val isNewTask = task == null
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: TaskPriority.MEDIUM) }
    var dueDate by remember { mutableStateOf(task?.dueDate?.toLocalDate() ?: LocalDate.now()) }
    var dueTime by remember { mutableStateOf(task?.dueDate?.toLocalTime() ?: LocalTime.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var hasDueDate by remember { mutableStateOf(task?.dueDate != null) }
    
    // Category state
    var selectedCategoryId by remember { mutableStateOf(task?.categoryId) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    
    // Collect categories
    val categories by viewModel.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    
    // Material3 DatePicker implementation
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            dueDate = selectedDate
                        }
                        showDatePicker = false
                        showTimePicker = true
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Material3 TimePicker implementation
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = dueTime.hour,
            initialMinute = dueTime.minute
        )
        
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Time",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TimePicker(state = timePickerState)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel")
                        }
                        
                        TextButton(
                            onClick = {
                                dueTime = LocalTime.of(
                                    timePickerState.hour,
                                    timePickerState.minute
                                )
                                showTimePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
    
    // Category Picker Dialog
    if (showCategoryPicker) {
        Dialog(onDismissRequest = { showCategoryPicker = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Select Category",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (categories.isEmpty()) {
                        Text(
                            text = "No categories available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(300.dp)
                        ) {
                            item {
                                // Option to clear category
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCategoryId = null
                                            showCategoryPicker = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "None",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                HorizontalDivider()
                            }
                            
                            items(categories) { category ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCategoryId = category.id
                                            showCategoryPicker = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Category color indicator
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                color = Color(category.color),
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (category.id == selectedCategoryId) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showCategoryPicker = false }
                        ) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = {
                                showCategoryPicker = false
                                showNewCategoryDialog = true
                            }
                        ) {
                            Text("New Category")
                        }
                    }
                }
            }
        }
    }
    
    // New Category Dialog
    if (showNewCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf(0xFFF44336.toInt()) } // Default red color
        
        val colorOptions = listOf(
            0xFFF44336.toInt(), // Red
            0xFFE91E63.toInt(), // Pink
            0xFF9C27B0.toInt(), // Purple
            0xFF3F51B5.toInt(), // Indigo
            0xFF2196F3.toInt(), // Blue
            0xFF03A9F4.toInt(), // Light Blue
            0xFF00BCD4.toInt(), // Cyan
            0xFF009688.toInt(), // Teal
            0xFF4CAF50.toInt(), // Green
            0xFF8BC34A.toInt(), // Light Green
            0xFFCDDC39.toInt(), // Lime
            0xFFFFEB3B.toInt(), // Yellow
            0xFFFFC107.toInt(), // Amber
            0xFFFF9800.toInt(), // Orange
            0xFF795548.toInt(), // Brown
            0xFF607D8B.toInt()  // Blue Grey
        )
        
        Dialog(onDismissRequest = { showNewCategoryDialog = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "New Category",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Select Color",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Color grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(colorOptions) { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .clickable { selectedColor = color }
                                    .border(
                                        width = 2.dp,
                                        color = if (selectedColor == color) 
                                            MaterialTheme.colorScheme.primary 
                                        else Color.Transparent,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showNewCategoryDialog = false }
                        ) {
                            Text("Cancel")
                        }
                        
                        TextButton(
                            onClick = {
                                if (newCategoryName.isNotEmpty()) {
                                    // Create and add the new category
                                    val newCategory = com.example.taskit.data.model.Category(
                                        name = newCategoryName,
                                        color = selectedColor
                                    )
                                    viewModel.addCategory(newCategory)
                                    // Set as selected category
                                    selectedCategoryId = newCategory.id
                                    showNewCategoryDialog = false
                                }
                            },
                            enabled = newCategoryName.isNotEmpty()
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
    
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
                    text = if (isNewTask) "Add New Task" else "Edit Task",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
                
                // Category selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryPicker = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Category",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Show selected category or "None"
                    val selectedCategory = categories.find { it.id == selectedCategoryId }
                    if (selectedCategory != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = Color(selectedCategory.color),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedCategory.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Text(
                            text = "None",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Due date & time selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasDueDate,
                        onCheckedChange = { hasDueDate = it }
                    )
                    Text("Set due date and time", 
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (hasDueDate) {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Select date"
                            )
                        }
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = "Select time"
                            )
                        }
                    }
                }
                
                if (hasDueDate) {
                    Text(
                        text = "Due: ${dueDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))} at ${dueTime.format(DateTimeFormatter.ofPattern("h:mm a"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Priority selector
                Text("Priority", style = MaterialTheme.typography.bodyLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PriorityButton(
                        isSelected = priority == TaskPriority.LOW,
                        color = Color.Green,
                        text = "Low",
                        onClick = { priority = TaskPriority.LOW }
                    )
                    
                    PriorityButton(
                        isSelected = priority == TaskPriority.MEDIUM,
                        color = Color(0xFF1976D2),
                        text = "Medium",
                        onClick = { priority = TaskPriority.MEDIUM }
                    )
                    
                    PriorityButton(
                        isSelected = priority == TaskPriority.HIGH,
                        color = Color.Red,
                        text = "High",
                        onClick = { priority = TaskPriority.HIGH }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }
                    
                    androidx.compose.material3.TextButton(
                        onClick = {
                            if (title.isNotEmpty()) {
                                val dueDateValue = if (hasDueDate) {
                                    LocalDateTime.of(dueDate, dueTime)
                                } else {
                                    null
                                }
                                
                                val updatedTask = (task ?: Task(title = "")).copy(
                                    title = title,
                                    description = description,
                                    priority = priority,
                                    dueDate = dueDateValue,
                                    categoryId = selectedCategoryId
                                )
                                onSaveTask(updatedTask)
                            }
                        },
                        enabled = title.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityButton(
    isSelected: Boolean,
    color: Color,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) color else color.copy(alpha = 0.2f))
        )
        Text(text, style = MaterialTheme.typography.bodySmall)
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

// Helper functions
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

@Composable
fun TasksCalendarView(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onTaskCheckToggle: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val tasksForSelectedDate = tasks.filter { task -> 
        task.dueDate?.toLocalDate()?.isEqual(selectedDate) == true
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Calendar header with month and navigation
        CalendarHeader(
            selectedDate = selectedDate,
            onPreviousMonth = { selectedDate = selectedDate.minusMonths(1) },
            onNextMonth = { selectedDate = selectedDate.plusMonths(1) }
        )
        
        // Calendar grid
        CalendarGrid(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            tasks = tasks
        )
        
        // Tasks for selected date
        Text(
            text = "Tasks for ${selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        if (tasksForSelectedDate.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks for this date",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = tasksForSelectedDate,
                    key = { it.id }
                ) { task ->
                    TaskItem(
                        task = task,
                        onClick = { onTaskClick(task) },
                        onCheckToggle = { onTaskCheckToggle(task) },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarHeader(
    selectedDate: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous Month"
            )
        }
        
        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleLarge
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next Month"
            )
        }
    }
}

@Composable
fun CalendarGrid(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    tasks: List<Task>
) {
    // Calculate first day of month and number of days
    val firstDayOfMonth = selectedDate.withDayOfMonth(1)
    val lastDayOfMonth = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth())
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 for Sunday, 6 for Saturday
    
    // Create a map of dates with tasks
    val datesWithTasks = tasks
        .mapNotNull { it.dueDate?.toLocalDate() }
        .groupBy { it }
        .mapValues { it.value.size }
    
    // Days of week header
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(1/7f),
                textAlign = TextAlign.Center
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    // Calendar days grid
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Calculate total number of cells to display (days + empty spaces)
        val totalCells = firstDayOfWeek + lastDayOfMonth.dayOfMonth
        val rows = (totalCells + 6) / 7 // Round up to full weeks
        
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    val dayOfMonth = index - firstDayOfWeek + 1
                    
                    if (dayOfMonth in 1..lastDayOfMonth.dayOfMonth) {
                        val date = firstDayOfMonth.plusDays((dayOfMonth - 1).toLong())
                        val isSelected = date.isEqual(selectedDate)
                        val hasTask = date in datesWithTasks.keys
                        val taskCount = datesWithTasks[date] ?: 0
                        
                        CalendarDay(
                            date = date,
                            isSelected = isSelected,
                            hasTask = hasTask,
                            taskCount = taskCount,
                            onDateClick = { onDateSelected(date) }
                        )
                    } else {
                        // Empty space
                        Spacer(modifier = Modifier.fillMaxWidth(1/7f))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    hasTask: Boolean,
    taskCount: Int,
    onDateClick: () -> Unit
) {
    val today = LocalDate.now()
    val isToday = date.isEqual(today)
    
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        else -> Color.Transparent
    }
    
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth(1/7f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onDateClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            
            if (hasTask) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )
                
                if (taskCount > 1) {
                    Text(
                        text = "$taskCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
} 