package com.ufpr.taskmanager.data.dataSource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.ufpr.taskmanager.data.model.Task
import kotlinx.coroutines.tasks.await

class FirestoreTaskService(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // Salva uma nova tarefa na coleção "tasks"
    suspend fun persistNewTask(task: Task) {
        currentUserId?.let { userId ->
            // Garante que a tarefa seja salva com o ID do usuário correto
            val taskWithOwner = task.copy(userId = userId)
            firestore.collection("tasks").add(taskWithOwner).await()
        }
    }

    // Busca todas as tarefas do usuário logado
    suspend fun retrieveUserTasks(): List<Task> {
        return currentUserId?.let { userId ->
            firestore.collection("tasks")
                .whereEqualTo("userId", userId) // Filtra as tarefas pelo ID do usuário
                .get()
                .await()
                .documents.mapNotNull { doc ->
                    doc.toObject<Task>()
                }
        } ?: emptyList()
    }

    // Atualiza uma tarefa existente
    suspend fun updateExistingTask(task: Task) {
        // Garante que o ID da tarefa não esteja vazio antes de tentar atualizar
        if (task.id.isNotBlank()) {
            firestore.collection("tasks").document(task.id).set(task).await()
        }
    }

    // Remove uma tarefa pelo seu ID
    suspend fun removeTaskById(taskId: String) {
        if (taskId.isNotBlank()) {
            firestore.collection("tasks").document(taskId).delete().await()
        }
    }
}