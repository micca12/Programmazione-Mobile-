package com.micca.taskmanager.domain.models

/**
 * Un task dell'utente. Rispecchia la tabella `tasks` su Supabase
 * (stesso schema del progetto ISW).
 *
 * Nota: id è String perché su Supabase è un uuid.
 * Le date viaggiano come String ISO-8601 e vengono formattate solo nella UI.
 */
data class Task(
    val id: String,
    val userId: String,
    val title: String,
    val description: String?,
    val priorityId: Int,
    val statusId: Int,
    val dueDate: String?,
    val completedAt: String?,
    val sortOrder: Int?,
    val tagIds: List<Int>,
    val photoUrl: String?,
)
