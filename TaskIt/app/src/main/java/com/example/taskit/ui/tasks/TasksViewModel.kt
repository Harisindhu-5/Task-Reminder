package com.example.taskit.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskit.data.model.SortDirection
import com.example.taskit.data.model.Task
import com.example.taskit.data.model.TaskPriority
import com.example.taskit.data.model.TaskSort
import com.example.taskit.data.model.TaskStatus
import com.example.taskit.data.model.TaskView
import com.example.taskit.data.repository.CategoryRepository
import com.example.taskit.data.repository.TaskRepository
import com.example.taskit.data.repository.UserPreferencesRepository
import com.example.taskit.work.WorkSchedulerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val sortBy: TaskSort = TaskSort.DUE_DATE,
    val sortDirection: SortDirection = SortDirection.ASCENDING,
    val showCompleted: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

enum class TaskFilter {
    ALL, TODAY, UPCOMING, PRIORITY, CATEGORIES
}

// Utility class for handling date-related operations
object DateUtils {
    fun isToday(dateTime: LocalDateTime): Boolean {
        val today = LocalDate.now()
        return dateTime.toLocalDate() == today
    }

    fun isFuture(dateTime: LocalDateTime): Boolean {
        return dateTime.isAfter(LocalDateTime.now())
    }
}

// Data class to represent a task with its category
data class TaskWithCategory(
    val task: Task,
    val category: Any? // Replace with actual category type
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workSchedulerHelper: WorkSchedulerHelper
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)
    private val _sortBy = MutableStateFlow(TaskSort.DUE_DATE)
    private val _sortDirection = MutableStateFlow(SortDirection.ASCENDING)
    private val _showCompleted = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    
    // Mock getAllTasksWithCategoriesFlow for now
    private val tasksWithCategories = taskRepository.getAllTasksFlow().map { tasks ->
        tasks.map { TaskWithCategory(it, null) }
    }
    
    // Filter tasks based on the selected filter and search query
    private val filteredTasks: Flow<List<Task>> = combine(
        tasksWithCategories,
        _selectedFilter,
        _searchQuery,
        _showCompleted
    ) { tasks, filter, query, showCompleted ->
        val filtered = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.TODAY -> tasks.filter { it.task.dueDate?.let { date ->
                DateUtils.isToday(date)
            } ?: false }
            TaskFilter.UPCOMING -> tasks.filter { it.task.dueDate?.let { date ->
                DateUtils.isFuture(date) && !DateUtils.isToday(date)
            } ?: false }
            TaskFilter.PRIORITY -> tasks.filter { it.task.priority != TaskPriority.LOW }
            TaskFilter.CATEGORIES -> tasks.filter { it.category != null }
        }

        // Apply completed filter
        val completedFiltered = if (showCompleted) {
            filtered
        } else {
            filtered.filter { it.task.status != TaskStatus.COMPLETED }
        }

        // Apply search query
        if (query.isEmpty()) {
            completedFiltered
        } else {
            completedFiltered.filter {
                it.task.title.contains(query, ignoreCase = true) ||
                it.task.description?.contains(query, ignoreCase = true) == true ||
                it.category?.toString()?.contains(query, ignoreCase = true) == true
            }
        }.map { it.task }
    }
    
    // Sort the filtered tasks
    private val sortedTasks: Flow<List<Task>> = combine(
        filteredTasks,
        _sortBy,
        _sortDirection
    ) { tasks, sortBy, direction ->
        sortTasks(tasks, sortBy, direction)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // UI state exposed to the UI
    val uiState: StateFlow<TasksUiState> = combine(
        sortedTasks,
        _isLoading,
        _selectedFilter,
        _sortBy,
        _sortDirection,
        _showCompleted,
        _searchQuery,
        _errorMessage
    ) { flowValues ->
        val tasks = flowValues[0] as List<Task>
        val isLoading = flowValues[1] as Boolean
        val filter = flowValues[2] as TaskFilter
        val sortBy = flowValues[3] as TaskSort
        val direction = flowValues[4] as SortDirection
        val showCompleted = flowValues[5] as Boolean
        val query = flowValues[6] as String
        val error = flowValues[7] as String?
        
        TasksUiState(
            tasks = tasks,
            isLoading = isLoading,
            selectedFilter = filter,
            sortBy = sortBy,
            sortDirection = direction,
            showCompleted = showCompleted,
            searchQuery = query,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasksUiState(isLoading = true)
    )
    
    // Categories flow exposed to the UI
    val categories = categoryRepository.getAllCategoriesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Task view preference flow
    val taskView = userPreferencesRepository.userPreferencesFlow
        .map { it.defaultTaskView }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskView.LIST
        )
    
    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { prefs ->
                _sortBy.value = prefs.defaultTaskSort
                _sortDirection.value = prefs.defaultTaskSortDirection
                _showCompleted.value = prefs.showCompletedTasks
            }
        }
    }
    
    fun setFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
    }
    
    fun setSort(sort: TaskSort) {
        _sortBy.value = sort
    }
    
    fun toggleSortDirection() {
        _sortDirection.value = if (_sortDirection.value == SortDirection.ASCENDING) {
            SortDirection.DESCENDING
        } else {
            SortDirection.ASCENDING
        }
    }
    
    fun toggleShowCompleted() {
        _showCompleted.value = !_showCompleted.value
        viewModelScope.launch {
            userPreferencesRepository.updateShowCompletedTasks(_showCompleted.value)
        }
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun addTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.addTask(task)
                // Schedule reminder for task if it has a due date
                if (task.dueDate != null) {
                    workSchedulerHelper.scheduleTaskReminder(task)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add task: ${e.localizedMessage}"
            }
        }
    }
    
    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.updateTask(task)
                // Update reminder for task (will cancel if completed or no due date)
                workSchedulerHelper.scheduleTaskReminder(task)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update task: ${e.localizedMessage}"
            }
        }
    }
    
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(task)
                // Cancel any reminder for this task
                workSchedulerHelper.cancelTaskReminder(task.id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete task: ${e.localizedMessage}"
            }
        }
    }
    
    fun toggleTaskCompletion(task: Task) {
        val updatedTask = if (task.status == TaskStatus.COMPLETED) {
            task.copy(status = TaskStatus.PENDING, completedDate = null)
        } else {
            task.copy(status = TaskStatus.COMPLETED, completedDate = LocalDateTime.now())
        }
        updateTask(updatedTask)
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    private fun sortTasks(tasks: List<Task>, sortBy: TaskSort, direction: SortDirection): List<Task> {
        val comparator = when (sortBy) {
            TaskSort.TITLE -> compareBy<Task> { it.title }
            TaskSort.DUE_DATE -> compareBy<Task> { it.dueDate ?: LocalDateTime.MAX }
            TaskSort.PRIORITY -> compareByDescending<Task> { it.priority.ordinal }
            TaskSort.CREATION_DATE -> compareBy<Task> { it.createdAt }
            TaskSort.CATEGORY -> compareBy<Task> { it.categoryId ?: "" }
        }
        
        return if (direction == SortDirection.ASCENDING) {
            tasks.sortedWith(comparator)
        } else {
            tasks.sortedWith(comparator.reversed())
        }
    }
    
    // Update task view preference
    fun setTaskView(view: TaskView) {
        viewModelScope.launch {
            userPreferencesRepository.updateDefaultTaskView(view)
        }
    }
    
    // New method to add a category
    fun addCategory(category: com.example.taskit.data.model.Category) {
        viewModelScope.launch {
            try {
                categoryRepository.addCategory(category)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add category: ${e.localizedMessage}"
            }
        }
    }
} 