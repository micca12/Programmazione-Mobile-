package com.micca.taskmanager.uicompose.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.micca.taskmanager.domain.models.Task
import com.micca.taskmanager.uicompose.R
import com.micca.taskmanager.uicompose.common.ErrorBanner
import com.micca.taskmanager.uicompose.common.Loader

/**
 * Dashboard: la lista delle card dei task.
 * Quattro stati: loading / errore / vuoto / contenuto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onTaskClick: (Task) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tasks by viewModel.visibleTasks.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val priorities by viewModel.priorities.collectAsStateWithLifecycle()
    val statuses by viewModel.statuses.collectAsStateWithLifecycle()
    val errorState by viewModel.errorState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.home_title)) },
                actions = {
                    TextButton(onClick = { viewModel.signOut() }) {
                        Text(text = stringResource(R.string.home_logout))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            val error = errorState
            if (error != null) {
                ErrorBanner(error = error, onRetry = { viewModel.startFetch() })
            }

            FilterBar(
                filters = filters,
                tags = tags,
                statuses = statuses,
                priorities = priorities,
                onSearchChange = { viewModel.setSearchQuery(it) },
                onSortChange = { viewModel.setSortBy(it) },
                onToggleTag = { viewModel.toggleTagFilter(it) },
                onToggleStatus = { viewModel.toggleStatusFilter(it) },
                onTogglePriority = { viewModel.togglePriorityFilter(it) },
            )

            when {
                isLoading -> Loader()

                tasks.isEmpty() -> Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(R.string.home_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items = tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            priority = viewModel.priorityFor(task.priorityId),
                            status = viewModel.statusFor(task.statusId),
                            onClick = onTaskClick,
                            onStatusClick = { viewModel.cycleStatus(it) },
                        )
                    }
                }
            }
        }
    }
}
