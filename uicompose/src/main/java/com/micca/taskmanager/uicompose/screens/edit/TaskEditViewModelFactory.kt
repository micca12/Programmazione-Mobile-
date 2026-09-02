package com.micca.taskmanager.uicompose.screens.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.micca.taskmanager.domain.repositories.TaskRepository

class TaskEditViewModelFactory(
    private val taskRepository: TaskRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskEditViewModel::class.java)) {
            return TaskEditViewModel(taskRepository = taskRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
