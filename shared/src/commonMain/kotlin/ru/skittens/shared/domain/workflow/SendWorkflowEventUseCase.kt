package ru.skittens.shared.domain.workflow

import ru.skittens.shared.data.workflow.WorkflowRepository
import ru.skittens.shared.model.workflow.WorkflowState

/**
 * Use Case для отправки события в workflow (триггерит FSM переходы)
 */
class SendWorkflowEventUseCase(
    private val repository: WorkflowRepository
) {
    /**
     * Отправить событие с опциональным обновлением контекста
     * 
     * @param eventName Имя события для триггера перехода
     * @param contextUpdates Дополнительные данные для обновления контекста
     * @return Result с обновленным WorkflowState
     */
    suspend operator fun invoke(
        eventName: String,
        contextUpdates: Map<String, Any> = emptyMap()
    ): Result<WorkflowState> {
        return repository.sendEvent(eventName, contextUpdates)
    }
}
