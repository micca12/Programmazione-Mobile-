package com.micca.taskmanager.uicompose.screens.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micca.taskmanager.domain.models.Task
import com.micca.taskmanager.domain.repositories.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel del form: stesso form per creare e modificare, distinguo con
 * task.id vuoto. Lo stato del form sta qui nel ViewModel cosi' sopravvive
 * alla rotazione.
 */
class TaskEditViewModel(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    /** copia locale del task in modifica */
    private val _draft = MutableStateFlow(emptyDraft())
    val draft: StateFlow<Task> = _draft

    val isEditMode: Boolean
        get() = _draft.value.id.isNotEmpty()

    private val _titleError = MutableStateFlow(false)
    val titleError: StateFlow<Boolean> = _titleError

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    /** true quando l'operazione è conclusa e la schermata può chiudersi. */
    private val _done = MutableStateFlow(false)
    val done: StateFlow<Boolean> = _done

    companion object {
        /** valori di default per un task nuovo */
        fun emptyDraft() = Task(
            id = "",
            userId = "",
            title = "",
            description = null,
            priorityId = 1,
            statusId = 1,
            dueDate = null,
            completedAt = null,
            sortOrder = null,
            tagIds = emptyList(),
            photoUrl = null,
        )
    }

    /** Inizializza il form: task esistente (modifica) o default (creazione). */
    fun load(task: Task?) {
        _draft.value = task ?: emptyDraft()
        _titleError.value = false
        _done.value = false
    }

    fun setTitle(value: String) {
        _draft.value = _draft.value.copy(title = value)
        if (value.isNotBlank()) _titleError.value = false
    }

    fun setDescription(value: String) {
        _draft.value = _draft.value.copy(description = value.ifBlank { null })
    }

    fun setPriority(id: Int) {
        _draft.value = _draft.value.copy(priorityId = id)
    }

    fun setStatus(id: Int) {
        _draft.value = _draft.value.copy(statusId = id)
    }

    fun toggleTag(id: Int) {
        val current = _draft.value.tagIds
        _draft.value = _draft.value.copy(
            tagIds = if (current.contains(id)) current - id else current + id
        )
    }

    fun setDueDate(isoDate: String?) {
        _draft.value = _draft.value.copy(dueDate = isoDate)
    }

    /**
     * Salva: create se id vuoto, altrimenti update.
     * A differenza del web, il titolo vuoto non lo faccio passare.
     */
    fun save() {
        val task = _draft.value
        if (task.title.isBlank()) {
            _titleError.value = true
            return
        }
        viewModelScope.launch {
            _isSaving.emit(true)
            if (isEditMode) {
                taskRepository.updateTask(task = task)
            } else {
                taskRepository.createTask(task = task)
            }
            _isSaving.emit(false)
            _done.emit(true)
        }
    }

    /** Eliminazione: solo in modifica, e la conferma la chiede la UI. */
    fun delete() {
        val task = _draft.value
        if (!isEditMode) return
        viewModelScope.launch {
            _isSaving.emit(true)
            taskRepository.deleteTask(taskId = task.id)
            _isSaving.emit(false)
            _done.emit(true)
        }
    }
}
