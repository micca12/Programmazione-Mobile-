package com.micca.taskmanager.uicompose.screens.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.micca.taskmanager.domain.models.Priority
import com.micca.taskmanager.domain.models.Status
import com.micca.taskmanager.domain.models.Tag
import com.micca.taskmanager.uicompose.R
import com.micca.taskmanager.uicompose.common.SelectableBadge

/**
 * Form per creare o modificare un task. Rispetto al web ho aggiunto: titolo
 * obbligatorio, conferma prima di eliminare, descrizione su piu' righe.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditScreen(
    viewModel: TaskEditViewModel,
    priorities: List<Priority>,
    statuses: List<Status>,
    tags: List<Tag>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val titleError by viewModel.titleError.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val done by viewModel.done.collectAsStateWithLifecycle()

    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

    // Quando l'operazione è conclusa chiudiamo la schermata (una volta sola)
    LaunchedEffect(done) {
        if (done) onClose()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (viewModel.isEditMode) R.string.edit_edit_title
                            else R.string.edit_create_title
                        )
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onClose) {
                        Text(text = stringResource(R.string.edit_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = draft.title,
                onValueChange = { viewModel.setTitle(it) },
                label = { Text(text = stringResource(R.string.edit_task_title)) },
                isError = titleError,
                supportingText = {
                    if (titleError) {
                        Text(text = stringResource(R.string.edit_task_title_required))
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = draft.description.orEmpty(),
                onValueChange = { viewModel.setDescription(it) },
                label = { Text(text = stringResource(R.string.edit_description)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            LookupDropdown(
                label = stringResource(R.string.edit_priority),
                options = priorities.map { it.id to it.name },
                selectedId = draft.priorityId,
                onSelect = { viewModel.setPriority(it) },
            )

            LookupDropdown(
                label = stringResource(R.string.edit_status),
                options = statuses.map { it.id to it.name },
                selectedId = draft.statusId,
                onSelect = { viewModel.setStatus(it) },
            )

            Text(
                text = stringResource(R.string.edit_tags),
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tags.forEach { tag ->
                    SelectableBadge(
                        text = tag.name,
                        isSelected = draft.tagIds.contains(tag.id),
                        onClick = { viewModel.toggleTag(tag.id) },
                    )
                }
            }

            Text(
                text = stringResource(R.string.edit_due_date),
                style = MaterialTheme.typography.titleSmall,
            )
            DueDateTimePicker(
                isoDate = draft.dueDate,
                onDateChange = { viewModel.setDueDate(it) },
            )

            Button(
                onClick = { viewModel.save() },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.edit_save))
            }

            if (viewModel.isEditMode) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.edit_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(text = stringResource(R.string.edit_delete_confirm_title)) },
            text = { Text(text = stringResource(R.string.edit_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.delete()
                    }
                ) { Text(text = stringResource(R.string.edit_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(text = stringResource(R.string.edit_cancel))
                }
            },
        )
    }
}

/** Select semplice (equivalente delle <select> del form web). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LookupDropdown(
    label: String,
    options: List<Pair<Int, String>>,
    selectedId: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedName = options.find { it.first == selectedId }?.second ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(text = name) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    },
                )
            }
        }
    }
}
