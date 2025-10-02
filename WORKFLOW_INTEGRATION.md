# LCT EFS Workflow Integration Guide

## Обзор

Этот документ описывает интеграцию **LCT EFS (Execution Flow System)** — workflow execution engine на базе Finite State Machine (FSM) — в LCT Mobile приложение.

## Архитектура интеграции

### Слои приложения

```
┌─────────────────────────────────────────────────────┐
│          Presentation Layer (Compose UI)            │
│  ┌──────────────────┐    ┌──────────────────────┐  │
│  │ Backend-Driven   │    │ Workflow Controller  │  │
│  │   Controller     │◄───┤  (MVI Pattern)       │  │
│  └──────────────────┘    └──────────────────────┘  │
└─────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────┐
│                  Domain Layer                       │
│  ┌──────────────────┐    ┌──────────────────────┐  │
│  │  LoadScreen      │    │ StartWorkflow        │  │
│  │  UseCase         │    │ SendEvent            │  │
│  │                  │    │ UpdateContext        │  │
│  └──────────────────┘    └──────────────────────┘  │
└─────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────┐
│                   Data Layer                        │
│  ┌──────────────────┐    ┌──────────────────────┐  │
│  │ Screen           │    │ Workflow             │  │
│  │ Repository       │    │ Repository           │  │
│  │                  │    │ + SessionManager     │  │
│  └──────────────────┘    └──────────────────────┘  │
└─────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────┐
│                 Network Layer                       │
│  ┌──────────────────┐    ┌──────────────────────┐  │
│  │ RemoteDataSource │    │ WorkflowApiClient    │  │
│  │  (Ktor)          │    │  (Ktor)              │  │
│  └──────────────────┘    └──────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

### Основные компоненты

#### 1. Workflow Models (`shared/model/workflow/`)

- **WorkflowModels.kt**: DTOs для API (StateModel, Transition, Expression, Request/Response)
- **WorkflowState.kt**: Domain модель состояния workflow
- **WorkflowError.kt**: Sealed class для обработки ошибок

#### 2. Network Layer (`shared/network/workflow/`)

- **WorkflowApiClient**: Интерфейс для работы с LCT EFS API
- **KtorWorkflowApiClient**: Реализация с Ktor HTTP Client

#### 3. Session Management (`shared/util/session/`)

- **SessionManager**: Управление `client_session_id` с multiplatform-settings
- Автоматическая генерация UUID
- Персистентное хранение между сессиями

#### 4. Data Layer (`shared/data/workflow/`)

- **WorkflowRepository**: Интерфейс репозитория
- **WorkflowRepositoryImpl**: Реализация с in-memory state management
- Конвертация JSON ↔ Kotlin types

#### 5. Domain Layer (`shared/domain/workflow/`)

- **StartWorkflowUseCase**: Запуск/восстановление workflow
- **SendWorkflowEventUseCase**: Отправка событий (триггеры FSM)
- **UpdateContextUseCase**: Обновление контекста
- **ObserveWorkflowStateUseCase**: Reactive наблюдение за состоянием

#### 6. Presentation Layer (`composeApp/presentation/workflow/`)

- **WorkflowController**: StateFlow-based controller (MVI)
- **WorkflowUiState**: UI состояние
- **WorkflowIntent**: Actions от пользователя

## API Reference

### Base URL

```
http://localhost:8000
```

### Endpoints

#### POST `/workflow/save`

Сохранение workflow definition.

**Request:**
```json
{
  "states": { "states": [...] },
  "predefined_context": { "key": "value" }
}
```

**Response:**
```json
{
  "status": "success",
  "wf_description_id": "...",
  "wf_context_id": "..."
}
```

#### POST `/client/workflow`

Управление сессией и выполнение workflow.

**Request:**
```json
{
  "client_session_id": "uuid",
  "client_workflow_id": "...",  // Обязателен для новой сессии
  "context": { ... },            // Опционально
  "event_name": "submit"         // Опционально
}
```

**Response:**
```json
{
  "session_id": "uuid",
  "context": {
    "__workflow_id": "...",
    "__created_at": "...",
    "current_state": "screen_name",
    ...
  }
}
```

#### GET `/healthcheck`

Проверка доступности API.

## Типы состояний (State Types)

### 1. Technical State

Выполняет Python-выражения, обновляет переменные.

```json
{
  "state_type": "technical",
  "name": "validate_input",
  "expressions": [
    {
      "variable": "is_valid",
      "dependent_variables": ["email"],
      "expression": "'@' in email and len(email) > 5"
    }
  ],
  "transitions": [
    { "target_state": "next_state", "condition": "is_valid" }
  ]
}
```

### 2. Integration State

Вызов внешних API.

```json
{
  "state_type": "integration",
  "name": "fetch_user",
  "integration_config": {
    "variable": "user_data",
    "url": "https://api.example.com/user",
    "params": { "user_id": "$user_id" },
    "method": "get"
  },
  "transitions": [
    { "target_state": "profile_screen" }
  ]
}
```

### 3. Screen State

UI состояние, управляется событиями.

```json
{
  "state_type": "screen",
  "name": "login_screen",
  "transitions": [
    { "target_state": "validate", "event_name": "submit" }
  ]
}
```

### 4. Service State

Системные состояния: `__init__`, `__error__`.

## Использование в коде

### 1. Инициализация Dependencies

```kotlin
// В createAppDependencies()
val dependencies = createAppDependencies(
    httpClient = httpClient,
    workflowBaseUrl = "http://localhost:8000",
    coroutineScope = viewModelScope
)

