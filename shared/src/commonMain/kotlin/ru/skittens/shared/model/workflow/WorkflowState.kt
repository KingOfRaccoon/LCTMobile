package ru.skittens.shared.model.workflow

/**
 * Domain модель состояния workflow для use cases и presentation layer
 */
data class WorkflowState(
    val sessionId: String,
    val workflowId: String,
    val currentState: String,
    val stateType: StateType,
    val context: Map<String, Any>,
    val screen: ScreenData? = null,
    val createdAt: String? = null,
    val isError: Boolean = false
) {
    companion object {
        // Служебные переменные с префиксом __
        const val WORKFLOW_ID_KEY = "__workflow_id"
        const val CREATED_AT_KEY = "__created_at"
        const val CURRENT_STATE_KEY = "current_state"
        const val ERROR_STATE_KEY = "__error__"

        /**
         * Фильтрует служебные переменные для отображения в UI
         */
        fun filterUserVisibleContext(context: Map<String, Any>): Map<String, Any> {
            return context.filterKeys { !it.startsWith("__") }
        }
    }

    /**
     * Устаревший метод для обратной совместимости
     */
    @Deprecated("Use currentState instead", ReplaceWith("currentState"))
    val currentScreen: String get() = currentState
}
