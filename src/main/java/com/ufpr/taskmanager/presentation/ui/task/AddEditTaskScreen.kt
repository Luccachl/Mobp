package com.ufpr.taskmanager.presentation.ui.task

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ufpr.taskmanager.data.model.ImportanceLevel
import com.ufpr.taskmanager.data.model.Task
import com.ufpr.taskmanager.presentation.viewmodel.TaskListViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    navController: NavController,
    viewModel: TaskListViewModel,
    taskId: String?
) {
    val isEditing = taskId != null
    val taskToEdit = if (isEditing) viewModel.getTaskById(taskId!!) else null

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var importance by remember { mutableStateOf(taskToEdit?.importance ?: ImportanceLevel.REGULAR) }
    var dueDate by remember { mutableStateOf(taskToEdit?.dueDate ?: System.currentTimeMillis()) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = dueDate

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            dueDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Tarefa" else "Nova Tarefa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(onClick = { datePickerDialog.show() }) {
                Text("Data Limite: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dueDate)}")
            }

            Text("Prioridade:")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ImportanceLevel.values().forEach { level ->
                    FilterChip(
                        selected = importance == level,
                        onClick = { importance = level },
                        label = { Text(level.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val task = (taskToEdit ?: Task()).copy(
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        importance = importance
                    )

                    if (isEditing) {
                        viewModel.updateExistingTask(task)
                    } else {
                        viewModel.submitNewTask(task)
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text(if (isEditing) "Salvar Alterações" else "Salvar Tarefa")
            }
        }
    }
}