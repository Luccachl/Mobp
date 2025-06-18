package com.ufpr.taskmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
// A importação incorreta foi removida daqui.
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ufpr.taskmanager.data.dataSource.FirestoreTaskService
import com.ufpr.taskmanager.data.repository.UserTaskRepositoryImpl
import com.ufpr.taskmanager.domain.usecase.AddNewTaskUseCase
import com.ufpr.taskmanager.domain.usecase.MarkTaskAsDoneUseCase
import com.ufpr.taskmanager.presentation.TaskManagerApp
import com.ufpr.taskmanager.presentation.ui.theme.TaskManagerTheme
import com.ufpr.taskmanager.presentation.viewmodel.AuthenticationViewModel
import com.ufpr.taskmanager.presentation.viewmodel.TaskListViewModel

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthenticationViewModel
    private lateinit var taskListViewModel: TaskListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialização dos ViewModels
        val firebaseAuth = FirebaseAuth.getInstance()
        authViewModel = AuthenticationViewModel(firebaseAuth)

        val firestore = FirebaseFirestore.getInstance()
        val firestoreTaskService = FirestoreTaskService(firestore, firebaseAuth)
        val userTaskRepository = UserTaskRepositoryImpl(firestoreTaskService)
        val addNewTaskUseCase = AddNewTaskUseCase(userTaskRepository)
        val markTaskAsDoneUseCase = MarkTaskAsDoneUseCase(userTaskRepository)

        taskListViewModel = TaskListViewModel(
            taskRepository = userTaskRepository,
            addNewTask = addNewTaskUseCase,
            markAsDone = markTaskAsDoneUseCase
        )

        // Chamada da função de criação do canal de notificação
        createNotificationChannel()

        enableEdgeToEdge()
        setContent {
            TaskManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskManagerApp(
                        authViewModel = authViewModel,
                        taskListViewModel = taskListViewModel
                    )
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Lembretes de Tarefas"
            val descriptionText = "Canal para notificações de tarefas próximas do prazo."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("TASK_REMINDERS", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}