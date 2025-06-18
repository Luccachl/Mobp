package com.ufpr.taskmanager.data.repository

import com.ufpr.taskmanager.data.dataSource.FirestoreTaskService
import com.ufpr.taskmanager.data.model.Task


class UserTaskRepositoryImpl(
    private val taskService: FirestoreTaskService
) : UserTaskRepository {

    override suspend fun storeTask(task: Task) = taskService.persistNewTask(task)

    override suspend fun fetchAllUserTasks(): List<Task> = taskService.retrieveUserTasks()

    override suspend fun modifyTask(task: Task) = taskService.updateExistingTask(task)

    override suspend fun eraseTask(taskId: String) = taskService.removeTaskById(taskId)
}