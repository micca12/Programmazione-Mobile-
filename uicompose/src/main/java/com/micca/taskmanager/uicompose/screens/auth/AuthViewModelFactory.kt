package com.micca.taskmanager.uicompose.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.micca.taskmanager.domain.repositories.AuthRepository

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository = authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
