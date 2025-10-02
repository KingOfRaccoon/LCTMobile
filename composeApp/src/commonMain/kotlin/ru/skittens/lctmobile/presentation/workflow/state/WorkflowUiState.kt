package ru.skittens.lctmobile.presentation.workflow.state

import ru.skittens.shared.model.workflow.ScreenData
import ru.skittens.shared.model.workflow.StateType

/**
 * UI State для workflow-driven экранов
 */
data class WorkflowUiState(
    val isLoading: Boolean = false,
    val currentState: String? = null,
    val stateType: StateType? = null,
    val screen: ScreenData? = null,
    val context: Map<String, Any> = emptyMap(),
    val visibleContext: Map<String, Any> = emptyMap(),
    val error: String? = null,
    val sessionId: String? = null,
    val workflowId: String? = null
) {
    /**
     * Устаревший метод для обратной совместимости
     */
    @Deprecated("Use currentState instead", ReplaceWith("currentState"))
    val currentScreen: String? get() = currentState
}
