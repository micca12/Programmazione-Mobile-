package com.micca.taskmanager.uicompose.screens.edit

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.micca.taskmanager.uicompose.R
import com.micca.taskmanager.uicompose.common.formatDueDate
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * Selettore di scadenza (data + ora), equivalente del VueDatePicker web.
 * Due dialog Material3 in sequenza: DatePicker, poi TimePicker.
 * Produce/consuma stringhe ISO-8601 con offset (il formato PostgREST).
 *
 * La scadenza è opzionale: "Rimuovi" la azzera — lato rete funziona
 * grazie alla null-serialization del converter (la PATCH invia
 * esplicitamente due_date: null).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueDateTimePicker(
    isoDate: String?,
    onDateChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var pickedDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = formatDueDate(isoDate) ?: stringResource(R.string.edit_no_due_date),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (isoDate != null) {
            TextButton(onClick = { onDateChange(null) }) {
                Text(text = stringResource(R.string.edit_remove_due_date))
            }
        }
        TextButton(onClick = { showDatePicker = true }) {
            Text(text = stringResource(R.string.edit_pick_date))
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedDateMillis = dateState.selectedDateMillis
                        showDatePicker = false
                        if (pickedDateMillis != null) showTimePicker = true
                    }
                ) { Text(text = stringResource(R.string.edit_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = stringResource(R.string.edit_cancel))
                }
            },
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                        val dateMillis = pickedDateMillis ?: return@TextButton
                        // il DatePicker torna la mezzanotte UTC del giorno scelto:
                        // prendo solo la data (letta in UTC) e ci attacco l'ora
                        // scelta, in ora locale
                        val localDate = Instant.ofEpochMilli(dateMillis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        val localDateTime = localDate.atTime(
                            LocalTime.of(timeState.hour, timeState.minute)
                        )
                        val iso: String = localDateTime
                            .atZone(ZoneId.systemDefault())
                            .toOffsetDateTime()
                            .toString()
                        onDateChange(iso)
                    }
                ) { Text(text = stringResource(R.string.edit_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(text = stringResource(R.string.edit_cancel))
                }
            },
            text = { TimePicker(state = timeState) },
        )
    }
}
