package com.micca.taskmanager.data.remote.models.tasks

import com.micca.taskmanager.domain.models.Priority
import com.micca.taskmanager.domain.models.Status
import com.micca.taskmanager.domain.models.Tag
import com.micca.taskmanager.domain.models.Task

/**
 * Conversioni fra i modelli remoti (PostgREST) e i modelli di dominio.
 *
 * `internal` invece di private-nel-repository (com'è nel progetto del corso)
 * per poterle coprire con unit test: sono il punto più fragile del parsing.
 *
 * Una riga senza id o senza user_id è inutilizzabile: toDomain() restituisce
 * null e il chiamante la scarta con mapNotNull (meglio ignorare una riga
 * malformata che crashare). Il titolo mancante invece diventa "" e
 * priority/status mancanti diventano 1: stessi default della web app,
 * che ammette task senza titolo.
 */

internal fun TaskRemote.toDomain(): Task? {
    val id = id ?: return null
    val userId = userId ?: return null
    return Task(
        id = id,
        userId = userId,
        title = title ?: "",
        description = description,
        priorityId = priorityId ?: 1,
        statusId = statusId ?: 1,
        dueDate = dueDate,
        completedAt = completedAt,
        sortOrder = sortOrder,
        tagIds = tagIds ?: emptyList(),
        photoUrl = photoUrl,
    )
}

internal fun Task.toBody(userId: String): TaskBody = TaskBody(
    userId = userId,
    title = title,
    description = description,
    priorityId = priorityId,
    statusId = statusId,
    dueDate = dueDate,
    completedAt = completedAt,
    sortOrder = sortOrder,
    tagIds = tagIds,
)

internal fun TagRemote.toDomain(): Tag? {
    val id = id ?: return null
    return Tag(id = id, name = name ?: "", color = color)
}

internal fun PriorityRemote.toDomain(): Priority? {
    val id = id ?: return null
    return Priority(id = id, name = name ?: "", color = color ?: "slate")
}

internal fun StatusRemote.toDomain(): Status? {
    val id = id ?: return null
    return Status(id = id, name = name ?: "", color = color ?: "slate")
}
