package com.micca.taskmanager.data.di

import android.content.Context
import com.micca.taskmanager.data.local.SessionManager
import com.micca.taskmanager.data.remote.api.RetrofitClient
import com.micca.taskmanager.data.repositories.AuthRepositoryImpl
import com.micca.taskmanager.data.repositories.TaskRepositoryImpl
import com.micca.taskmanager.domain.di.RepositoryProvider
import com.micca.taskmanager.domain.repositories.AuthRepository
import com.micca.taskmanager.domain.repositories.TaskRepository

class RepositoryProviderImpl(
    private val context: Context
) : RepositoryProvider {

    private val sessionManager = SessionManager(context = context)

    private val retrofitClient = RetrofitClient(sessionManager = sessionManager)

    override val authRepository: AuthRepository = AuthRepositoryImpl(
        sessionManager = sessionManager,
        retrofitClient = retrofitClient,
    )

    override val taskRepository: TaskRepository = TaskRepositoryImpl(
        context = context,
        sessionManager = sessionManager,
        retrofitClient = retrofitClient,
    )
}
