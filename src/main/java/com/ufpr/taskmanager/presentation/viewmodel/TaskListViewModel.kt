package com.ufpr.taskmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ufpr.taskmanager.data.model.ImportanceLevel
import com.ufpr.taskmanager.data.model.ProgressState
import com.ufpr.taskmanager.data.model.Task
import com.ufpr.taskmanager.data.repository.UserTaskRepository
import com.ufpr.taskmanager.domain.usecase.AddNewTaskUseCase
import com.ufpr.taskmanager.domain.usecase.MarkTaskAsDoneUseCase
import com.ufpr.taskmanager.domain.usecase.OrganizeAndFilterTasksUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Data class para agrupar todas as opções de filtro e ordenação
data class FilterOptions(
    val searchQuery: String = "",
    val importanceFilter: ImportanceLevel? = null,
    val statusFilter: ProgressState? = null,
    val sortByCompletionDate: Boolean = false
)

class TaskListViewModel(
    private val taskRepository: UserTaskRepository,
    private val addNewTask: AddNewTaskUseCase,
    private val markAsDone: MarkTaskAsDoneUseCase,
    private val organizeAndFilterTasks: OrganizeAndFilterTasksUseCase = OrganizeAndFilterTasksUseCase()
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    // Um único StateFlow para todas as opções de filtro
    private val _filterOptions = MutableStateFlow(FilterOptions())

    val taskListUiState: StateFlow<TaskListUiState> =
        combine(
            _tasks,
            _isLoading,
            _error,
            _filterOptions
        ) { tasks, isLoading, error, filters ->
            if (isLoading) {
                TaskListUiState.Loading
            } else if (error != null) {
                TaskListUiState.Error(error)
            } else {
                val processedTasks = organizeAndFilterTasks(
                    tasks = tasks,
                    searchQuery = filters.searchQuery,
                    filterByImportance = filters.importanceFilter,
                    filterByStatus = filters.statusFilter,
                    sortByCompletionDate = filters.sortByCompletionDate
                )
                TaskListUiState.Success(
                    tasks = processedTasks,
                    filters = filters
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskListUiState.Loading
        )

    init {
        refreshTasks()
    }

    fun refreshTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null // Limpa erros anteriores
            try {
                _tasks.value = taskRepository.fetchAllUserTasks()
            } catch (e: Exception) {
                _error.value = "Falha ao carregar tarefas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Métodos para atualizar as opções de filtro
    fun onSearchQueryChanged(query: String) {
        _filterOptions.update { it.copy(searchQuery = query) }
    }

    fun onImportanceFilterChanged(importance: ImportanceLevel?) {
        _filterOptions.update { it.copy(importanceFilter = importance) }
    }

    fun onStatusFilterChanged(status: ProgressState?) {
        _filterOptions.update { it.copy(statusFilter = status) }
    }

    fun onSortByCompletionDateChanged(enabled: Boolean) {
        _filterOptions.update { it.copy(sortByCompletionDate = enabled) }
    }

    fun getTaskById(taskId: String): Task? {
        return _tasks.value.find { it.id == taskId }
    }

    fun submitNewTask(task: Task) {
        viewModelScope.launch {
            try {
                addNewTask.execute(task)
                refreshTasks()
            } catch (e: Exception) {
                _error.value = "Falha ao adicionar tarefa: ${e.message}"
            }
        }
    }

    fun updateTaskStatus(task: Task, newStatus: ProgressState) {
        viewModelScope.launch {
            var updatedTask = task.copy(currentStatus = newStatus)
            if (newStatus == ProgressState.UNDERWAY && task.initiatedAt == null) {
                updatedTask = updatedTask.copy(initiatedAt = System.currentTimeMillis())
            }
            if (newStatus == ProgressState.CONCLUDED) {
                val startTime = task.initiatedAt ?: System.currentTimeMillis()
                updatedTask = updatedTask.copy(
                    initiatedAt = startTime,
                    completedAt = System.currentTimeMillis()
                )
            }
            updateExistingTask(updatedTask)
        }
    }

    fun removeTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.eraseTask(taskId)
                refreshTasks()
            } catch (e: Exception) {
                _error.value = "Falha ao deletar tarefa: ${e.message}"
            }
        }
    }

    fun updateExistingTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.modifyTask(task)
                refreshTasks()
            } catch (e: Exception) {
                _error.value = "Falha ao atualizar tarefa: ${e.message}"
            }
        }
    }
}

// Estado da UI atualizado para usar a classe FilterOptions
sealed class TaskListUiState {
    object Loading : TaskListUiState()
    data class Success(
        val tasks: List<Task>,
        val filters: FilterOptions
    ) : TaskListUiState()
    data class Error(val message: String) : TaskListUiState()
}