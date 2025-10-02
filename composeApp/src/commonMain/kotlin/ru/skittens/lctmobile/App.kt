package ru.skittens.lctmobile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ru.skittens.lctmobile.di.AppEnvironment
import ru.skittens.lctmobile.di.createAppDependencies
import ru.skittens.lctmobile.presentation.render.BackendDrivenScreen

@Composable
fun App(environment: AppEnvironment, initialScreenId: String = "main_screen") {
    val dependencies = remember(environment) {
        createAppDependencies(
            httpClient = environment.httpClient,
            remoteOverride = environment.remoteDataSource
        )
    }
    val controller = dependencies.controller
    val uiState by controller.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(initialScreenId) {
        controller.loadScreen(initialScreenId)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(onClick = controller::refresh) {
                    androidx.compose.material3.Text("↻")
                }
            }
        ) { padding ->
            BackendDrivenScreen(
                uiState = uiState,
                actionHandler = controller.actionHandler,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}
