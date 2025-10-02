package ru.skittens.shared.domain.workflow

import ru.skittens.shared.data.workflow.WorkflowRepository
import ru.skittens.shared.model.workflow.WorkflowState

/**
 * Use Case для обновления контекста workflow без отправки события
 */
class UpdateContextUseCase(
    private val repository: WorkflowRepository
) {
    /**
     * Обновить переменные контекста
     * 
     * @param updates Map с ключами и значениями для обновления
     * @return Result с обновленным WorkflowState
     */
    suspend operator fun invoke(updates: Map<String, Any>): Result<WorkflowState> {
        return repository.updateContext(updates)
    }
}
