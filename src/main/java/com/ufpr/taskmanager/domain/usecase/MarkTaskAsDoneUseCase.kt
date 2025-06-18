package com.ufpr.taskmanager.domain.usecase

import com.ufpr.taskmanager.data.model.Task
import com.ufpr.taskmanager.data.model.ProgressState
import com.ufpr.taskmanager.data.repository.UserTaskRepository

class MarkTaskAsDoneUseCase(private val repository: UserTaskRepository) {
    suspend fun execute(task: Task) {
        val finalizedTask = task.copy(
            currentStatus = ProgressState.CONCLUDED,
            completedAt = System.currentTimeMillis(),
            initiatedAt = task.initiatedAt ?: System.currentTimeMillis()
        )
        repository.modifyTask(finalizedTask)
    }
}