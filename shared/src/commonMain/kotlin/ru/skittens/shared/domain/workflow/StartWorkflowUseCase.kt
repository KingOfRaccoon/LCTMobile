package ru.skittens.shared.domain.workflow

import ru.skittens.shared.data.workflow.WorkflowRepository
import ru.skittens.shared.model.workflow.WorkflowState

/**
 * Use Case для запуска workflow или получения текущей сессии
 */
class StartWorkflowUseCase(
    private val repository: WorkflowRepository
) {
    /**
     * Запустить workflow
     * 
     * @param workflowId ID workflow (опционально если сессия уже существует)
     * @return Result с WorkflowState
     */
    suspend operator fun invoke(workflowId: String? = null): Result<WorkflowState> {
        return repository.startWorkflow(workflowId)
    }
}
