package com.micca.taskmanager.data.repositories

import com.micca.taskmanager.domain.models.ErrorState
import retrofit2.HttpException
import java.io.IOException

// Traduce un'eccezione di rete in ErrorState guardando lo status HTTP
// (non il body: GoTrue e PostgREST hanno formati d'errore diversi).
internal fun Exception.toErrorState(): ErrorState = when (this) {
    is HttpException -> when (code()) {
        400, 401, 403 -> ErrorState.Unauthorized
        else -> ErrorState.Generic
    }
    is IOException -> ErrorState.Network
    else -> ErrorState.Generic
}
