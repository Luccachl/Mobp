package com.ufpr.taskmanager.domain.usecase

import com.ufpr.taskmanager.data.model.ImportanceLevel
import com.ufpr.taskmanager.data.model.ProgressState
import com.ufpr.taskmanager.data.model.Task

class OrganizeAndFilterTasksUseCase {
    operator fun invoke(
        tasks: List<Task>,
        searchQuery: String,
        filterByImportance: ImportanceLevel? = null,
        filterByStatus: ProgressState? = null,
        sortByCompletionDate: Boolean = false
    ): List<Task> {

        val searchedList = if (searchQuery.isBlank()) {
            tasks
        } else {
            tasks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }

        val filteredList = searchedList.filter { task ->
            val importanceMatches = filterByImportance == null || task.importance == filterByImportance
            val statusMatches = filterByStatus == null || task.currentStatus == filterByStatus
            importanceMatches && statusMatches
        }

        return if (sortByCompletionDate) {
            filteredList.sortedWith(compareBy(nullsLast()) { if (it.completedAt == 0L) null else it.completedAt }).reversed()
        } else {
            filteredList.sortedBy { it.createdAt }
        }
    }
}