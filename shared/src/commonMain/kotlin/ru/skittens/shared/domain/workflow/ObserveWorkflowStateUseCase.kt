package ru.skittens.shared.domain.workflow

import kotlinx.coroutines.flow.Flow
import ru.skittens.shared.data.workflow.WorkflowRepository
import ru.skittens.shared.model.workflow.WorkflowState

/**
 * Use Case для наблюдения за изменениями состояния workflow
 */
class ObserveWorkflowStateUseCase(
    private val repository: WorkflowRepository
) {
    /**
     * Получить Flow с текущим состоянием workflow
     * 
     * @return Flow<WorkflowState?> для reactive updates
     */
    operator fun invoke(): Flow<WorkflowState?> {
        return repository.observeWorkflowState()
    }
}
