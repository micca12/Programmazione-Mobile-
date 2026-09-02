package com.micca.taskmanager.uicompose.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.micca.taskmanager.domain.models.ErrorState
import com.micca.taskmanager.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// stato della schermata di login/registrazione (un form solo, con toggle)
class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** Errore dell'ultima operazione (dal repository, tipizzato). */
    val authError: StateFlow<ErrorState?> = authRepository.errorState

    /** true dopo un signup andato a buon fine ma senza sessione:
     *  il progetto Supabase richiede la conferma via email. */
    private val _signUpNeedsConfirmation = MutableStateFlow(false)
    val signUpNeedsConfirmation: StateFlow<Boolean> = _signUpNeedsConfirmation

    fun toggleMode() {
        _isLoginMode.value = !_isLoginMode.value
        _signUpNeedsConfirmation.value = false
    }

    fun submit(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.emit(true)
            _signUpNeedsConfirmation.emit(false)
            if (_isLoginMode.value) {
                authRepository.signIn(email = email, password = password)
                // esito osservato da fuori: isLoggedIn cambia da solo
            } else {
                val created = authRepository.signUp(email = email, password = password)
                if (created) {
                    // se la sessione non è arrivata, serve la conferma email
                    _signUpNeedsConfirmation.emit(true)
                }
            }
            _isLoading.emit(false)
        }
    }
}
