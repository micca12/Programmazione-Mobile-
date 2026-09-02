package com.micca.taskmanager.domain.repositories

import com.micca.taskmanager.domain.models.ErrorState
import com.micca.taskmanager.domain.models.Priority
import com.micca.taskmanager.domain.models.Status
import com.micca.taskmanager.domain.models.Tag
import com.micca.taskmanager.domain.models.Task
import kotlinx.coroutines.flow.StateFlow

/**
 * Contratto del repository dei task. L'implementazione (in :data) parla
 * con Supabase via Retrofit; la UI lo vede solo attraverso gli use case.
 */
interface TaskRepository {

    val tasks: StateFlow<List<Task>>
    val tags: StateFlow<List<Tag>>
    val priorities: StateFlow<List<Priority>>
    val statuses: StateFlow<List<Status>>
    val errorState: StateFlow<ErrorState?>

    /** Primo caricamento. Oggi delega a refreshTasks(); con la cache Room
     *  leggerà prima dal database locale. */
    fun startFetchTasks()

    /** Riscarica tutto dalla rete. */
    fun refreshTasks()

    suspend fun createTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: String)
}
