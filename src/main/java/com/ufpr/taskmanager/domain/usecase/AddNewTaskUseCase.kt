package com.ufpr.taskmanager.domain.usecase

import com.ufpr.taskmanager.data.model.Task
import com.ufpr.taskmanager.data.repository.UserTaskRepository

class AddNewTaskUseCase(private val repository: UserTaskRepository) {
    suspend fun execute(task: Task) {
        repository.storeTask(task)
    }
}