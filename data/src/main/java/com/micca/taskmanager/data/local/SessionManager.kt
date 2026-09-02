package com.micca.taskmanager.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// il DataStore va creato una volta sola per processo
private val Context.dataStore by preferencesDataStore(name = "session_prefs")

data class Session(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val expiresAtEpochSeconds: Long,
)

/**
 * Tiene la sessione Supabase: la salva su DataStore (così l'app riparte
 * loggata) e la espone in memoria per l'interceptor.
 */
class SessionManager(
    private val context: Context,
) {

    companion object {
        private const val TAG = "SessionManager"

        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_EXPIRES_AT = longPreferencesKey("expires_at")
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session

    // diventa true quando il ripristino da DataStore è finito.
    // Serve perché la lettura da disco è asincrona: se una chiamata parte
    // prima, va senza Bearer e Supabase (RLS) risponde 200 [] senza errore.
    private val _restored = MutableStateFlow(false)

    init {
        scope.launch {
            try {
                val prefs = context.dataStore.data.first()
                val access = prefs[KEY_ACCESS_TOKEN]
                val refresh = prefs[KEY_REFRESH_TOKEN]
                val userId = prefs[KEY_USER_ID]
                val expiresAt = prefs[KEY_EXPIRES_AT]
                if (access != null && refresh != null && userId != null && expiresAt != null) {
                    _session.value = Session(access, refresh, userId, expiresAt)
                    Log.d(TAG, "Sessione ripristinata per utente $userId")
                }
            } catch (e: Exception) {
                // DataStore illeggibile: meglio "non loggato" che un crash
                Log.e(TAG, "Ripristino sessione fallito", e)
            } finally {
                _restored.value = true
            }
        }
    }

    /** Aspetta che il ripristino da disco sia finito. */
    suspend fun awaitRestore() {
        _restored.filter { it }.first()
    }

    fun getAccessToken(): String? = _session.value?.accessToken

    val userId: String?
        get() = _session.value?.userId

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        expiresInSeconds: Long,
    ) {
        val expiresAt = System.currentTimeMillis() / 1000 + expiresInSeconds
        _session.value = Session(accessToken, refreshToken, userId, expiresAt)
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID] = userId
            prefs[KEY_EXPIRES_AT] = expiresAt
        }
    }

    suspend fun clearSession() {
        _session.value = null
        try {
            context.dataStore.edit { prefs -> prefs.clear() }
        } catch (e: Exception) {
            Log.e(TAG, "Pulizia DataStore fallita", e)
        }
        Log.d(TAG, "Sessione cancellata")
    }
}
