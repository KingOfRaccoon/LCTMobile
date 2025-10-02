package ru.skittens.shared.model.workflow

/**
 * Sealed class для обработки ошибок workflow
 */
sealed class WorkflowError : Exception() {
    data class NetworkError(override val message: String) : WorkflowError()
    
    data class ServerError(
        val code: Int, 
        override val message: String
    ) : WorkflowError()
    
    object SessionExpired : WorkflowError() {
        override val message: String = "Workflow session expired"
    }
    
    data class ValidationError(override val message: String) : WorkflowError()
    
    data class ParseError(override val message: String) : WorkflowError()
    
    data class UnknownError(override val message: String) : WorkflowError()
}
