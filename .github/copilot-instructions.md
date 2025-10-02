# LCT Mobile - AI Coding Agent Instructions

## Архитектура проекта

Это **Backend-Driven UI (BDU)** приложение на **Kotlin Multiplatform** с Compose Multiplatform и интеграцией **LCT EFS (Execution Flow System)** — workflow execution engine на базе FSM.

### Модули и слои

- **`shared/`** — общая логика (Data + Domain), используется Android и iOS
  
  **Backend-Driven UI:**
  - `data/` — `ScreenRepository` (in-memory state, no persistent cache)
  - `domain/` — `LoadScreenUseCase` (единственная бизнес-логика для экранов)
  - `model/` — JSON-модели: `ScreenSchema`, `SchemaNode`, `ScreenSections`
  - `network/` — `RemoteDataSource`, `KtorRemoteDataSource`, mock fallback
  
  **LCT EFS Workflow:**
  - `data/workflow/` — `WorkflowRepository`, `WorkflowRepositoryImpl` (session management)
  - `domain/workflow/` — Use Cases: `StartWorkflowUseCase`, `SendWorkflowEventUseCase`, `UpdateContextUseCase`, `ObserveWorkflowStateUseCase`
  - `model/workflow/` — DTOs: `StateModel`, `WorkflowState`, `WorkflowError`
  - `network/workflow/` — `WorkflowApiClient`, `KtorWorkflowApiClient`
  - `util/session/` — `SessionManager` (multiplatform-settings для client_session_id)
  
  **Общее:**
  - `util/` — `SharedJson` (kotlinx.serialization config), `RetryUtils`
  
- **`composeApp/`** — Presentation layer (UI)
  - `commonMain/presentation/` — `BackendDrivenController`, `BackendDrivenRenderer`, `App.kt`
  - `commonMain/presentation/workflow/` — `WorkflowController` (MVI), `WorkflowUiState`, `WorkflowIntent`
  - `commonMain/di/` — `AppModule.kt` (DI для обоих layers)
  - `androidMain/` — `MainActivity` с OkHttp engine
  - `iosMain/` — `MainViewController` с Darwin engine

- **`iosApp/`** — SwiftUI wrapper для iOS (не редактировать Kotlin здесь)

### Ключевые компоненты

**Backend-Driven UI:**
1. **`BackendDrivenController`** (`composeApp/presentation/`) — управляет UI state через `StateFlow`, вызывает use-case
2. **`BackendDrivenRenderer`** (`composeApp/presentation/render/`) — рендерит `SchemaNode` в Compose UI
3. **`ScreenRepository`** (`shared/data/`) — держит state в памяти, без SQLite/Room
4. **`RemoteDataSource`** (`shared/network/`) — интерфейс + Ktor impl + mock fallback

**LCT EFS Workflow:**
1. **`WorkflowController`** (`composeApp/presentation/workflow/`) — управляет FSM-driven навигацией через StateFlow (MVI)
2. **`WorkflowRepository`** (`shared/data/workflow/`) — управление сессиями, отправка событий, синхронизация контекста
3. **`WorkflowApiClient`** (`shared/network/workflow/`) — HTTP клиент для LCT EFS API (Ktor)
4. **`SessionManager`** (`shared/util/session/`) — персистентное хранение `client_session_id`

## JSON-схемы и компоненты

### Структура ScreenSchema

```kotlin
ScreenSchema {
  document: DocumentMeta
  screen: ScreenDefinition {
    sections: {
      topBar: SchemaNode?
      body: SchemaNode?
      bottomBar: SchemaNode?
    }
  }
}
```

### Поддерживаемые типы SchemaNode

В `BackendDrivenRenderer.kt` обрабатываются:
- `"column"/"container"/"section"/"body"` → Column с spacing/alignment
- `"row"` → Row с spacing/alignment
- `"text"/"title"/"subtitle"/"label"` → Text
- `"button"` → Button (onClick заглушка, см. раздел Actions)
- `"image"` → (заглушка, расширяемо)
- `"spacer"` → Spacer

