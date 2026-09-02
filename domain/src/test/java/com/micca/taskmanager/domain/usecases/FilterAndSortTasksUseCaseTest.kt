package com.micca.taskmanager.domain.usecases

import com.micca.taskmanager.domain.models.SortBy
import com.micca.taskmanager.domain.models.Task
import com.micca.taskmanager.domain.models.TaskFilters
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterAndSortTasksUseCaseTest {

    private val useCase = FilterAndSortTasksUseCaseImpl()

    private fun task(
        id: String,
        title: String = "task $id",
        description: String? = null,
        priorityId: Int = 1,
        statusId: Int = 1,
        dueDate: String? = null,
        sortOrder: Int? = null,
        tagIds: List<Int> = emptyList(),
    ) = Task(
        id = id, userId = "u", title = title, description = description,
        priorityId = priorityId, statusId = statusId, dueDate = dueDate,
        completedAt = null, sortOrder = sortOrder, tagIds = tagIds, photoUrl = null,
    )

    @Test
    fun `filtri in AND fra categorie e OR interno`() {
        val tasks = listOf(
            task(id = "a", statusId = 1, priorityId = 1, tagIds = listOf(1)),
            task(id = "b", statusId = 2, priorityId = 1, tagIds = listOf(1)),
            task(id = "c", statusId = 1, priorityId = 2, tagIds = listOf(2)),
        )
        // stato IN {1,2} AND tag IN {1}
        val result = useCase.invoke(
            tasks = tasks,
            filters = TaskFilters(
                selectedStatusIds = setOf(1, 2),
                selectedTagIds = setOf(1),
            ),
        )
        assertEquals(listOf("a", "b"), result.map { it.id })
    }

    @Test
    fun `selezione vuota disattiva il filtro`() {
        val tasks = listOf(task(id = "a"), task(id = "b"))
        val result = useCase.invoke(tasks = tasks, filters = TaskFilters())
        assertEquals(2, result.size)
    }

    @Test
    fun `ricerca case-insensitive su titolo e descrizione`() {
        val tasks = listOf(
            task(id = "a", title = "Comprare LATTE"),
            task(id = "b", title = "altro", description = "il latte scade"),
            task(id = "c", title = "niente"),
        )
        val result = useCase.invoke(
            tasks = tasks,
            filters = TaskFilters(searchQuery = "latte"),
        )
        assertEquals(listOf("a", "b"), result.map { it.id })
    }

    @Test
    fun `ordinamento per scadenza mette i task senza data in fondo`() {
        // Nel web finivano in CIMA (null -> epoch 1970): qui correggiamo
        val tasks = listOf(
            task(id = "senza"),
            task(id = "tardi", dueDate = "2026-12-31T10:00:00+00:00"),
            task(id = "presto", dueDate = "2026-01-01T10:00:00+00:00"),
        )
        val result = useCase.invoke(
            tasks = tasks,
            filters = TaskFilters(sortBy = SortBy.DUE_DATE),
        )
        assertEquals(listOf("presto", "tardi", "senza"), result.map { it.id })
    }

    @Test
    fun `posizione con sort_order null in fondo e tiebreak su id`() {
        val tasks = listOf(
            task(id = "z", sortOrder = null),
            task(id = "b", sortOrder = 2),
            task(id = "a", sortOrder = 2),
            task(id = "c", sortOrder = 1),
        )
        val result = useCase.invoke(
            tasks = tasks,
            filters = TaskFilters(sortBy = SortBy.POSITION),
        )
        assertEquals(listOf("c", "a", "b", "z"), result.map { it.id })
    }

    @Test
    fun `data non parsabile trattata come assente`() {
        val tasks = listOf(
            task(id = "rotta", dueDate = "non-una-data"),
            task(id = "ok", dueDate = "2026-06-01T08:00:00+00:00"),
        )
        val result = useCase.invoke(
            tasks = tasks,
            filters = TaskFilters(sortBy = SortBy.DUE_DATE),
        )
        assertEquals(listOf("ok", "rotta"), result.map { it.id })
    }
}