val workflowController = dependencies.workflowController
```

### 2. Запуск Workflow в UI

```kotlin
@Composable
fun WorkflowScreen(controller: WorkflowController) {
    val uiState by controller.uiState.collectAsState()

    LaunchedEffect(Unit) {
        // Запуск workflow при открытии экрана
        controller.onIntent(
            WorkflowIntent.StartWorkflow(workflowId = "your_workflow_id")
        )
    }

    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.error != null -> ErrorMessage(uiState.error)
        else -> {
            when (uiState.currentScreen) {
                "login_screen" -> LoginScreen(controller)
                "dashboard_screen" -> DashboardScreen(controller)
                else -> EmptyScreen()
            }
        }
    }
}
```

### 3. Обработка событий

```kotlin
@Composable
fun LoginScreen(controller: WorkflowController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )
        
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )

        Button(onClick = {
            controller.onIntent(
                WorkflowIntent.SendEvent(
                    eventName = "submit",
                    data = mapOf(
                        "username" to username,
                        "password" to password
                    )
                )
            )
        }) {
            Text("Login")
        }
    }
}
```

### 4. Обновление контекста

```kotlin
// Обновить только определенные переменные
controller.onIntent(
    WorkflowIntent.UpdateContext(
        updates = mapOf(
            "profile_photo" to "base64_image_data"
        )
    )
)
```

### 5. Сброс workflow

```kotlin
Button(onClick = {
    controller.onIntent(WorkflowIntent.ResetWorkflow)
}) {
    Text("Logout")
}
```

## Жизненный цикл Workflow

### 1. Создание workflow definition

```kotlin
val repository = dependencies.workflowRepository

val states = StateSet(
    states = listOf(
        StateModel(
            stateType = StateType.SERVICE,
            name = "__init__",
            transitions = listOf(
                Transition(targetState = "welcome_screen")
            ),
            initialState = true
        ),
        StateModel(
            stateType = StateType.SCREEN,
            name = "welcome_screen",
            transitions = listOf(
                Transition(
                    targetState = "process",
                    eventName = "start"
                )
            )
        )
        // ... другие состояния
    )
)

val workflowId = repository.saveWorkflow(
    states = states,
    predefinedContext = mapOf("counter" to 0)
).getOrThrow()
```

### 2. Инициализация сессии

```kotlin
// Автоматически при первом запуске
controller.onIntent(
    WorkflowIntent.StartWorkflow(workflowId = workflowId)
)
```

### 3. Навигация через события

```kotlin
// Screen state → event → Technical/Integration → Screen state
controller.onIntent(
    WorkflowIntent.SendEvent(
        eventName = "button_click",
        data = mapOf("selected_id" to 123)
    )
)
```

### 4. Обновление без навигации

```kotlin
// Обновить переменные контекста без смены состояния
controller.onIntent(
    WorkflowIntent.UpdateContext(
        updates = mapOf("temp_data" to "value")
    )
)
```

## Особенности реализации

### Служебные переменные

Переменные с префиксом `__` не отображаются в UI:

```kotlin
val visibleContext = WorkflowState.filterUserVisibleContext(context)
// Исключает: __workflow_id, __created_at, __error__, etc.
```

### Session Management

- `client_session_id` автоматически генерируется и сохраняется локально
- Восстановление сессии при перезапуске приложения
- Очистка сессии через `SessionManager.clearSession()`

### Error Handling

```kotlin
sealed class WorkflowError : Exception() {
    data class NetworkError(override val message: String)
    data class ServerError(val code: Int, override val message: String)
    object SessionExpired
    data class ValidationError(override val message: String)
}
```

Ошибки автоматически обрабатываются в `WorkflowController` и отображаются в `uiState.error`.

### Retry Logic

Используйте `RetryUtils` для автоматических повторов при сетевых ошибках:

```kotlin
import ru.skittens.shared.util.RetryUtils

