package com.ufpr.taskmanager.presentation.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ufpr.taskmanager.presentation.navigation.Screen
import com.ufpr.taskmanager.presentation.viewmodel.TaskListViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    navController: NavController,
    viewModel: TaskListViewModel,
    taskId: String?
) {
    val task = taskId?.let { viewModel.getTaskById(it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Tarefa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (task != null) {
                        IconButton(onClick = {
                            navController.navigate(Screen.AddEditTask.route + "?taskId=${task.id}")
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Tarefa")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (task == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tarefa não encontrada.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(task.title, style = MaterialTheme.typography.headlineMedium)
                }
                item {
                    Text(task.description, style = MaterialTheme.typography.bodyLarge)
                }
                item {
                    Divider()
                }
                item {
                    DetailItem("Status:", task.currentStatus.name)
                    DetailItem("Prioridade:", task.importance.name)
                    DetailItem("Data Limite:", SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(task.dueDate))
                }

                if (task.subTasks.isNotEmpty()) {
                    item { Divider() }
                    item { Text("Subtarefas", style = MaterialTheme.typography.titleMedium) }
                    items(task.subTasks.size) { index ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = task.subTasks[index].isDone, onCheckedChange = null)
                            Text(task.subTasks[index].title)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.width(120.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}