**Расширение компонентов:** Для добавления новых типов (TextField, Checkbox, Card, etc.) следуй паттерну: создай `RenderXXX()` функцию и добавь case в `RenderNode()` switch.

### Стили (JsonObject)

Применяются через `Modifier.applyStyle()` (`composeApp/util/StyleExtensions.kt`):
- `padding`, `paddingHorizontal`, `paddingVertical` → dp
- `backgroundColor` → "#RRGGBB" или "#AARRGGBB"
- `height`, `width` → dp
- `spacing` → для Column/Row
- `horizontalAlignment`/`verticalAlignment` → "start", "center", "end"

**Всегда используй JsonObject для стилей**, не создавай typed classes.

## Actions и события

**Система событий временно отключена.** Файлы `shared/event/UiEvent.kt` и `shared/cache/PendingEventQueue.kt` содержат заглушки. В `BackendDrivenRenderer.kt` все `onClick` обработчики — пустые лямбды.

**Для реализации actions:**
1. Добавь поле `action` в `SchemaNode` (например, `action: JsonObject?`)
2. Создай action handler в `BackendDrivenController`
3. Передавай callback через параметры `RenderNode()`
4. В JSON-схеме описывай actions как `{"type": "navigate", "target": "screen_id"}` или `{"type": "api_call", "endpoint": "/submit"}`

## Паттерны и конвенции

### Dependency Injection

- Вручную в `createAppDependencies()` (`composeApp/di/AppModule.kt`)
- Возвращает `AppDependencies(controller, repository, remoteDataSource)`
- Для тестов передавай `remoteOverride: RemoteDataSource?`

### Сериализация

- **Всегда используй `SharedJson`** (`shared/util/JsonUtils.kt`) для декодирования/энкодирования
- Config: `ignoreUnknownKeys=true`, `isLenient=true`, `explicitNulls=false`

### State Management

- Repository эмиттит `Flow<ScreenResult>` (sealed: Idle, Loading, Success, Error)
- Controller редуцирует `ScreenResult` → `BackendDrivenUiState` (`composeApp/presentation/state/`)
- UI наблюдает за `controller.state.collectAsState()`

### Platform-specific code

- `androidMain/` — Ktor OkHttp engine, `MainActivity`
- `iosMain/` — Ktor Darwin engine, `MainViewController()`
- Общий код в `commonMain/` — Compose UI, controller, renderer

### Тесты

- `shared/src/commonTest/` — JUnit + `runTest` из coroutines-test
- Примеры: `ScreenSchemaParsingTest`, `ScreenRepositoryTest`
- Запуск: `./gradlew :shared:allTests`

## Workflow

### Сборка и запуск

**Android:**
```bash
./gradlew :composeApp:installDebug
# или в Android Studio: Run 'composeApp'
```

**iOS:**
1. Открой `iosApp/iosApp.xcodeproj` в Xcode
2. Запусти на симуляторе/устройстве

### Добавление новых компонентов UI

1. Расширь `SchemaNode` type в `shared/model/UiSchema.kt` (если нужно новое поле)
2. Добавь case в `RenderNode()` в `BackendDrivenRenderer.kt`
3. Реализуй `@Composable` функцию (например, `RenderTextField()`)
4. Применяй стили через `Modifier.applyStyle(node.style)`

**Примеры новых компонентов для реализации:**
- `TextField` — текстовый ввод с `properties.placeholder`, `properties.value`
- `Checkbox`/`Switch` — boolean состояние через `properties.checked`
- `Card` — контейнер с elevation и rounded corners
- `LazyColumn` — для списков с `children` или `items`
- `Dropdown` — выпадающий список с `properties.options`

### Интеграция с реальным API

