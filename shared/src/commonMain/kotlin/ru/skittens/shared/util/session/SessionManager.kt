package ru.skittens.shared.util.session

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlin.random.Random

/**
 * Менеджер для управления client_session_id workflow
 * Использует multiplatform-settings для кросс-платформенного хранения
 */
class SessionManager(
    private val settings: Settings
) {
    /**
     * Получить текущий session_id или создать новый
     */
    fun getSessionId(): String {
        val existingSessionId = settings.getStringOrNull(SESSION_ID_KEY)
        return if (existingSessionId != null) {
            existingSessionId
        } else {
            createNewSession()
        }
    }

    /**
     * Создать новую сессию с UUID
     */
    private fun createNewSession(): String {
        val newSessionId = generateUUID()
        settings.putString(SESSION_ID_KEY, newSessionId)
        return newSessionId
    }

    /**
     * Обновить session_id (например, после получения от сервера)
     */
    fun updateSessionId(sessionId: String) {
        settings.putString(SESSION_ID_KEY, sessionId)
    }

    /**
     * Очистить текущую сессию
     */
    fun clearSession() {
        settings.remove(SESSION_ID_KEY)
    }

    /**
     * Проверить, существует ли активная сессия
     */
    fun hasActiveSession(): Boolean {
        return settings.getStringOrNull(SESSION_ID_KEY) != null
    }

    /**
     * Сохранить workflow_id текущей сессии
     */
    fun saveWorkflowId(workflowId: String) {
        settings.putString(WORKFLOW_ID_KEY, workflowId)
    }

    /**
     * Получить workflow_id текущей сессии
     */
    fun getWorkflowId(): String? {
        return settings.getStringOrNull(WORKFLOW_ID_KEY)
    }

    /**
     * Генерация UUID-подобного идентификатора
     * (Простая имплементация для multiplatform)
     */
    private fun generateUUID(): String {
        val chars = "0123456789abcdef"
        return buildString {
            repeat(8) { append(chars.random()) }
            append("-")
            repeat(4) { append(chars.random()) }
            append("-")
            append("4") // UUID v4
            repeat(3) { append(chars.random()) }
            append("-")
            append(chars[Random.nextInt(8, 12)]) // Variant bits
            repeat(3) { append(chars.random()) }
            append("-")
            repeat(12) { append(chars.random()) }
        }
    }

    companion object {
        private const val SESSION_ID_KEY = "workflow_session_id"
        private const val WORKFLOW_ID_KEY = "workflow_workflow_id"
    }
}
