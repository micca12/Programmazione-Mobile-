package com.micca.taskmanager.data.remote.models.tasks

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Parsing e mapping delle righe di PostgREST: i punti fragili sono
 * tag_ids null, campi omessi, e le righe malformate.
 */
class TaskMappingTest {

    private val moshi = Moshi.Builder().build()
    private val listAdapter = moshi.adapter<List<TaskRemote>>(
        Types.newParameterizedType(List::class.java, TaskRemote::class.java)
    )

    @Test
    fun `riga completa con data ISO e microsecondi diventa Task`() {
        val json = """
            [{
              "id": "b7f9a3c0-0000-0000-0000-000000000001",
              "user_id": "u-1",
              "title": "Studiare per LPSM",
              "description": "ripassare coroutine",
              "priority_id": 3,
              "status_id": 1,
              "due_date": "2026-09-01T14:30:00.123456+00:00",
              "completed_at": null,
              "sort_order": 2,
              "tag_ids": [1, 4]
            }]
        """.trimIndent()

        val task = listAdapter.fromJson(json)!!.first().toDomain()

        assertNotNull(task)
        assertEquals("Studiare per LPSM", task!!.title)
        assertEquals(listOf(1, 4), task.tagIds)
        assertEquals("2026-09-01T14:30:00.123456+00:00", task.dueDate)
        assertEquals(2, task.sortOrder)
    }

    @Test
    fun `tag_ids null diventa lista vuota, sort_order assente resta null`() {
        val json = """
            [{ "id": "t2", "user_id": "u-1", "title": "Senza tag", "tag_ids": null }]
        """.trimIndent()

        val task = listAdapter.fromJson(json)!!.first().toDomain()

        assertNotNull(task)
        assertEquals(emptyList<Int>(), task!!.tagIds)
        assertNull(task.sortOrder)
        assertNull(task.dueDate)
    }

    @Test
    fun `riga senza id viene scartata dal mapping`() {
        val json = """
            [{ "title": "Riga malformata", "user_id": "u-1" }]
        """.trimIndent()

        val task = listAdapter.fromJson(json)!!.first().toDomain()

        assertNull(task)
    }

    @Test
    fun `il body di insert contiene user_id e non contiene id`() {
        val task = com.micca.taskmanager.domain.models.Task(
            id = "ignorato",
            userId = "vecchio",
            title = "Nuovo task",
            description = null,
            priorityId = 1,
            statusId = 1,
            dueDate = null,
            completedAt = null,
            sortOrder = 5,
            tagIds = listOf(2),
            photoUrl = null,
        )

        val body = task.toBody(userId = "u-sessione")
        val json = moshi.adapter(TaskBody::class.java).serializeNulls().toJson(body)

        assertEquals("u-sessione", body.userId)
        assert(!json.contains("\"id\""))
        assert(json.contains("\"due_date\":null")) // null-serialization: il campo viaggia
    }
}
