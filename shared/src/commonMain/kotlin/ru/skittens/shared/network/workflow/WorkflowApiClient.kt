package ru.skittens.shared.network.workflow

import ru.skittens.shared.model.workflow.*

/**
 * Интерфейс для работы с LCT EFS Workflow API
 */
interface WorkflowApiClient {
    /**
     * Сохранение workflow definition в MongoDB
     * 
     * @param request Определение workflow со states и predefined_context
     * @return ID сохраненного workflow
     */
    suspend fun saveWorkflow(request: SaveWorkflowRequest): Result<SaveWorkflowResponse>

    /**
     * Выполнение workflow: создание/обновление сессии, отправка событий
     * 
     * @param request Параметры выполнения (session_id, workflow_id, context, event_name)
     * @return Обновленное состояние workflow
     */
    suspend fun executeWorkflow(request: WorkflowExecutionRequest): Result<WorkflowExecutionResponse>

    /**
     * Проверка доступности API
     * 
     * @return Статус сервера
     */
    suspend fun healthCheck(): Result<HealthCheckResponse>
}
