package com.micca.taskmanager.data.remote.models.auth

import com.micca.taskmanager.data.local.Session

/**
 * Converte la risposta di auth in una Session, o null se incompleta
 * (es. signup con conferma email attiva: nessun token).
 */
internal fun AuthResponse.toSession(): Session? {
    val access = accessToken ?: return null
    val refresh = refreshToken ?: return null
    val id = user?.id ?: return null
    val expiresIn = expiresIn ?: 3600L
    return Session(
        accessToken = access,
        refreshToken = refresh,
        userId = id,
        expiresAtEpochSeconds = System.currentTimeMillis() / 1000 + expiresIn,
    )
}
