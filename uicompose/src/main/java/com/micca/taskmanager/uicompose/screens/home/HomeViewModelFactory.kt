package com.micca.taskmanager.uicompose.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.micca.taskmanager.domain.repositories.AuthRepository
import com.micca.taskmanager.domain.repositories.TaskRepository
import com.micca.taskmanager.domain.usecases.FilterAndSortTasksUseCase

class HomeViewModelFactory(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val filterAndSortTasks: FilterAndSortTasksUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                taskRepository = taskRepository,
                authRepository = authRepository,
                filterAndSortTasks = filterAndSortTasks,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
