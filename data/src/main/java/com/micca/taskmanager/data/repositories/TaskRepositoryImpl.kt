package com.micca.taskmanager.data.repositories

import android.content.Context
import android.util.Log
import com.micca.taskmanager.data.local.SessionManager
import com.micca.taskmanager.data.remote.api.RetrofitClient
import com.micca.taskmanager.data.remote.api.eqId
import com.micca.taskmanager.data.remote.models.tasks.toBody
import com.micca.taskmanager.data.remote.models.tasks.toDomain
import com.micca.taskmanager.domain.models.ErrorState
import com.micca.taskmanager.domain.models.Priority
import com.micca.taskmanager.domain.models.Status
import com.micca.taskmanager.domain.models.Tag
import com.micca.taskmanager.domain.models.Task
import com.micca.taskmanager.domain.repositories.TaskRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository dei task via Retrofit. Lavora su Dispatchers.IO (la UI resta
 * sul main). awaitRestore() prima di ogni chiamata: se no al primo avvio
 * parte senza token e Supabase mi torna lista vuota.
 * TODO: aggiungere la cache Room, per ora solo rete.
 */
class TaskRepositoryImpl(
    private val context: Context, // servirà per Room: AppDatabase.getInstance(context)
    private val sessionManager: SessionManager,
    private val retrofitClient: RetrofitClient,
) : TaskRepository {

    companion object {
        private const val TAG = "TaskRepository"
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    override val tasks: StateFlow<List<Task>> = _tasks

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    override val tags: StateFlow<List<Tag>> = _tags

    private val _priorities = MutableStateFlow<List<Priority>>(emptyList())
    override val priorities: StateFlow<List<Priority>> = _priorities

    private val _statuses = MutableStateFlow<List<Status>>(emptyList())
    override val statuses: StateFlow<List<Status>> = _statuses

    private val _errorState = MutableStateFlow<ErrorState?>(null)
    override val errorState: StateFlow<ErrorState?> = _errorState

    override fun startFetchTasks() {
        // TODO: prima leggere dalla cache Room, poi refresh dalla rete
        refreshTasks()
    }

    override fun refreshTasks() {
        scope.launch {
            try {
                sessionManager.awaitRestore()

                // le 4 GET sono indipendenti -> in parallelo con async.
                // aspetto tutte prima di emettere, cosi' se una fallisce
                // non mostro dati a meta'.
                coroutineScope {
                    val tasksDeferred = async { retrofitClient.restApi.getTasks() }
                    val tagsDeferred = async { retrofitClient.restApi.getTags() }
                    val prioritiesDeferred = async { retrofitClient.restApi.getPriorities() }
                    val statusesDeferred = async { retrofitClient.restApi.getStatuses() }

                    val taskList = tasksDeferred.await().mapNotNull { it.toDomain() }
                    val tagList = tagsDeferred.await().mapNotNull { it.toDomain() }
                    val priorityList = prioritiesDeferred.await().mapNotNull { it.toDomain() }
                    val statusList = statusesDeferred.await().mapNotNull { it.toDomain() }

                    _tags.emit(tagList)
                    _priorities.emit(priorityList)
                    _statuses.emit(statusList)
                    _tasks.emit(taskList)
                }
                _errorState.emit(null)
                Log.d(TAG, "Refresh completato: ${_tasks.value.size} task")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Refresh fallito", e)
                _errorState.emit(e.toErrorState())
            }
        }
    }

    override suspend fun createTask(task: Task) = withContext(Dispatchers.IO) {
        try {
            sessionManager.awaitRestore()

            // user_id dalla sessione, serve per la RLS.
            // sort_order = max+1 e non size+1 (con size dopo una delete si ripete)
            val userId = sessionManager.userId
                ?: throw IllegalStateException("createTask senza sessione")
            val nextSortOrder = (_tasks.value.mapNotNull { it.sortOrder }.maxOrNull() ?: 0) + 1
            val body = task.copy(sortOrder = nextSortOrder).toBody(userId = userId)

            val created = retrofitClient.restApi.createTask(body = body)
                .firstOrNull()?.toDomain()
                ?: throw IllegalStateException("insert senza representation")

            // update{} invece di .value cosi' se arriva un refresh nel mentre
            // non mi sovrascrive il task appena aggiunto
            _tasks.update { current -> current + created }
            _errorState.emit(null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "createTask fallita", e)
            _errorState.emit(e.toErrorState())
        }
    }

    override suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        try {
            sessionManager.awaitRestore()
            val userId = sessionManager.userId
                ?: throw IllegalStateException("updateTask senza sessione")

            val updated = retrofitClient.restApi.updateTask(
                idFilter = eqId(task.id),
                body = task.toBody(userId = userId),
            ).firstOrNull()?.toDomain()
                ?: throw IllegalStateException("update senza representation")

            _tasks.update { current -> current.map { if (it.id == updated.id) updated else it } }
            _errorState.emit(null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "updateTask fallita", e)
            _errorState.emit(e.toErrorState())
        }
    }

    override suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        try {
            sessionManager.awaitRestore()
            retrofitClient.restApi.deleteTask(idFilter = eqId(taskId))
            _tasks.update { current -> current.filter { it.id != taskId } }
            _errorState.emit(null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "deleteTask fallita", e)
            _errorState.emit(e.toErrorState())
        }
    }
}
