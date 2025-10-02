package ru.skittens.lctmobile.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.skittens.lctmobile.presentation.state.BackendDrivenUiState
import ru.skittens.lctmobile.presentation.state.reduce
import ru.skittens.lctmobile.presentation.action.ActionHandler
import ru.skittens.lctmobile.presentation.action.DefaultActionHandler
import ru.skittens.shared.domain.LoadScreenUseCase

class BackendDrivenController(
    private val loadScreenUseCase: LoadScreenUseCase,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(BackendDrivenUiState(screenId = ""))
    val state: StateFlow<BackendDrivenUiState> = _state.asStateFlow()

    private var currentScreenJob: Job? = null
    
    // ActionHandler for handling UI actions from schema
    val actionHandler: ActionHandler = DefaultActionHandler(
        onNavigate = { screenId, clearStack ->
            if (clearStack) {
                _state.value = BackendDrivenUiState(screenId = "")
            }
            loadScreen(screenId, refresh = false)
        },
        onNavigateBack = {
            // TODO: implement navigation back
            println("Navigate back requested")
        },
        onShowSnackbar = { message, actionLabel ->
            // TODO: integrate with SnackbarHost
            println("Snackbar: $message")
        },
        onShowDialog = { title, message, confirmLabel, cancelLabel, onConfirm, onCancel ->
            // TODO: integrate with Dialog
            println("Dialog: $title - $message")
        },
        onRefresh = {
            refresh()
        },
        onApiCall = { endpoint, method, body ->
            // TODO: implement API call through repository
            println("API Call: $method $endpoint")
            Result.success(Unit)
        }
    )

    fun loadScreen(screenId: String, refresh: Boolean = true) {
        if (_state.value.screenId != screenId) {
            _state.value = BackendDrivenUiState(screenId = screenId)
        }
        currentScreenJob?.cancel()
        currentScreenJob = scope.launch {
            loadScreenUseCase(screenId, refresh).collect { result ->
                _state.value = _state.value.reduce(result)
            }
        }
    }

    fun refresh() {
        val id = _state.value.screenId
        if (id.isNotEmpty()) {
            loadScreen(id, refresh = true)
        }
    }
}
