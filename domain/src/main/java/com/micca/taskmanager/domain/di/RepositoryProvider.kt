package com.micca.taskmanager.domain.di

import com.micca.taskmanager.domain.repositories.AuthRepository
import com.micca.taskmanager.domain.repositories.TaskRepository

/**
 * Contratto della DI manuale (convenzione del corso, niente Hilt):
 * domain dichiara cosa serve, :data lo implementa (RepositoryProviderImpl),
 * :app li collega nella CustomApplication.
 */
interface RepositoryProvider {
    val taskRepository: TaskRepository
    val authRepository: AuthRepository
}
