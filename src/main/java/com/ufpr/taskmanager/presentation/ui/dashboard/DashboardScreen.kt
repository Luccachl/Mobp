package com.ufpr.taskmanager.presentation.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ufpr.taskmanager.data.model.ProgressState
import com.ufpr.taskmanager.presentation.navigation.Screen
import com.ufpr.taskmanager.presentation.viewmodel.AuthenticationViewModel
import com.ufpr.taskmanager.presentation.viewmodel.TaskListUiState
import com.ufpr.taskmanager.presentation.viewmodel.TaskListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    taskListViewModel: TaskListViewModel,
    authViewModel: AuthenticationViewModel
) {
    val tasksUiState by taskListViewModel.taskListUiState.collectAsState()
    val userName = authViewModel.currentUserName ?: "Usuário"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
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
            Text(
                text = "Bem-vindo, $userName!",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = tasksUiState) {
                is TaskListUiState.Success -> {
                    val tasks = state.tasks
                    val pendingCount = tasks.count { it.currentStatus == ProgressState.PENDING }
                    val underwayCount = tasks.count { it.currentStatus == ProgressState.UNDERWAY }
                    val concludedCount = tasks.count { it.currentStatus == ProgressState.CONCLUDED }

                    SummaryCard(
                        title = "Resumo das Tarefas",
                        pending = pendingCount,
                        underway = underwayCount,
                        concluded = concludedCount
                    )
                }
                is TaskListUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is TaskListUiState.Error -> {
                    Text("Não foi possível carregar o resumo das tarefas.", color = MaterialTheme.colorScheme.error)
                }
            }


            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate(Screen.TaskList.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Ver todas as tarefas")
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, pending: Int, underway: Int, concluded: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            SummaryItem(label = "Pendentes", count = pending)
            SummaryItem(label = "Em Andamento", count = underway)
            SummaryItem(label = "Concluídas", count = concluded)
        }
    }
}

@Composable
fun SummaryItem(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = count.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}