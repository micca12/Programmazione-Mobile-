package com.micca.taskmanager.domain.di

import com.micca.taskmanager.domain.repositories.AuthRepository
import com.micca.taskmanager.domain.repositories.TaskRepository
import com.micca.taskmanager.domain.usecases.FilterAndSortTasksUseCase
import com.micca.taskmanager.domain.usecases.FilterAndSortTasksUseCaseImpl

/**
 * Singleton che espone alla UI le dipendenze di dominio.
 * setup() va chiamato una volta sola, nella CustomApplication.
 *
 * I ViewModel vedono solo le *interfacce* dichiarate qui in :domain: le
 * implementazioni vivono in :data, che non e' nel classpath di :uicompose.
 */
object AppContainer {

    lateinit var taskRepository: TaskRepository
        private set

    lateinit var authRepository: AuthRepository
        private set

    // Funzione pura, nessuna dipendenza: istanziabile subito
    val filterAndSortTasks: FilterAndSortTasksUseCase = FilterAndSortTasksUseCaseImpl()

    fun setup(repositoryProvider: RepositoryProvider) {
        taskRepository = repositoryProvider.taskRepository
        authRepository = repositoryProvider.authRepository
    }
}
