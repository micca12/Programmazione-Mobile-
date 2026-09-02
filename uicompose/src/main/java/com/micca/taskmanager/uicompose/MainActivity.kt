package com.micca.taskmanager.uicompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.micca.taskmanager.domain.di.AppContainer
import com.micca.taskmanager.uicompose.common.Loader
import com.micca.taskmanager.uicompose.screens.auth.AuthScreen
import com.micca.taskmanager.uicompose.screens.auth.AuthViewModel
import com.micca.taskmanager.uicompose.screens.auth.AuthViewModelFactory
import com.micca.taskmanager.uicompose.screens.edit.TaskEditScreen
import com.micca.taskmanager.uicompose.screens.edit.TaskEditViewModel
import com.micca.taskmanager.uicompose.screens.edit.TaskEditViewModelFactory
import com.micca.taskmanager.uicompose.screens.home.HomeScreen
import com.micca.taskmanager.uicompose.screens.home.HomeViewModel
import com.micca.taskmanager.uicompose.screens.home.HomeViewModelFactory
import com.micca.taskmanager.uicompose.ui.theme.TaskManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskManagerTheme {
                AppRoot()
            }
        }
    }
}

/**
 * Radice dell'app: decide la schermata in base alla sessione, senza
 * Navigation Compose. isLoggedIn parte a null = "ancora non lo so" -> Loader:
 * cosi' non mostro il login a chi risultera' gia' loggato un attimo dopo.
 */
@Composable
private fun AppRoot() {
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        AppContainer.authRepository.isLoggedIn.collect { isLoggedIn = it }
    }

    when (isLoggedIn) {
        null -> Loader()
        false -> AuthDestination()
        true -> LoggedInDestinations()
    }
}

@Composable
private fun AuthDestination() {
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            authRepository = AppContainer.authRepository,
        )
    )
    AuthScreen(viewModel = authViewModel)
}

/**
 * Navigazione interna da loggati, con lo stato minimo possibile:
 *  - null  -> dashboard
 *  - ""    -> form in creazione
 *  - uuid  -> form in modifica di quel task
 * rememberSaveable: la destinazione sopravvive alla rotazione.
 */
@Composable
private fun LoggedInDestinations() {
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            taskRepository = AppContainer.taskRepository,
            authRepository = AppContainer.authRepository,
            filterAndSortTasks = AppContainer.filterAndSortTasks,
        )
    )

    var editingTaskId by rememberSaveable { mutableStateOf<String?>(null) }

    if (editingTaskId == null) {
        HomeScreen(
            viewModel = homeViewModel,
            onTaskClick = { task -> editingTaskId = task.id },
            onCreateClick = { editingTaskId = "" },
        )
    } else {
        val editViewModel: TaskEditViewModel = viewModel(
            factory = TaskEditViewModelFactory(
                taskRepository = AppContainer.taskRepository,
            )
        )
        val tasks by homeViewModel.tasks.collectAsStateWithLifecycle()
        val priorities by homeViewModel.priorities.collectAsStateWithLifecycle()
        val statuses by homeViewModel.statuses.collectAsStateWithLifecycle()
        val tags by homeViewModel.tags.collectAsStateWithLifecycle()

        // Carica il draft quando si entra nel form: id vuoto -> find fallisce
        // -> load(null) -> valori di default (task nuovo)
        LaunchedEffect(editingTaskId) {
            editViewModel.load(tasks.find { it.id == editingTaskId })
        }

        // Il tasto back chiude il form invece di uscire dall'app
        BackHandler { editingTaskId = null }

        TaskEditScreen(
            viewModel = editViewModel,
            priorities = priorities,
            statuses = statuses,
            tags = tags,
            onClose = { editingTaskId = null },
        )
    }
}
