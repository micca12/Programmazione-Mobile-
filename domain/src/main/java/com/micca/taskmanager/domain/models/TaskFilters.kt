package com.micca.taskmanager.domain.models

// stato dei filtri: multi-selezione tag/stato/priorita', ricerca, ordinamento
data class TaskFilters(
    val selectedTagIds: Set<Int> = emptySet(),
    val selectedStatusIds: Set<Int> = emptySet(),
    val selectedPriorityIds: Set<Int> = emptySet(),
    val searchQuery: String = "",
    val sortBy: SortBy = SortBy.POSITION,
)

/** i 4 ordinamenti possibili, tutti crescenti */
enum class SortBy {
    POSITION,
    PRIORITY,
    STATUS,
    DUE_DATE,
}