val result = RetryUtils.retryIO(
    times = 3,
    initialDelay = 1000,
    factor = 2.0
) {
    apiClient.executeWorkflow(request)
}
```

## Примеры Workflow Definitions

См. файлы в `examples/`:

- **workflow-simple.json**: Простой пример с technical state
- **workflow-login-flow.json**: Полный flow аутентификации
- **workflow-form-flow.json**: Валидация и отправка формы

### Пример: Login Flow

```json
{
  "states": {
    "states": [
      {
        "state_type": "service",
        "name": "__init__",
        "transitions": [{"target_state": "login_screen"}],
        "initial_state": true
      },
      {
        "state_type": "screen",
        "name": "login_screen",
        "transitions": [
          {"target_state": "validate_credentials", "event_name": "submit"}
        ]
      },
      {
        "state_type": "technical",
        "name": "validate_credentials",
        "expressions": [
          {
            "variable": "is_valid",
            "dependent_variables": ["username", "password"],
            "expression": "len(username) > 0 and len(password) >= 6"
          }
        ],
        "transitions": [
          {"target_state": "authenticate_user", "condition": "is_valid"},
          {"target_state": "login_screen", "condition": "not is_valid"}
        ]
      },
      {
        "state_type": "integration",
        "name": "authenticate_user",
        "integration_config": {
          "variable": "auth_response",
          "url": "https://api.example.com/auth/login",
          "params": {"username": "$username", "password": "$password"},
          "method": "post"
        },
        "transitions": [
          {"target_state": "dashboard_screen", "condition": "auth_success"}
        ]
      }
    ]
  },
  "predefined_context": {
    "username": "",
    "password": ""
  }
}
```

## Тестирование

### Unit Tests для Repository

```kotlin
class WorkflowRepositoryTest {
    @Test
    fun `should start workflow successfully`() = runTest {
        val mockApiClient = mockk<WorkflowApiClient>()
        val sessionManager = SessionManager(Settings())
        val repository = WorkflowRepositoryImpl(mockApiClient, sessionManager)

        coEvery { 
            mockApiClient.executeWorkflow(any()) 
        } returns Result.success(mockResponse)

        val result = repository.startWorkflow("workflow_id")

        assertTrue(result.isSuccess)
        assertEquals("login_screen", result.getOrNull()?.currentScreen)
    }
}
```

### Integration Tests

```kotlin
@Test
fun `end-to-end workflow execution`() = runTest {
    val httpClient = HttpClient(MockEngine) { /* ... */ }
    val dependencies = createAppDependencies(httpClient)
    
    // Start workflow
    dependencies.workflowController.onIntent(
        WorkflowIntent.StartWorkflow("test_workflow_id")
    )
    
    // Wait for state update
    dependencies.workflowController.uiState
        .filter { !it.isLoading }
        .first()
    
    // Send event
    dependencies.workflowController.onIntent(
        WorkflowIntent.SendEvent("submit", mapOf("data" to "value"))
    )
}
```

## Best Practices

### 1. Всегда проверяйте сессию

```kotlin
LaunchedEffect(Unit) {
    if (sessionManager.hasActiveSession()) {
        controller.onIntent(WorkflowIntent.StartWorkflow())
    } else {
        controller.onIntent(
            WorkflowIntent.StartWorkflow(workflowId = "new_workflow_id")
        )
    }
}
```

### 2. Обрабатывайте ошибки в UI

```kotlin
uiState.error?.let { error ->
    Snackbar(
        action = {
            TextButton(onClick = { 
                controller.onIntent(WorkflowIntent.ClearError) 
            }) {
                Text("Dismiss")
            }
        }
    ) {
        Text(error)
    }
}
```

### 3. Используйте LaunchedEffect для side effects

```kotlin
LaunchedEffect(uiState.currentScreen) {
    when (uiState.currentScreen) {
        "dashboard_screen" -> {
            // Загрузка дополнительных данных
            controller.onIntent(
                WorkflowIntent.UpdateContext(
                    updates = loadDashboardData()
                )
            )
        }
    }
}
```

### 4. Логируйте переходы состояний

```kotlin
LaunchedEffect(Unit) {
    controller.uiState.collect { state ->
        Log.d("Workflow", "Current screen: ${state.currentScreen}")
        Log.d("Workflow", "Context: ${state.visibleContext}")
    }
}
```

## Troubleshooting

### Проблема: Session expired

**Решение**: Очистите сессию и создайте новую

```kotlin
controller.onIntent(WorkflowIntent.ResetWorkflow)
dependencies.workflowRepository.clearSession()
controller.onIntent(
    WorkflowIntent.StartWorkflow(workflowId = "workflow_id")
)
```

### Проблема: Workflow не переходит в следующее состояние

**Причины**:
1. Условие в `transition.condition` не выполнено
2. Неправильное `event_name`
3. Ошибка в Python expression

**Решение**: Проверьте контекст и логику переходов:

```kotlin
val currentContext = controller.uiState.value.context
println("Current context: $currentContext")
```

### Проблема: Context не обновляется

**Решение**: Используйте `UpdateContext` для явного обновления:

```kotlin
controller.onIntent(
    WorkflowIntent.UpdateContext(
        updates = mapOf("key" to "new_value")
    )
)
```

## Дополнительные ресурсы

- [Ktor Client Documentation](https://ktor.io/docs/client.html)
- [kotlinx.serialization Guide](https://github.com/Kotlin/kotlinx.serialization)
- [Multiplatform Settings](https://github.com/russhwolf/multiplatform-settings)
- [Kotlin Flows](https://kotlinlang.org/docs/flow.html)

---

**Последнее обновление:** 1 октября 2025 г.
