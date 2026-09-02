package com.micca.taskmanager.uicompose.common

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Le date viaggiano come stringhe ISO-8601 con offset (formato PostgREST,
 * es. 2026-09-01T14:30:00.123456+00:00) e diventano leggibili solo qui.
 * minSdk 31 -> java.time nativo, niente desugaring.
 */

private val displayFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")

/** "dd/MM HH:mm" in ora locale, o null se la data non si riesce a leggere */
fun formatDueDate(isoDate: String?): String? {
    isoDate ?: return null
    return try {
        OffsetDateTime.parse(isoDate)
            .atZoneSameInstant(ZoneId.systemDefault())
            .format(displayFormatter)
    } catch (e: Exception) {
        null
    }
}

/**
 * true se la scadenza è passata. Come nel web: conta solo la data,
 * indipendentemente dallo stato del task.
 */
fun isOverdue(isoDate: String?): Boolean {
    isoDate ?: return false
    return try {
        OffsetDateTime.parse(isoDate).toInstant().isBefore(Instant.now())
    } catch (e: Exception) {
        false
    }
}
