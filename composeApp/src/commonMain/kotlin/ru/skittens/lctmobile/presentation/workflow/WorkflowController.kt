package ru.skittens.lctmobile.presentation.workflow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.skittens.lctmobile.presentation.workflow.intent.WorkflowIntent
import ru.skittens.lctmobile.presentation.workflow.state.WorkflowUiState
import ru.skittens.shared.domain.workflow.*
import ru.skittens.shared.model.workflow.WorkflowError
import ru.skittens.shared.model.workflow.WorkflowState

/**
 * Controller для управления workflow state (MVI/MVVM pattern)
 * Использует StateFlow для reactive UI updates
 */
class WorkflowController(
    private val startWorkflow: StartWorkflowUseCase,
    private val sendEvent: SendWorkflowEventUseCase,
    private val updateContext: UpdateContextUseCase,
    private val observeWorkflowState: ObserveWorkflowStateUseCase,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(WorkflowUiState())
    val uiState: StateFlow<WorkflowUiState> = _uiState.asStateFlow()

    init {
        // Observe workflow state changes
        scope.launch {
            observeWorkflowState().collect { workflowState ->
                workflowState?.let {
                    updateUiState(it)
                }
            }
        }
    }

    /**
     * Обработка intent от UI
     */
    fun onIntent(intent: WorkflowIntent) {
        when (intent) {
            is WorkflowIntent.StartWorkflow -> startWorkflowFlow(intent.workflowId)
            is WorkflowIntent.SendEvent -> sendEventFlow(intent.eventName, intent.data)
            is WorkflowIntent.UpdateContext -> updateContextFlow(intent.updates)
            is WorkflowIntent.ClearError -> clearError()
            is WorkflowIntent.ResetWorkflow -> resetWorkflow()
        }
    }

    /**
     * Запуск workflow
     */
    private fun startWorkflowFlow(workflowId: String?) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            startWorkflow(workflowId)
                .onSuccess { workflowState ->
                    updateUiState(workflowState)
                }
                .onFailure { error ->
                    handleError(error)
                }
        }
    }

    /**
     * Отправка события
     */
    private fun sendEventFlow(eventName: String, data: Map<String, Any>) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            sendEvent(eventName, data)
                .onSuccess { workflowState ->
                    updateUiState(workflowState)
                }
                .onFailure { error ->
                    handleError(error)
                }
        }
    }

    /**
     * Обновление контекста
     */
    private fun updateContextFlow(updates: Map<String, Any>) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            updateContext(updates)
                .onSuccess { workflowState ->
                    updateUiState(workflowState)
                }
                .onFailure { error ->
                    handleError(error)
                }
        }
    }

    /**
     * Обновление UI state из domain модели
     */
    private fun updateUiState(workflowState: WorkflowState) {
        _uiState.update {
            it.copy(
                isLoading = false,
                currentState = workflowState.currentState,
                stateType = workflowState.stateType,
                screen = workflowState.screen,
                context = workflowState.context,
                visibleContext = WorkflowState.filterUserVisibleContext(workflowState.context),
                error = if (workflowState.isError) "Workflow error state" else null,
                sessionId = workflowState.sessionId,
                workflowId = workflowState.workflowId
            )
        }
    }

    /**
     * Обработка ошибок
     */
    private fun handleError(error: Throwable) {
        val errorMessage = when (error) {
            is WorkflowError.NetworkError -> "Network error: ${error.message}"
            is WorkflowError.ServerError -> "Server error (${error.code}): ${error.message}"
            is WorkflowError.SessionExpired -> "Session expired. Please restart workflow."
            is WorkflowError.ValidationError -> "Validation error: ${error.message}"
            is WorkflowError.ParseError -> "Parse error: ${error.message}"
            else -> "Unknown error: ${error.message}"
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                error = errorMessage
            )
        }
    }

    /**
     * Очистка ошибки
     */
    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Сброс workflow
     */
    private fun resetWorkflow() {
        _uiState.value = WorkflowUiState()
    }
}
