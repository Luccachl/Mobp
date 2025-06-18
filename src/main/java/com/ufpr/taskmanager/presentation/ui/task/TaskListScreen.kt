package com.ufpr.taskmanager.presentation.ui.task

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ufpr.taskmanager.data.model.ImportanceLevel
import com.ufpr.taskmanager.data.model.ProgressState
import com.ufpr.taskmanager.data.model.Task
import com.ufpr.taskmanager.presentation.navigation.Screen
import com.ufpr.taskmanager.presentation.viewmodel.AuthenticationViewModel
import com.ufpr.taskmanager.presentation.viewmodel.TaskListUiState
import com.ufpr.taskmanager.presentation.viewmodel.TaskListViewModel
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    authViewModel: AuthenticationViewModel,
    taskListViewModel: TaskListViewModel
) {
    val tasksUiState by taskListViewModel.taskListUiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Task?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

    showDeleteDialog?.let { task ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Você tem certeza que deseja deletar a tarefa '${task.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        taskListViewModel.removeTask(task.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Deletar") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            }
        )
    }

    if (showFilterDialog) {
        FilterDialog(
            currentState = tasksUiState,
            onDismiss = { showFilterDialog = false },
            viewModel = taskListViewModel
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas Tarefas") },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                    }
                    IconButton(onClick = { authViewModel.endSession() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Sair")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddEditTask.route) }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar Tarefa")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = tasksUiState) {
                is TaskListUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is TaskListUiState.Success -> {
                    OutlinedTextField(
                        value = state.filters.searchQuery,
                        onValueChange = { taskListViewModel.onSearchQueryChanged(it) },
                        label = { Text("Pesquisar tarefas...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )

                    if (state.tasks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhuma tarefa encontrada.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.tasks, key = { it.id }) { task ->
                                TaskItem(
                                    task = task,
                                    onClick = {
                                        navController.navigate(Screen.AddEditTask.route + "?taskId=${task.id}")
                                    },
                                    onDeleteClick = { showDeleteDialog = task },
                                    onStatusChange = { newStatus ->
                                        taskListViewModel.updateTaskStatus(task, newStatus)
                                    }
                                )
                            }
                        }
                    }
                }
                is TaskListUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Erro: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStatusChange: (ProgressState) -> Unit
) {
    val isDone = task.currentStatus == ProgressState.CONCLUDED
    val textStyle = if (isDone) {
        MaterialTheme.typography.titleLarge.copy(textDecoration = TextDecoration.LineThrough)
    } else {
        MaterialTheme.typography.titleLarge
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.title,
                    style = textStyle,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Deletar Tarefa",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if(isDone && task.initiatedAt != null && task.completedAt != 0L) {
                val duration = (task.completedAt - task.initiatedAt).milliseconds
                val formattedDuration = duration.toComponents { days, hours, minutes, _, _ ->
                    buildString {
                        if (days > 0) append("$days" + "d ")
                        if (hours > 0) append("$hours" + "h ")
                        if (minutes > 0) append("$minutes" + "m")
                        if (this.isEmpty()) append("0m")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tempo para conclusão: $formattedDuration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (task.currentStatus == ProgressState.PENDING) {
                    IconButton(onClick = { onStatusChange(ProgressState.UNDERWAY) }) {
                        Icon(Icons.Default.PlayArrow, "Iniciar Tarefa", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                if (task.currentStatus == ProgressState.UNDERWAY) {
                    IconButton(onClick = { onStatusChange(ProgressState.PENDING) }) {
                        Icon(Icons.Default.Pause, "Pausar Tarefa", tint = MaterialTheme.colorScheme.secondary)
                    }
                }
                if (task.currentStatus != ProgressState.CONCLUDED) {
                    IconButton(onClick = { onStatusChange(ProgressState.CONCLUDED) }) {
                        Icon(Icons.Default.Check, "Concluir Tarefa", tint = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentState: TaskListUiState,
    onDismiss: () -> Unit,
    viewModel: TaskListViewModel
) {
    val state = currentState as? TaskListUiState.Success ?: return
    val filters = state.filters

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtros e Ordenação") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Filtrar por Status", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = filters.statusFilter == null,
                        onClick = { viewModel.onStatusFilterChanged(null) },
                        shape = RoundedCornerShape(0.dp)
                    ) { Text("Todos") }

                    ProgressState.values().forEach {
                        SegmentedButton(
                            selected = filters.statusFilter == it,
                            onClick = { viewModel.onStatusFilterChanged(it) },
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Text(it.name.capitalize(Locale.getDefault()))
                        }
                    }
                }

                Text("Filtrar por Prioridade", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = filters.importanceFilter == null,
                        onClick = { viewModel.onImportanceFilterChanged(null) },
                        shape = RoundedCornerShape(0.dp)
                    ) { Text("Todas") }

                    ImportanceLevel.values().forEach {
                        SegmentedButton(
                            selected = filters.importanceFilter == it,
                            onClick = { viewModel.onImportanceFilterChanged(it) },
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Text(it.name.capitalize(Locale.getDefault()))
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ordenar por data de conclusão", modifier = Modifier.weight(1f))
                    Switch(
                        checked = filters.sortByCompletionDate,
                        onCheckedChange = { viewModel.onSortByCompletionDateChanged(it) }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

private fun String.capitalize(locale: Locale): String {
    return this.lowercase(locale).replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}