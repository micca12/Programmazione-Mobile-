package com.micca.taskmanager.uicompose.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.micca.taskmanager.domain.models.Priority
import com.micca.taskmanager.domain.models.Status
import com.micca.taskmanager.domain.models.Task
import com.micca.taskmanager.uicompose.R
import com.micca.taskmanager.uicompose.common.Badge
import com.micca.taskmanager.uicompose.common.badgeColorsFor
import com.micca.taskmanager.uicompose.common.formatDueDate
import com.micca.taskmanager.uicompose.common.isOverdue

/**
 * Card di un task:
 *  - la priorità è il colore di sfondo (si vede a colpo d'occhio)
 *  - lo stato è un badge cliccabile che avanza al prossimo stato
 *    senza aprire il form
 * Composable stateless: riceve dati e callback, niente ViewModel.
 */
@Composable
fun TaskCard(
    task: Task,
    priority: Priority?,
    status: Status?,
    onClick: (Task) -> Unit,
    onStatusClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    val priorityTint = badgeColorsFor(priority?.color).container.copy(alpha = 0.55f)

    Card(
        colors = CardDefaults.cardColors(containerColor = priorityTint),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(task) },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (status != null) {
                    Badge(
                        text = status.name,
                        colorName = status.color,
                        onClick = { onStatusClick(task) },
                    )
                }
            }

            if (!task.description.isNullOrBlank()) {
                Text(
                    text = task.description.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = formatDueDate(task.dueDate)
                        ?: stringResource(R.string.task_no_due_date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isOverdue(task.dueDate)) {
                    Badge(
                        text = stringResource(R.string.task_overdue),
                        colorName = "amber",
                    )
                }
            }
        }
    }
}
