package ru.skittens.lctmobile.presentation.workflow.intent

/**
 * Actions/Intents для WorkflowController (MVI pattern)
 */
sealed class WorkflowIntent {
    /**
     * Запустить workflow (или получить существующую сессию)
     */
    data class StartWorkflow(val workflowId: String?) : WorkflowIntent()

    /**
     * Отправить событие в workflow FSM
     */
    data class SendEvent(
        val eventName: String,
        val data: Map<String, Any> = emptyMap()
    ) : WorkflowIntent()

    /**
     * Обновить контекст workflow
     */
    data class UpdateContext(val updates: Map<String, Any>) : WorkflowIntent()

    /**
     * Очистить ошибку
     */
    object ClearError : WorkflowIntent()

    /**
     * Сбросить workflow сессию
     */
    object ResetWorkflow : WorkflowIntent()
}
