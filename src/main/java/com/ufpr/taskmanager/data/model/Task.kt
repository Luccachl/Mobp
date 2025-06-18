package com.ufpr.taskmanager.data.model

import com.google.firebase.firestore.DocumentId

enum class ImportanceLevel {
    LOW_PRIORITY,
    REGULAR,
    CRITICAL
}

enum class ProgressState {
    PENDING,
    UNDERWAY,
    CONCLUDED
}

data class Task(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: Long = 0L,
    val importance: ImportanceLevel = ImportanceLevel.REGULAR,
    val subTasks: List<SubTask> = emptyList(),
    val userId: String = "",
    val currentStatus: ProgressState = ProgressState.PENDING,
    val completedAt: Long = 0L,
    val initiatedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)