package com.ufpr.taskmanager.data.repository

import com.ufpr.taskmanager.data.model.Task

interface UserTaskRepository {
    suspend fun storeTask(task: Task)
    suspend fun fetchAllUserTasks(): List<Task>
    suspend fun modifyTask(task: Task)
    suspend fun eraseTask(taskId: String)
}