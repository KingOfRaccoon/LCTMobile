# Быстрый старт: LCT EFS Workflow

## 1. Запуск LCT EFS Backend

```bash
# Убедитесь, что LCT EFS сервер запущен на localhost:8000
# Проверка доступности:
curl http://localhost:8000/healthcheck
# Ожидаемый ответ: {"status":"ok"}
```

## 2. Базовое использование в коде

### Инициализация WorkflowController

```kotlin
@Composable
fun App() {
    val httpClient = remember { createHttpClient() }
    val dependencies = remember {
        createAppDependencies(
            httpClient = httpClient,
            workflowBaseUrl = "http://localhost:8000"
        )
    }
    
    WorkflowScreen(controller = dependencies.workflowController)
}
```

### Экран с workflow

```kotlin
@Composable
fun WorkflowScreen(controller: WorkflowController) {
    val state by controller.uiState.collectAsState()
    
    // Запуск workflow при первой загрузке
    LaunchedEffect(Unit) {
        controller.onIntent(
            WorkflowIntent.StartWorkflow(
                workflowId = "YOUR_WORKFLOW_ID" // ID из POST /workflow/save
            )
        )
    }
    
    when {
        state.isLoading -> CircularProgressIndicator()
        state.error != null -> ErrorMessage(state.error!!)
        else -> CurrentScreen(state.currentScreen, controller)
    }
}
```

### Отображение текущего экрана

```kotlin
@Composable
fun CurrentScreen(screenName: String?, controller: WorkflowController) {
    when (screenName) {
        "login_screen" -> LoginScreen(controller)
        "dashboard_screen" -> DashboardScreen(controller)
        "profile_screen" -> ProfileScreen(controller)
        null -> Text("Initializing...")
        else -> Text("Unknown screen: $screenName")
    }
}
```

### Пример экрана с формой

```kotlin
@Composable
fun LoginScreen(controller: WorkflowController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )
        
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
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

### Отображение контекста

```kotlin
@Composable
fun DashboardScreen(controller: WorkflowController) {
    val state by controller.uiState.collectAsState()
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome!", style = MaterialTheme.typography.h4)
        
        // Отображение user-visible данных из контекста
        state.visibleContext.forEach { (key, value) ->
            Text("$key: $value")
        }
        
        Button(onClick = {
            controller.onIntent(
                WorkflowIntent.SendEvent(eventName = "view_profile")
            )
        }) {
            Text("View Profile")
        }
        
        Button(onClick = {
            controller.onIntent(
                WorkflowIntent.SendEvent(eventName = "logout")
            )
        }) {
            Text("Logout")
        }
    }
}
```

## 3. Создание workflow definition

### Простой login flow

```kotlin
val loginWorkflow = StateSet(
    states = listOf(
        StateModel(
            stateType = StateType.SERVICE,
            name = "__init__",
            transitions = listOf(
                Transition(targetState = "login_screen")
            ),
            initialState = true
        ),
        StateModel(
            stateType = StateType.SCREEN,
            name = "login_screen",
            transitions = listOf(
                Transition(
                    targetState = "dashboard_screen",
                    eventName = "submit"
                )
            )
        ),
        StateModel(
            stateType = StateType.SCREEN,
            name = "dashboard_screen",
            transitions = listOf(
                Transition(
                    targetState = "login_screen",
                    eventName = "logout"
                )
            )
        )
    )
)

// Сохранение workflow
val result = workflowRepository.saveWorkflow(
    states = loginWorkflow,
    predefinedContext = mapOf(
        "app_name" to "LCT Mobile",
        "version" to "1.0.0"
    )
)

result.onSuccess { workflowId ->
    println("Workflow saved with ID: $workflowId")
}
```

## 4. Lifecycle workflow сессии

```kotlin
// 1. Создание/получение сессии при запуске
controller.onIntent(WorkflowIntent.StartWorkflow("WORKFLOW_ID"))

// 2. Отправка событий в процессе работы
controller.onIntent(WorkflowIntent.SendEvent("button_click", mapOf("value" to 123)))

// 3. Обновление контекста
controller.onIntent(WorkflowIntent.UpdateContext(mapOf("theme" to "dark")))

// 4. Очистка ошибок
controller.onIntent(WorkflowIntent.ClearError)

// 5. Сброс workflow (logout)
controller.onIntent(WorkflowIntent.ResetWorkflow)
```

## 5. Обработка ошибок

```kotlin
@Composable
fun ErrorMessage(error: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
        Spacer(modifier = Modifier.height(8.dp))
        Text(error, style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            controller.onIntent(WorkflowIntent.ClearError)
        }) {
            Text("Dismiss")
        }
    }
}
```

## 6. Конфигурация для production

```kotlin
// Android MainActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val httpClient = createHttpClient()
        val dependencies = createAppDependencies(
            httpClient = httpClient,
            workflowBaseUrl = BuildConfig.WORKFLOW_API_URL // Из build config
        )
        
        setContent {
            MaterialTheme {
                App(dependencies)
            }
        }
    }
}
```

```kotlin
// build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "WORKFLOW_API_URL", "\"https://api.example.com\"")
    }
}
```

## 7. Дополнительные возможности

### Retry при сетевых ошибках

```kotlin
// Автоматически встроено в WorkflowApiClient
// Для кастомной логики:
val result = RetryUtils.retryIO(times = 3) {
    workflowRepository.startWorkflow("workflow_id")
}
```

### Reactive обновления через Flow

```kotlin
LaunchedEffect(Unit) {
    dependencies.workflowRepository
        .observeWorkflowState()
        .collect { workflowState ->
            workflowState?.let {
                println("Current screen: ${it.currentScreen}")
                println("Context: ${it.context}")
            }
        }
}
```

## Полезные команды

```bash
# Проверка healthcheck
curl http://localhost:8000/healthcheck

# Сохранение workflow (пример)
curl -X POST http://localhost:8000/workflow/save \
  -H "Content-Type: application/json" \
  -d @examples/workflow-login-flow.json

# Создание сессии
curl -X POST http://localhost:8000/client/workflow \
  -H "Content-Type: application/json" \
  -d '{
    "client_session_id": "test-uuid",
    "client_workflow_id": "YOUR_WORKFLOW_ID"
  }'
```

## Troubleshooting

**Ошибка: "workflow_id is required for new session"**
→ При первом запуске укажите `workflowId` в `StartWorkflow`

**Ошибка: "Network error"**
→ Убедитесь, что LCT EFS сервер запущен на указанном URL

**Ошибка: "Session expired"**
→ Вызовите `ResetWorkflow` и запустите workflow заново

**Session_id не сохраняется между запусками**
→ Проверьте, что `multiplatform-settings` правильно настроен для вашей платформы

## Дополнительная документация

- [WORKFLOW_INTEGRATION.md](./WORKFLOW_INTEGRATION.md) — полная документация
- [examples/workflow-login-flow.json](./examples/workflow-login-flow.json) — пример workflow definition
- [LCT EFS API Docs](http://localhost:8000/docs) — Swagger документация (когда сервер запущен)