- В `AppModule.kt` замени `defaultRemoteDataSource()` на `KtorRemoteDataSource(httpClient, baseUrl="https://your-api.com")`
- Убери mock fallback, если не нужен

### Debugging

- Repository держит последний state в памяти — используй `getCurrent(screenId)`
- Ktor logging включен — смотри сетевые запросы в logcat/console
- Для тестирования схем: передай `remoteOverride = MockRemoteDataSource(mapOf(...))`

## Не делай

- ❌ Не добавляй Room/SQLite — репо работает in-memory
- ❌ Не создавай typed style classes — используй JsonObject
- ❌ Не меняй `SharedJson` config без проверки тестов
- ❌ Не редактируй SwiftUI код в `iosApp/` — логика в Kotlin
- ❌ Не используй другой JSON config — только `SharedJson`

## LCT EFS Workflow Integration

### API Endpoints (Base URL: http://localhost:8000)

1. **POST `/workflow/save`** — сохранение workflow definition
2. **POST `/client/workflow`** — управление сессией и выполнение workflow
3. **GET `/healthcheck`** — проверка доступности

### Типы состояний (State Types)

1. **Technical** — выполнение Python-выражений, обновление переменных
2. **Integration** — вызов внешних API
3. **Screen** — UI-состояние, управляется событиями (`event_name`)
4. **Service** — системные состояния (`__init__`, `__error__`)

### Workflow Lifecycle

1. **Создание workflow** → `repository.saveWorkflow(states, context)`
2. **Инициализация сессии** → `controller.onIntent(WorkflowIntent.StartWorkflow(workflowId))`
3. **Переходы через события** → `controller.onIntent(WorkflowIntent.SendEvent(eventName, data))`
4. **Обновление контекста** → `controller.onIntent(WorkflowIntent.UpdateContext(updates))`

### Использование в UI

```kotlin
@Composable
fun WorkflowScreen(controller: WorkflowController) {
    val uiState by controller.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        controller.onIntent(WorkflowIntent.StartWorkflow("workflow_id"))
    }
    
    when (uiState.currentScreen) {
        "login_screen" -> LoginScreen(controller)
        "dashboard" -> DashboardScreen(controller)
        else -> EmptyScreen()
    }
}
```

### Важные особенности

- **Session Management**: `client_session_id` автоматически генерируется и сохраняется локально
- **Служебные переменные**: префикс `__` (не отображаются в UI)
- **Stateful workflow**: контекст сохраняется между запросами
- **Event-driven navigation**: Screen states управляются через `event_name`

### Примеры и документация

- `examples/workflow-*.json` — примеры workflow definitions
- `WORKFLOW_INTEGRATION.md` — полная документация по интеграции

## Полезные файлы для reference

**Backend-Driven UI:**
- `shared/src/commonMain/kotlin/ru/skittens/shared/model/UiSchema.kt` — модель данных
- `composeApp/src/commonMain/kotlin/ru/skittens/lctmobile/presentation/render/BackendDrivenRenderer.kt` — логика рендеринга
- `composeApp/src/commonMain/kotlin/ru/skittens/lctmobile/util/StyleExtensions.kt` — парсинг стилей
- `shared/src/commonMain/kotlin/ru/skittens/shared/data/ScreenRepository.kt` — управление state

**LCT EFS Workflow:**
- `shared/src/commonMain/kotlin/ru/skittens/shared/model/workflow/WorkflowModels.kt` — DTOs для API
- `shared/src/commonMain/kotlin/ru/skittens/shared/data/workflow/WorkflowRepository.kt` — управление сессиями
- `composeApp/src/commonMain/kotlin/ru/skittens/lctmobile/presentation/workflow/WorkflowController.kt` — FSM controller
- `shared/src/commonMain/kotlin/ru/skittens/shared/util/session/SessionManager.kt` — session storage

**Общее:**
- `composeApp/src/commonMain/kotlin/ru/skittens/lctmobile/di/AppModule.kt` — DI для обоих layers
