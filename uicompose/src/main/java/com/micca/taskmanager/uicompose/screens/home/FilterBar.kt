package com.micca.taskmanager.uicompose.screens.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.micca.taskmanager.domain.models.Priority
import com.micca.taskmanager.domain.models.SortBy
import com.micca.taskmanager.domain.models.Status
import com.micca.taskmanager.domain.models.Tag
import com.micca.taskmanager.domain.models.TaskFilters
import com.micca.taskmanager.uicompose.R
import com.micca.taskmanager.uicompose.common.SelectableBadge

/**
 * Ricerca + ordinamento + filtri a badge (tag, stato, priorità),
 * replicando la dashboard web: multi-selezione, OR interno, AND fra
 * categorie; badge selezionato = pieno.
 */
@Composable
fun FilterBar(
    filters: TaskFilters,
    tags: List<Tag>,
    statuses: List<Status>,
    priorities: List<Priority>,
    onSearchChange: (String) -> Unit,
    onSortChange: (SortBy) -> Unit,
    onToggleTag: (Int) -> Unit,
    onToggleStatus: (Int) -> Unit,
    onTogglePriority: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = filters.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(text = stringResource(R.string.filter_search)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            SortDropdown(selected = filters.sortBy, onSortChange = onSortChange)
        }

        FilterRow(
            items = statuses.map { it.id to it.name },
            selected = filters.selectedStatusIds,
            onToggle = onToggleStatus,
        )
        FilterRow(
            items = priorities.map { it.id to it.name },
            selected = filters.selectedPriorityIds,
            onToggle = onTogglePriority,
        )
        FilterRow(
            items = tags.map { it.id to it.name },
            selected = filters.selectedTagIds,
            onToggle = onToggleTag,
        )
    }
}

@Composable
private fun FilterRow(
    items: List<Pair<Int, String>>,
    selected: Set<Int>,
    onToggle: (Int) -> Unit,
) {
    if (items.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        items.forEach { (id, name) ->
            SelectableBadge(
                text = name,
                isSelected = selected.contains(id),
                onClick = { onToggle(id) },
            )
        }
    }
}

@Composable
private fun SortDropdown(
    selected: SortBy,
    onSortChange: (SortBy) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    @Composable
    fun label(sortBy: SortBy): String = stringResource(
        when (sortBy) {
            SortBy.POSITION -> R.string.sort_position
            SortBy.PRIORITY -> R.string.sort_priority
            SortBy.STATUS -> R.string.sort_status
            SortBy.DUE_DATE -> R.string.sort_due_date
        }
    )

    OutlinedButton(onClick = { expanded = true }) {
        Text(text = label(selected), style = MaterialTheme.typography.labelMedium)
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        SortBy.entries.forEach { option ->
            DropdownMenuItem(
                text = { Text(text = label(option)) },
                onClick = {
                    onSortChange(option)
                    expanded = false
                },
            )
        }
    }
}
