# Workflow Engine Usage Guide - Kotlin Multiplatform

Полное руководство по использованию LCT EFS Workflow Engine в KMP приложении.

## 📚 Содержание

- [Быстрый старт](#быстрый-старт)
- [Архитектура интеграции](#архитектура-интеграции)
- [Основные компоненты](#основные-компоненты)
- [Примеры использования](#примеры-использования)
- [API Reference](#api-reference)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

---

## Быстрый старт

### 1. Настройка зависимостей

Все необходимые компоненты уже включены в проект:

```kotlin
// shared/build.gradle.kts
dependencies {
    // Workflow зависимости
    implementation("io.ktor:ktor-client-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("com.russhwolf:multiplatform-settings")
}
```

### 2. Инициализация WorkflowManager

В вашем DI модуле (`AppModule.kt`):

```kotlin
import ru.skittens.shared.domain.workflow.WorkflowManager
import ru.skittens.shared.data.workflow.WorkflowRepositoryImpl
import ru.skittens.shared.network.workflow.KtorWorkflowApiClient

fun createWorkflowManager(httpClient: HttpClient): WorkflowManager {
    val apiClient = KtorWorkflowApiClient(
        httpClient = httpClient,
        baseUrl = "http://localhost:8000" // Замените на ваш API URL
    )
    
    val sessionManager = SessionManager(settings)
    
    val repository = WorkflowRepositoryImpl(
        apiClient = apiClient,
        sessionManager = sessionManager
    )
    
    return WorkflowManager(repository)
}
```

### 3. Запуск первого workflow

```kotlin
// В ViewModel или Controller
suspend fun startLoginFlow() {
    workflowManager.startWorkflow(
        workflowId = "login_workflow_v1",
        initialContext = mapOf(
            "device_type" to "android",
            "app_version" to BuildConfig.VERSION_NAME
        )
    )
        .onSuccess { state ->
            // state.screen содержит данные для отображения
            renderScreen(state.screen)
        }
        .onFailure { error ->
            // Обработка ошибки
            showError(error.message)
        }
}

// Отправка события (например, при нажатии кнопки)
suspend fun submitForm(email: String, password: String) {
    workflowManager.sendEvent(
        eventName = "submit",
        additionalContext = mapOf(
            "email" to email,
            "password" to password
        )
    )
        .onSuccess { state ->
            // Переход к следующему экрану
            renderScreen(state.screen)
        }
        .onFailure { error ->
            showError(error.message)
        }
}
```

---

## Архитектура интеграции

```
┌─────────────────────────────────────────────────┐
│           Presentation Layer (UI)               │
│  ┌───────────────────┐  ┌─────────────────┐   │
│  │ WorkflowScreen    │  │ ViewModel       │   │
│  │ (Compose UI)      │←─│ (State)         │   │
│  └───────────────────┘  └────────┬────────┘   │
│                                   │            │
└───────────────────────────────────┼────────────┘
                                    │
┌───────────────────────────────────┼────────────┐
│           Domain Layer            │            │
│  ┌────────────────────────────────┴─────────┐ │
│  │       WorkflowManager (Facade)           │ │
│  └────────────────┬─────────────────────────┘ │
│                   │                            │
│  ┌────────────────┴─────────────────────────┐ │
│  │      WorkflowRepository (Interface)      │ │
│  └──────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
                    │
┌───────────────────┼─────────────────────────────┐
│           Data Layer              │             │
│  ┌────────────────┴──────────────────────────┐ │
│  │   WorkflowRepositoryImpl (In-memory)     │ │
│  └────────────────┬──────────────────────────┘ │
│                   │                             │
│  ┌────────────────┴──────────────────────────┐ │
│  │   WorkflowApiClient (Ktor HTTP)          │ │
│  └────────────────┬──────────────────────────┘ │
│                   │                             │
│  ┌────────────────┴──────────────────────────┐ │
│  │   SessionManager (Persistent Storage)    │ │
│  └───────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
                    │
                    ▼
        ┌───────────────────┐
        │  Backend API      │
        │  (LCT EFS)        │
        └───────────────────┘
```

---

## Основные компоненты

### WorkflowManager

Высокоуровневый фасад для работы с workflow.

```kotlin
class WorkflowManager(repository: WorkflowRepository)
```

**Основные методы:**

| Метод | Описание |
|-------|----------|
| `startWorkflow(workflowId, initialContext)` | Начать новый workflow |
| `sendEvent(eventName, additionalContext)` | Отправить событие |
| `updateContext(updates)` | Обновить контекст без события |
| `restoreSession(sessionId?)` | Восстановить сессию |
| `endSession()` | Завершить сессию |
| `hasActiveSession()` | Проверить наличие активной сессии |

### WorkflowState

Domain модель состояния workflow.

```kotlin
data class WorkflowState(
    val sessionId: String,
    val workflowId: String,
    val currentState: String,           // Имя текущего состояния
    val stateType: StateType,           // SCREEN, TECHNICAL, INTEGRATION, SERVICE
    val context: Map<String, Any>,      // Полный контекст workflow
    val screen: ScreenData?,            // Данные экрана (только для SCREEN)
    val createdAt: String?,
    val isError: Boolean
)
```

### ScreenData

Данные для отображения экрана (для state_type = SCREEN).

```kotlin
data class ScreenData(
    val title: String?,
    val description: String?,
    val fields: List<FieldData>?,       // Поля ввода
    val buttons: List<ButtonData>?,     // Кнопки
    val components: List<ComponentData>? // Другие компоненты
)
```

### WorkflowScreenRenderer

Composable для динамического рендеринга экранов из `ScreenData`.

```kotlin
@Composable
fun WorkflowScreenRenderer(
    screenData: ScreenData,
    context: Map<String, Any> = emptyMap(),
    onEvent: (eventName: String, data: Map<String, Any>) -> Unit,
    modifier: Modifier = Modifier
)
```

---

## Примеры использования

### Пример 1: Простая авторизация

**Workflow на сервере:**

```json
{
  "states": [
    {
      "state_type": "screen",
      "name": "LoginScreen",
      "screen": {
        "title": "Вход в систему",
        "fields": [
          {
            "id": "email",
            "type": "email",
            "label": "Email",
            "required": true
          },
          {
            "id": "password",
            "type": "password",
            "label": "Пароль",
            "required": true
          }
        ],
        "buttons": [
          {
            "id": "submit",
            "label": "Войти",
            "event": "submit",
            "style": "primary"
          }
        ]
      },
      "expressions": [{"event_name": "submit"}],
      "transitions": [
        {
          "case": "submit",
          "state_id": "ValidateCredentials"
        }
      ],
      "initial_state": true
    },
    {
      "state_type": "technical",
      "name": "ValidateCredentials",
      "expressions": [
        {
          "variable": "is_valid",
          "dependent_variables": ["email", "password"],
          "expression": "len(email) > 0 and len(password) >= 8"
        }
      ],
      "transitions": [
        {
          "variable": "is_valid",
          "case": "True",
          "state_id": "LoginSuccess"
        },
        {
          "variable": "is_valid",
          "case": "False",
          "state_id": "LoginScreen"
        }
      ]
    },
    {
      "state_type": "service",
      "name": "LoginSuccess",
      "final_state": true
    }
  ]
}
```

**Kotlin код:**

```kotlin
// ViewModel
class LoginViewModel(
    private val workflowManager: WorkflowManager,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun startLogin() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true) }

            workflowManager.startWorkflow("login_workflow_v1")
                .onSuccess { state ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            screen = state.screen
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    fun submitCredentials(email: String, password: String) {
        scope.launch {
            workflowManager.sendEvent(
                eventName = "submit",
                additionalContext = mapOf(
                    "email" to email,
                    "password" to password
                )
            )
                .onSuccess { state ->
                    if (state.currentState == "LoginSuccess") {
                        // Успешная авторизация
                        navigateToDashboard()
                    } else {
                        // Показываем следующий экран
                        _uiState.update { it.copy(screen = state.screen) }
                    }
                }
        }
    }
}

// Composable Screen
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startLogin()
    }

    uiState.screen?.let { screen ->
        WorkflowScreenRenderer(
            screenData = screen,
            onEvent = { eventName, data ->
                if (eventName == "submit") {
                    val email = data["email"]?.toString() ?: ""
                    val password = data["password"]?.toString() ?: ""
                    viewModel.submitCredentials(email, password)
                }
            }
        )
    }
}
```

### Пример 2: Многошаговая форма заявки

```kotlin
class LoanApplicationViewModel(
    private val workflowManager: WorkflowManager,
    private val scope: CoroutineScope
) {
    fun startApplication() {
        scope.launch {
            workflowManager.startWorkflow(
                workflowId = "loan_application_v2",
                initialContext = mapOf(
                    "user_id" to getUserId(),
                    "started_at" to System.currentTimeMillis()
                )
            )
        }
    }

    fun submitStep(eventName: String, data: Map<String, Any>) {
        scope.launch {
            workflowManager.sendEvent(eventName, data)
                .onSuccess { state ->
                    when (state.stateType) {
                        StateType.SCREEN -> {
                            // Показываем следующий шаг
                            updateScreen(state.screen)
                        }
                        StateType.SERVICE -> {
                            if (state.currentState == "ApplicationApproved") {
                                showSuccessScreen()
                            } else if (state.currentState == "ApplicationRejected") {
                                showRejectionScreen()
                            }
                        }
                        else -> {
                            // Technical/Integration states обрабатываются автоматически
                        }
                    }
                }
        }
    }
}
```

### Пример 3: Восстановление сессии

```kotlin
// В Application/MainActivity onCreate
class MyApplication : Application() {
    lateinit var workflowManager: WorkflowManager

    override fun onCreate() {
        super.onCreate()
        
        // Инициализация
        workflowManager = createWorkflowManager(httpClient)
        
        // Восстановление активных сессий
        lifecycleScope.launch {
            if (workflowManager.hasActiveSession()) {
                workflowManager.restoreSession()
                    ?.onSuccess { state ->
                        // Продолжаем с текущего состояния
                        resumeWorkflow(state)
                    }
                    ?.onFailure {
                        // Сессия истекла - очищаем
                        workflowManager.endSession()
                    }
            }
        }
    }
}
```

---

## API Reference

### WorkflowManager API

#### startWorkflow

```kotlin
suspend fun startWorkflow(
    workflowId: String,
    initialContext: Map<String, Any> = emptyMap()
): Result<WorkflowState>
```

**Параметры:**
- `workflowId` - ID workflow (получен от backend)
- `initialContext` - Начальные данные (user_id, device_type, etc.)

**Возвращает:**
- `Result<WorkflowState>` - Первое состояние workflow

**Пример:**
```kotlin
workflowManager.startWorkflow(
    workflowId = "onboarding_v1",
    initialContext = mapOf(
        "user_id" to "12345",
        "device_type" to "android",
        "locale" to "ru"
    )
)
```

#### sendEvent

```kotlin
suspend fun sendEvent(
    eventName: String,
    additionalContext: Map<String, Any> = emptyMap()
): Result<WorkflowState>
```

**Параметры:**
- `eventName` - Имя события (определено в workflow)
- `additionalContext` - Данные для передачи (form data, user input)

**Возвращает:**
- `Result<WorkflowState>` - Следующее состояние

**Пример:**
```kotlin
workflowManager.sendEvent(
    eventName = "next",
    additionalContext = mapOf(
        "age" to 25,
        "city" to "Moscow"
    )
)
```

#### updateContext

```kotlin
suspend fun updateContext(
    updates: Map<String, Any>
): Result<WorkflowState>
```

Обновляет контекст без отправки события.

#### restoreSession

```kotlin
suspend fun restoreSession(
    sessionId: String? = null
): Result<WorkflowState>?
```

Восстанавливает сессию после закрытия приложения.

#### endSession

```kotlin
fun endSession()
```

Завершает текущую сессию (очищает local storage).

---

## Best Practices

### 1. Управление сессиями

**DO:**
```kotlin
// Всегда проверяйте активную сессию при старте
if (workflowManager.hasActiveSession()) {
    workflowManager.restoreSession()
} else {
    workflowManager.startWorkflow("my_workflow")
}

// Очищайте сессию при logout
fun logout() {
    workflowManager.endSession()
    clearUserData()
}
```

**DON'T:**
```kotlin
// Не создавайте несколько параллельных сессий одного workflow
workflowManager.startWorkflow("workflow1")
workflowManager.startWorkflow("workflow1") // ❌ Конфликт сессий
```

### 2. Обработка ошибок

```kotlin
workflowManager.sendEvent("submit", data)
    .onSuccess { state ->
        // Success handling
    }
    .onFailure { error ->
        when (error) {
            is WorkflowError.NetworkError -> {
                // Показать retry кнопку
                showRetryDialog()
            }
            is WorkflowError.SessionExpired -> {
                // Перезапустить workflow
                workflowManager.startWorkflow(workflowId)
            }
            is WorkflowError.ValidationError -> {
                // Показать ошибку валидации
                showValidationError(error.message)
            }
            else -> {
                // Общая ошибка
                showGenericError()
            }
        }
    }
```

### 3. Типы состояний

```kotlin
when (workflowState.stateType) {
    StateType.SCREEN -> {
        // Отображаем экран пользователю
        renderScreen(workflowState.screen)
    }
    
    StateType.TECHNICAL -> {
        // Не должно дойти до UI
        // Technical states обрабатываются автоматически
        showLoading()
    }
    
    StateType.INTEGRATION -> {
        // API calls обрабатываются на сервере
        showLoading()
    }
    
    StateType.SERVICE -> {
        // Служебные состояния (__init__, __error__, final states)
        if (workflowState.isError) {
            showError()
        } else {
            // Проверяем финальное ли состояние
            checkFinalState(workflowState.currentState)
        }
    }
}
```

### 4. Не храните чувствительные данные

```kotlin
// ❌ DON'T
workflowManager.sendEvent("submit", mapOf(
    "credit_card_number" to "1234-5678-9012-3456"
))

// ✅ DO - отправляйте на сервер, но не сохраняйте в контексте
// Сервер обработает данные и не вернет их в context
workflowManager.sendEvent("submit", mapOf(
    "payment_token" to getPaymentToken()
))
```

### 5. Используйте Retry для сетевых запросов

```kotlin
suspend fun startWorkflowWithRetry(workflowId: String) {
    RetryUtils.retryNetworkErrors(maxRetries = 3) {
        workflowManager.startWorkflow(workflowId).getOrThrow()
    }
}
```

---

## Troubleshooting

### Проблема: "Session expired"

**Причина:** TTL сессии истек (по умолчанию 24 часа).

**Решение:**
```kotlin
workflowManager.restoreSession()
    ?.onFailure {
        // Начать новый workflow
        workflowManager.endSession()
        workflowManager.startWorkflow(workflowId)
    }
```

### Проблема: "Workflow не переходит к следующему состоянию"

**Причина:** Неверное имя события или condition не выполнился.

**Решение:**
1. Проверьте `expressions` в workflow definition
2. Убедитесь что `event_name` соответствует
3. Проверьте контекст - все ли переменные присутствуют

```kotlin
// Логируйте состояние для отладки
workflowManager.sendEvent("submit", data)
    .onSuccess { state ->
        println("Current state: ${state.currentState}")
        println("Context: ${state.context}")
    }
```

### Проблема: "Network timeout"

**Решение:**
```kotlin
// Настройте timeout в HTTP client
val httpClient = HttpClient(engine) {
    install(HttpTimeout) {
        requestTimeoutMillis = 30000L // 30 секунд
        connectTimeoutMillis = 10000L
        socketTimeoutMillis = 30000L
    }
}
```

### Проблема: "Screen не отображается"

**Причина:** `state_type` не `SCREEN` или `screen` = null.

**Решение:**
```kotlin
when (state.stateType) {
    StateType.SCREEN -> {
        state.screen?.let { screen ->
            WorkflowScreenRenderer(screen, onEvent = { ... })
        } ?: run {
            println("Screen is null!")
        }
    }
    else -> {
        println("Not a screen state: ${state.stateType}")
    }
}
```

---

## Полезные ссылки

- [Backend API Documentation](../WORKFLOW_INTEGRATION.md)
- [Workflow Examples](../examples/)
- [Project Architecture](.github/copilot-instructions.md)

---

**Последнее обновление:** 2 октября 2025 г.
