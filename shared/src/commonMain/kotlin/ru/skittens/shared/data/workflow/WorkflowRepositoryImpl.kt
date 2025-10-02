package ru.skittens.shared.data.workflow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.*
import ru.skittens.shared.model.workflow.*
import ru.skittens.shared.network.workflow.WorkflowApiClient
import ru.skittens.shared.util.session.SessionManager

/**
 * Реализация WorkflowRepository с in-memory state management
 */
class WorkflowRepositoryImpl(
    private val apiClient: WorkflowApiClient,
    private val sessionManager: SessionManager
) : WorkflowRepository {

    private val _currentState = MutableStateFlow<WorkflowState?>(null)

    override suspend fun saveWorkflow(
        states: StateSet,
        predefinedContext: Map<String, Any>
    ): Result<String> {
        val jsonContext = predefinedContext.mapValues { (_, value) ->
            value.toJsonElement()
        }

        val request = SaveWorkflowRequest(
            states = states,
            predefinedContext = jsonContext
        )

        return apiClient.saveWorkflow(request).mapCatching { response ->
            // Сохраняем workflow_id для последующего использования
            sessionManager.saveWorkflowId(response.wfDescriptionId)
            response.wfDescriptionId
        }
    }

    override suspend fun startWorkflow(workflowId: String?): Result<WorkflowState> {
        val sessionId = sessionManager.getSessionId()
        val effectiveWorkflowId = workflowId ?: sessionManager.getWorkflowId()

        if (effectiveWorkflowId == null && !sessionManager.hasActiveSession()) {
            return Result.failure(
                WorkflowError.ValidationError("workflow_id is required for new session")
            )
        }

        val request = WorkflowExecutionRequest(
            clientSessionId = sessionId,
            clientWorkflowId = effectiveWorkflowId
        )

        return apiClient.executeWorkflow(request).mapCatching { response ->
            // Обновляем session_id если он изменился
            sessionManager.updateSessionId(response.sessionId)

            val workflowState = response.toWorkflowState()
            _currentState.value = workflowState
            workflowState
        }
    }

    override suspend fun sendEvent(
        eventName: String,
        context: Map<String, Any>
    ): Result<WorkflowState> {
        val sessionId = sessionManager.getSessionId()
        val jsonContext = context.mapValues { (_, value) ->
            value.toJsonElement()
        }

        val request = WorkflowExecutionRequest(
            clientSessionId = sessionId,
            eventName = eventName,
            context = jsonContext
        )

        return apiClient.executeWorkflow(request).mapCatching { response ->
            val workflowState = response.toWorkflowState()
            _currentState.value = workflowState
            workflowState
        }
    }

    override suspend fun updateContext(context: Map<String, Any>): Result<WorkflowState> {
        val sessionId = sessionManager.getSessionId()
        val jsonContext = context.mapValues { (_, value) ->
            value.toJsonElement()
        }

        val request = WorkflowExecutionRequest(
            clientSessionId = sessionId,
            context = jsonContext
        )

        return apiClient.executeWorkflow(request).mapCatching { response ->
            val workflowState = response.toWorkflowState()
            _currentState.value = workflowState
            workflowState
        }
    }

    override fun getCurrentSessionId(): String? {
        return if (sessionManager.hasActiveSession()) {
            sessionManager.getSessionId()
        } else {
            null
        }
    }

    override fun getCurrentWorkflowId(): String? {
        return sessionManager.getWorkflowId()
    }

    override fun clearSession() {
        sessionManager.clearSession()
        _currentState.value = null
    }

    override fun observeWorkflowState(): Flow<WorkflowState?> {
        return _currentState.asStateFlow()
    }

    /**
     * Преобразование WorkflowExecutionResponse в domain модель
     */
    private fun WorkflowExecutionResponse.toWorkflowState(): WorkflowState {
        val contextMap = context.mapValues { (_, value) ->
            value.toAny()
        }

        val workflowId = contextMap[WorkflowState.WORKFLOW_ID_KEY] as? String ?: ""
        val createdAt = contextMap[WorkflowState.CREATED_AT_KEY] as? String
        val isError = currentState == WorkflowState.ERROR_STATE_KEY

        return WorkflowState(
            sessionId = sessionId,
            workflowId = workflowId,
            currentState = currentState,
            stateType = stateType,
            context = contextMap,
            screen = screen,
            createdAt = createdAt,
            isError = isError
        )
    }

    /**
     * Конвертация Kotlin типов в JsonElement
     */
    private fun Any.toJsonElement(): JsonElement = when (this) {
        is String -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is Map<*, *> -> JsonObject(
            this.mapKeys { it.key.toString() }
                .mapValues { it.value?.toJsonElement() ?: JsonNull }
        )
        is List<*> -> JsonArray(this.map { it?.toJsonElement() ?: JsonNull })
        else -> JsonPrimitive(this.toString())
    }

    /**
     * Конвертация JsonElement в Kotlin типы
     */
    private fun JsonElement.toAny(): Any = when (this) {
        is JsonPrimitive -> {
            when {
                this.isString -> this.content
                this.booleanOrNull != null -> this.boolean
                this.longOrNull != null -> this.long
                this.doubleOrNull != null -> this.double
                else -> this.content
            }
        }
        is JsonObject -> this.mapValues { it.value.toAny() }
        is JsonArray -> this.map { it.toAny() }
        JsonNull -> "null"
    }
}
