package com.micca.taskmanager.domain.usecases

import com.micca.taskmanager.domain.models.SortBy
import com.micca.taskmanager.domain.models.Task
import com.micca.taskmanager.domain.models.TaskFilters
import java.time.OffsetDateTime

/**
 * Filtra + cerca + ordina la lista. Filtri in AND fra categorie e OR dentro
 * (vuoto = disattivo), ricerca su titolo e descrizione.
 * Rispetto al web: i task senza scadenza li metto in fondo (nel web finivano
 * in cima perche' null diventava il 1970) e ordino anche per id per non ballare.
 * Funzione pura -> testabile senza Android.
 */
interface FilterAndSortTasksUseCase {
    fun invoke(tasks: List<Task>, filters: TaskFilters): List<Task>
}

class FilterAndSortTasksUseCaseImpl : FilterAndSortTasksUseCase {

    override fun invoke(tasks: List<Task>, filters: TaskFilters): List<Task> {
        val query = filters.searchQuery.trim().lowercase()

        val filtered = tasks.filter { task ->
            val tagOk = filters.selectedTagIds.isEmpty() ||
                task.tagIds.any { it in filters.selectedTagIds }

            val statusOk = filters.selectedStatusIds.isEmpty() ||
                task.statusId in filters.selectedStatusIds

            val priorityOk = filters.selectedPriorityIds.isEmpty() ||
                task.priorityId in filters.selectedPriorityIds

            val searchOk = query.isEmpty() ||
                task.title.lowercase().contains(query) ||
                (task.description ?: "").lowercase().contains(query)

            tagOk && statusOk && priorityOk && searchOk
        }

        val comparator: Comparator<Task> = when (filters.sortBy) {
            SortBy.POSITION -> compareBy<Task, Int?>(nullsLast()) { it.sortOrder }
            SortBy.PRIORITY -> compareBy { it.priorityId }
            SortBy.STATUS -> compareBy { it.statusId }
            SortBy.DUE_DATE -> compareBy<Task, OffsetDateTime?>(nullsLast()) { it.parsedDueDate() }
        }

        // .thenBy { id }: a parita' di chiave ordino per id, cosi' e' stabile
        return filtered.sortedWith(comparator.thenBy { it.id })
    }

    private fun Task.parsedDueDate(): OffsetDateTime? = try {
        dueDate?.let { OffsetDateTime.parse(it) }
    } catch (e: Exception) {
        null
    }
}
