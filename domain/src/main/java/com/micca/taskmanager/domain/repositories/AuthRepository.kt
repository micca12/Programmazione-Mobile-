package com.micca.taskmanager.domain.repositories

import com.micca.taskmanager.domain.models.ErrorState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Autenticazione Supabase (email + password). La sessione è persistita
 * in DataStore dall'implementazione, così l'app riparte loggata.
 */
interface AuthRepository {

    /** Emette solo dopo il ripristino della sessione da disco. */
    val isLoggedIn: Flow<Boolean>

    val errorState: StateFlow<ErrorState?>

    suspend fun signIn(email: String, password: String): Boolean
    suspend fun signUp(email: String, password: String): Boolean
    suspend fun signOut()
}
