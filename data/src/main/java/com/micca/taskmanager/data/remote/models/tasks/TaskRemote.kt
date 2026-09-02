package com.micca.taskmanager.data.remote.models.tasks

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Riga della tabella `tasks` come la restituisce PostgREST.
 * Tutto nullable con default: le API possono omettere campi, e la colonna
 * photo_url arriverà solo quando ci saranno le foto (finché non esiste,
 * il campo resta null in lettura).
 */
@JsonClass(generateAdapter = true)
data class TaskRemote(
    @Json(name = "id") var id: String? = null,
    @Json(name = "user_id") var userId: String? = null,
    @Json(name = "title") var title: String? = null,
    @Json(name = "description") var description: String? = null,
    @Json(name = "priority_id") var priorityId: Int? = null,
    @Json(name = "status_id") var statusId: Int? = null,
    @Json(name = "due_date") var dueDate: String? = null,
    @Json(name = "completed_at") var completedAt: String? = null,
    @Json(name = "sort_order") var sortOrder: Int? = null,
    @Json(name = "tag_ids") var tagIds: List<Int>? = null,
    @Json(name = "photo_url") var photoUrl: String? = null,
)
