package com.micca.taskmanager.data.repositories

import com.micca.taskmanager.data.local.SessionManager
import com.micca.taskmanager.data.remote.api.RetrofitClient
import com.micca.taskmanager.data.local.Session
import com.micca.taskmanager.data.remote.models.auth.toSession
import com.micca.taskmanager.data.remote.models.auth.CredentialsRequest
import com.micca.taskmanager.domain.models.ErrorState
import com.micca.taskmanager.domain.repositories.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Autenticazione contro GoTrue (Supabase) via Retrofit.
 * La sessione vive in SessionManager (DataStore + memoria).
 *
 * Gli errori sono mappati per HTTP status, senza parsare i body
 * (GoTrue e PostgREST hanno formati di errore diversi: lo status basta).
 *
 * Non c'e' rinnovo del token: alla scadenza (un'ora, default di Supabase)
 * la prima chiamata torna 401 e la UI mostra ErrorState.Unauthorized, quindi
 * si rifa' login. La copertura completa sarebbe un okhttp3.Authenticator sul
 * client REST, che intercetta ogni 401 e riprova; un refresh fatto solo
 * all'avvio, invece, coprirebbe una finestra di pochi secondi.
 */
class AuthRepositoryImpl(
    private val sessionManager: SessionManager,
    private val retrofitClient: RetrofitClient,
) : AuthRepository {

    // flow che PRIMA attende il ripristino da DataStore e POI osserva la
    // sessione: senza l'attesa, al cold start la UI vedrebbe un falso
    // "non loggato" e mostrerebbe il login a un utente con sessione valida
    override val isLoggedIn: Flow<Boolean> = flow {
        sessionManager.awaitRestore()
        emitAll(sessionManager.session.map { it != null })
    }

    private val _errorState = MutableStateFlow<ErrorState?>(null)
    override val errorState: StateFlow<ErrorState?> = _errorState

    override suspend fun signIn(email: String, password: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val response = retrofitClient.authApi.signIn(
                    body = CredentialsRequest(email = email, password = password)
                )
                val session = response.toSession()
                if (session != null) {
                    persistSession(session)
                    _errorState.emit(null)
                    true
                } else {
                    _errorState.emit(ErrorState.Generic)
                    false
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorState.emit(e.toErrorState())
                false
            }
        }

    override suspend fun signUp(email: String, password: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val response = retrofitClient.authApi.signUp(
                    body = CredentialsRequest(email = email, password = password)
                )
                val session = response.toSession()
                if (session != null) {
                    persistSession(session)
                }
                // Account creato: se session e' null serve la conferma email,
                // la UI lo capisce da isLoggedIn che resta false
                _errorState.emit(null)
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorState.emit(e.toErrorState())
                false
            }
        }

    override suspend fun signOut() {
        sessionManager.clearSession()
        // TODO: svuotare anche la cache Room (privacy fra account diversi)
    }

    private suspend fun persistSession(session: Session) {
        sessionManager.saveSession(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            userId = session.userId,
            expiresInSeconds = session.expiresAtEpochSeconds - System.currentTimeMillis() / 1000,
        )
    }
}
