package com.micca.taskmanager.domain.models

/**
 * Tabelle di lookup di Supabase: `priorities`, `statuses`, `tags`.
 *
 * Nota su Tag.id: nel frontend web ISW era tipizzato come string, ma la colonna
 * è numerica (tag_ids è un array di interi) — il codice web era pieno di
 * Number(tag.id)/toString() per compensare. Qui il tipo è quello vero.
 */
data class Priority(
    val id: Int,
    val name: String,
    val color: String,
)

data class Status(
    val id: Int,
    val name: String,
    val color: String,
)

data class Tag(
    val id: Int,
    val name: String,
    val color: String?,
)
