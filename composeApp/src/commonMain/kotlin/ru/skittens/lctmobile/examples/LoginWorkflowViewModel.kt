package ru.skittens.lctmobile.examples

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.skittens.shared.domain.workflow.WorkflowManager
import ru.skittens.shared.model.workflow.ScreenData
import ru.skittens.shared.model.workflow.StateType
import ru.skittens.shared.model.workflow.WorkflowError

/**
 * Пример ViewModel для workflow-driven Login Flow
 * 
 * Демонстрирует использование WorkflowManager для реализации
 * многошагового процесса авторизации без hardcode экранов в приложении.
 * 
 * Workflow на сервере определяет:
 * - Экран ввода логина/пароля
 * - Валидацию credentials
 * - Экран двухфакторной аутентификации (если нужно)
 * - Переход на Dashboard при успехе
 */
class LoginWorkflowViewModel(
    private val workflowManager: WorkflowManager,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Запустить workflow авторизации
     * 
     * @param workflowId ID login workflow (получен от backend team)
     */
    fun startLoginFlow(workflowId: String = "login_workflow_v1") {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            workflowManager.startWorkflow(
                workflowId = workflowId,
                initialContext = mapOf(
                    "device_type" to "android", // или "ios"
                    "app_version" to "1.0.0",
                    "timestamp" to System.currentTimeMillis()
                )
            )
                .onSuccess { workflowState ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentScreen = workflowState.screen,
                            currentState = workflowState.currentState,
                            stateType = workflowState.stateType
                        )
                    }
                }
                .onFailure { error ->
                    handleError(error)
                }
        }
    }

    /**
     * Отправить credentials (email + password)
     * 
     * Вызывается при нажатии кнопки "Войти" на первом экране.
     */
    fun submitCredentials(email: String, password: String) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            workflowManager.sendEvent(
                eventName = "submit",
                additionalContext = mapOf(
                    "email" to email,
                    "password" to password
                )
            )
                .onSuccess { workflowState ->
                    handleWorkflowStateUpdate(workflowState)
                }
                .onFailure { error ->
                    handleError(error)
                }
        }
    }

    /**
     * Отправить 2FA код
     * 
     * Вызывается если сервер запросил двухфакторную аутентификацию.
     */
    fun submitTwoFactorCode(code: String) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            workflowManager.sendEvent(
                eventName = "submit_2fa",
                additionalContext = mapOf(
                    "two_factor_code" to code
                )
            )
                .onSuccess { workflowState ->
                    handleWorkflowStateUpdate(workflowState)
                }
                .onFailure { error ->
                    handleError(error)
                }
        }
    }

    /**
     * Обработать "Забыли пароль?"
     */
    fun forgotPassword() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            workflowManager.sendEvent(
                eventName = "forgot_password",
                additionalContext = emptyMap()
            )
                .onSuccess { workflowState ->
                    handleWorkflowStateUpdate(workflowState)
                }
                .onFailure { error ->
                    handleError(error)
                }
        }
    }

    /**
     * Восстановить сессию при запуске приложения
     */
    fun restoreSessionIfExists() {
        if (workflowManager.hasActiveSession()) {
            scope.launch {
                _uiState.update { it.copy(isLoading = true) }

                workflowManager.restoreSession()
                    ?.onSuccess { workflowState ->
                        handleWorkflowStateUpdate(workflowState)
                    }
                    ?.onFailure { error ->
                        handleError(error)
                        // Если восстановление не удалось - начинаем заново
                        startLoginFlow()
                    }
            }
        } else {
            // Нет активной сессии - начинаем новый flow
            startLoginFlow()
        }
    }

    /**
     * Завершить workflow (при logout)
     */
    fun logout() {
        workflowManager.endSession()
        _uiState.value = LoginUiState() // Reset state
    }

    /**
     * Обработка обновления workflow state
     */
    private fun handleWorkflowStateUpdate(workflowState: ru.skittens.shared.model.workflow.WorkflowState) {
        when (workflowState.stateType) {
            StateType.SCREEN -> {
                // Отображаем экран пользователю
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentScreen = workflowState.screen,
                        currentState = workflowState.currentState,
                        stateType = workflowState.stateType
                    )
                }
            }

            StateType.SERVICE -> {
                // Служебное состояние - проверяем финальное ли
                if (workflowState.currentState == "LoginSuccess") {
                    // Авторизация успешна!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            currentScreen = null
                        )
                    }
                    // TODO: Navigate to main screen
                } else if (workflowState.isError) {
                    handleError(WorkflowError.ValidationError("Login failed"))
                }
            }

            StateType.TECHNICAL, StateType.INTEGRATION -> {
                // Technical и Integration states обрабатываются автоматически
                // Не должны дойти до UI, но на всякий случай показываем loading
                _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    /**
     * Обработка ошибок
     */
    private fun handleError(error: Throwable) {
        val errorMessage = when (error) {
            is WorkflowError.NetworkError ->
                "Проблема с подключением. Проверьте интернет."
            is WorkflowError.ServerError ->
                "Ошибка сервера (${error.code}). Попробуйте позже."
            is WorkflowError.SessionExpired ->
                "Сессия истекла. Войдите заново."
            is WorkflowError.ValidationError ->
                "Неверный логин или пароль."
            else ->
                "Произошла ошибка: ${error.message}"
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                error = errorMessage
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI State для Login Flow
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val currentScreen: ScreenData? = null,
    val currentState: String? = null,
    val stateType: StateType? = null,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)
