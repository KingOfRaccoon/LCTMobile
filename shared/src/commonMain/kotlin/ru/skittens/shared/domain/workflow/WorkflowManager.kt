package ru.skittens.shared.domain.workflow

import ru.skittens.shared.data.workflow.WorkflowRepository
import ru.skittens.shared.model.workflow.StateSet
import ru.skittens.shared.model.workflow.WorkflowState
import java.util.UUID

/**
 * WorkflowManager - высокоуровневый фасад для работы с Workflow Engine
 * 
 * Упрощает использование workflow API, предоставляя понятные методы:
 * - startWorkflow() - начать новый workflow
 * - sendEvent() - отправить событие и получить следующее состояние
 * - restoreSession() - восстановить сессию после закрытия приложения
 * - endSession() - завершить текущую сессию
 */
class WorkflowManager(
    private val repository: WorkflowRepository
) {
    /**
     * Начать новый workflow
     * 
     * @param workflowId ID workflow для запуска
     * @param initialContext Начальный контекст (user_id, device_type, etc.)
     * @return WorkflowState с первым экраном или ошибка
     */
    suspend fun startWorkflow(
        workflowId: String,
        initialContext: Map<String, Any> = emptyMap()
    ): Result<WorkflowState> {
        return try {
            // Добавляем контекст к репозиторию если нужно
            if (initialContext.isNotEmpty()) {
                repository.updateContext(initialContext)
            }
            
            repository.startWorkflow(workflowId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Отправить событие в workflow
     * 
     * Используется для перехода между screen states.
     * События определяются в expressions состояний workflow.
     * 
     * @param eventName Имя события (например, "submit", "next", "cancel")
     * @param additionalContext Дополнительные данные (form data, user input)
     * @return WorkflowState со следующим состоянием или ошибка
     */
    suspend fun sendEvent(
        eventName: String,
        additionalContext: Map<String, Any> = emptyMap()
    ): Result<WorkflowState> {
        return repository.sendEvent(eventName, additionalContext)
    }

    /**
     * Восстановить сессию после закрытия приложения
     * 
     * Если сессия существует и не истекла, возвращает текущее состояние.
     * 
     * @param sessionId ID сессии для восстановления (опционально)
     * @return WorkflowState с текущим состоянием или null если сессия не найдена
     */
    suspend fun restoreSession(sessionId: String? = null): Result<WorkflowState>? {
        val currentSessionId = sessionId ?: repository.getCurrentSessionId()
        
        return if (currentSessionId != null) {
            repository.startWorkflow() // Без workflow_id - восстанавливает сессию
        } else {
            null
        }
    }

    /**
     * Обновить контекст workflow без отправки события
     * 
     * Полезно для постепенного заполнения данных перед отправкой.
     * 
     * @param updates Данные для обновления контекста
     * @return WorkflowState с обновленным контекстом
     */
    suspend fun updateContext(updates: Map<String, Any>): Result<WorkflowState> {
        return repository.updateContext(updates)
    }

    /**
     * Завершить текущую сессию
     * 
     * Очищает локальное хранилище session_id.
     * Сервер автоматически удалит сессию через TTL.
     */
    fun endSession() {
        repository.clearSession()
    }

    /**
     * Получить ID текущей сессии
     * 
     * @return session_id или null если сессия не активна
     */
    fun getCurrentSessionId(): String? {
        return repository.getCurrentSessionId()
    }

    /**
     * Получить ID текущего workflow
     * 
     * @return workflow_id или null если workflow не запущен
     */
    fun getCurrentWorkflowId(): String? {
        return repository.getCurrentWorkflowId()
    }

    /**
     * Проверить, есть ли активная сессия
     * 
     * @return true если сессия существует
     */
    fun hasActiveSession(): Boolean {
        return repository.getCurrentSessionId() != null
    }

    /**
     * Сохранить новый workflow definition (Admin функция)
     * 
     * Обычно вызывается из admin panel, не из мобильного приложения.
     * 
     * @param states Набор состояний workflow
     * @param predefinedContext Предопределенный контекст (app_version, platform, etc.)
     * @return ID сохраненного workflow
     */
    suspend fun saveWorkflow(
        states: StateSet,
        predefinedContext: Map<String, Any> = emptyMap()
    ): Result<String> {
        return repository.saveWorkflow(states, predefinedContext)
    }

    companion object {
        /**
         * Генерировать уникальный session_id
         * 
         * Формат: session_{userId}_{timestamp}_{random}
         * 
         * @param userId ID пользователя (опционально)
         * @return Уникальный session_id
         */
        fun generateSessionId(userId: String? = null): String {
            val userPart = userId ?: "anonymous"
            val timestamp = System.currentTimeMillis()
            val random = UUID.randomUUID().toString().take(8)
            return "session_${userPart}_${timestamp}_${random}"
        }
    }
}
