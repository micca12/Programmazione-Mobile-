package com.micca.taskmanager.uicompose.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micca.taskmanager.domain.models.ErrorState
import com.micca.taskmanager.domain.models.Priority
import com.micca.taskmanager.domain.models.SortBy
import com.micca.taskmanager.domain.models.Status
import com.micca.taskmanager.domain.models.Tag
import com.micca.taskmanager.domain.models.Task
import com.micca.taskmanager.domain.models.TaskFilters
import com.micca.taskmanager.domain.repositories.AuthRepository
import com.micca.taskmanager.domain.repositories.TaskRepository
import com.micca.taskmanager.domain.usecases.FilterAndSortTasksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/**
 * Stato della dashboard. Vede solo le interfacce dei repository dichiarate
 * in :domain, mai le implementazioni di :data.
 */
class HomeViewModel(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val filterAndSortTasks: FilterAndSortTasksUseCase,
) : ViewModel() {

    // Il repository espone gia' StateFlow: ri-esporli direttamente evita una
    // copia inutile; la coppia _x/x resta per lo stato posseduto dal VM
    val tasks: StateFlow<List<Task>> = taskRepository.tasks
    val tags: StateFlow<List<Tag>> = taskRepository.tags
    val priorities: StateFlow<List<Priority>> = taskRepository.priorities
    val statuses: StateFlow<List<Status>> = taskRepository.statuses
    val errorState: StateFlow<ErrorState?> = taskRepository.errorState

    private val _filters = MutableStateFlow(TaskFilters())
    val filters: StateFlow<TaskFilters> = _filters

    /** La lista che la UI mostra: già filtrata, cercata e ordinata. */
    private val _visibleTasks = MutableStateFlow<List<Task>>(emptyList())
    val visibleTasks: StateFlow<List<Task>> = _visibleTasks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Ricalcola la lista visibile a ogni cambio di dati o filtri.
        // Tutto in memoria: i filtri non toccano mai la rete.
        viewModelScope.launch {
            combine(tasks, _filters) { taskList, currentFilters ->
                filterAndSortTasks.invoke(tasks = taskList, filters = currentFilters)
            }.collect { _visibleTasks.emit(it) }
        }

        // Il loader si spegne alla prima emissione reale: drop(1) salta il
        // valore iniziale che uno StateFlow ripete a ogni nuovo collector
        viewModelScope.launch {
            tasks.drop(1).collect { _isLoading.value = false }
        }
        viewModelScope.launch {
            errorState.drop(1).collect { if (it != null) _isLoading.value = false }
        }

        startFetch()
    }

    fun startFetch() {
        _isLoading.value = true
        taskRepository.startFetchTasks()
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    // --- filtri ---

    fun toggleTagFilter(id: Int) {
        _filters.value = _filters.value.copy(
            selectedTagIds = _filters.value.selectedTagIds.toggle(id)
        )
    }

    fun toggleStatusFilter(id: Int) {
        _filters.value = _filters.value.copy(
            selectedStatusIds = _filters.value.selectedStatusIds.toggle(id)
        )
    }

    fun togglePriorityFilter(id: Int) {
        _filters.value = _filters.value.copy(
            selectedPriorityIds = _filters.value.selectedPriorityIds.toggle(id)
        )
    }

    fun setSearchQuery(query: String) {
        _filters.value = _filters.value.copy(searchQuery = query)
    }

    fun setSortBy(sortBy: SortBy) {
        _filters.value = _filters.value.copy(sortBy = sortBy)
    }

    private fun Set<Int>.toggle(id: Int): Set<Int> =
        if (contains(id)) this - id else this + id

    // --- azioni rapide sulla card ---

    /** Avanza il task allo stato successivo (per id, ciclico) senza aprire il form. */
    fun cycleStatus(task: Task) {
        val ordered = statuses.value.sortedBy { it.id }
        if (ordered.isEmpty()) return
        val currentIndex = ordered.indexOfFirst { it.id == task.statusId }
        val next = ordered[(currentIndex + 1) % ordered.size]
        viewModelScope.launch {
            taskRepository.updateTask(task = task.copy(statusId = next.id))
        }
    }

    /** Lookup per i badge delle card. */
    fun priorityFor(id: Int): Priority? = priorities.value.find { it.id == id }
    fun statusFor(id: Int): Status? = statuses.value.find { it.id == id }
}
