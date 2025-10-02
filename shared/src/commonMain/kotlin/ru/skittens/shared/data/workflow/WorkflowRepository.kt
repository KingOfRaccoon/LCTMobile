package ru.skittens.shared.data.workflow

import kotlinx.coroutines.flow.Flow
import ru.skittens.shared.model.workflow.*

/**
 * Repository для управления workflow состоянием и сессиями
 */
interface WorkflowRepository {
    /**
     * Сохранить workflow definition
     */
    suspend fun saveWorkflow(
        states: StateSet,
        predefinedContext: Map<String, Any>
    ): Result<String>

    /**
     * Запустить новый workflow или получить существующую сессию
     * 
     * @param workflowId ID workflow для инициализации (опционально если сессия существует)
     * @return WorkflowState с текущим контекстом
     */
    suspend fun startWorkflow(workflowId: String? = null): Result<WorkflowState>

    /**
     * Отправить событие в workflow (триггерит переходы Screen states)
     * 
     * @param eventName Имя события
     * @param context Дополнительный контекст для обновления
     */
    suspend fun sendEvent(
        eventName: String,
        context: Map<String, Any> = emptyMap()
    ): Result<WorkflowState>

    /**
     * Обновить контекст workflow без отправки события
     */
    suspend fun updateContext(context: Map<String, Any>): Result<WorkflowState>

    /**
     * Получить текущий session_id
     */
    fun getCurrentSessionId(): String?

    /**
     * Получить текущий workflow_id
     */
    fun getCurrentWorkflowId(): String?

    /**
     * Очистить текущую сессию
     */
    fun clearSession()

    /**
     * Reactive stream текущего состояния workflow
     */
    fun observeWorkflowState(): Flow<WorkflowState?>
}
