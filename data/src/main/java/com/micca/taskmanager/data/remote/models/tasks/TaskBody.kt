package com.micca.taskmanager.data.remote.models.tasks

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Body per INSERT (POST) e UPDATE (PATCH) sulla tabella `tasks`.
 *
 * È separato da TaskRemote per due ragioni:
 *  - id lo genera il database (non va mai inviato);
 *  - user_id è OBBLIGATORIO negli insert per le policy RLS.
 *
 * Con la null-serialization attiva sul converter, i campi null vengono
 * inviati esplicitamente: così una PATCH può azzerare due_date.
 *
 * NB: photo_url NON è ancora qui — la colonna sul DB ancora non esiste,
 * e inviarla farebbe fallire la richiesta.
 */
@JsonClass(generateAdapter = true)
data class TaskBody(
    @Json(name = "user_id") val userId: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String?,
    @Json(name = "priority_id") val priorityId: Int,
    @Json(name = "status_id") val statusId: Int,
    @Json(name = "due_date") val dueDate: String?,
    @Json(name = "completed_at") val completedAt: String?,
    @Json(name = "sort_order") val sortOrder: Int?,
    @Json(name = "tag_ids") val tagIds: List<Int>,
)
