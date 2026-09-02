package com.micca.taskmanager.domain.models

// sealed class invece di una stringa: cosi' il traducibile lo fa la UI,
// data non deve sapere in che lingua sta l'utente
sealed class ErrorState {
    /** Errore di rete: timeout, connessione assente. */
    data object Network : ErrorState()

    /** Sessione scaduta o credenziali non valide (HTTP 401/403 da Supabase). */
    data object Unauthorized : ErrorState()

    /** Qualsiasi altro errore. */
    data object Generic : ErrorState()
}
