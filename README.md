## Backend-Driven UI Kotlin Multiplatform App

Полнофункциональный пример backend-driven UI (BDU) на Kotlin Multiplatform с общей Compose Multiplatform-представлением и Clean Architecture. Структура экрана полностью задаётся JSON-схемами, приходящими из сети, а приложение демонстрирует компактный стек без локального кэша: всё состояние берётся из удалённого API с graceful fallback на моковые данные.

## 📦 Структура проекта

```
composeApp/
	commonMain/        ← слой Presentation (UI + контроллеры)
	androidMain/       ← Android-специфика (Activity, Sqlite driver, Http client)
	iosMain/           ← iOS-специфика (UIViewController, Sqlite driver, Http client)

shared/
	commonMain/
		data/            ← репозиторий и удалённый источник данных
		domain/          ← use-case'ы и бизнес-логика
		model/           ← JSON-схемы компонентов и стили
		network/         ← Ktor-клиент и моки
		util/            ← JSON/стили и вспомогательные функции
		androidMain/       ← Ktor OkHttp engine
		iosMain/           ← Ktor Darwin engine

iosApp/              ← точка входа для SwiftUI/Xcode
```

## 🧱 Архитектура

### Backend-Driven UI Layer
- **Presentation (composeApp/commonMain)** — `BackendDrivenController` управляет состоянием экрана и триггерит загрузку/обновление. `BackendDrivenRenderer` рендерит узлы `SchemaNode` (text, button, column, row, image) из JSON.
- **Domain (shared/domain)** — `LoadScreenUseCase` инкапсулирует получение схемы из репозитория.
- **Data (shared/data + shared/network)** — `ScreenRepository` берёт данные напрямую из `RemoteDataSource`, кэширует их в памяти до следующего обновления и умеет падать backoff'ом на моковые схемы.

### Workflow (LCT EFS) Layer
- **Presentation (composeApp/presentation/workflow)** — `WorkflowController` управляет FSM-driven навигацией через StateFlow (MVI pattern).
- **Domain (shared/domain/workflow)** — Use Cases: `StartWorkflowUseCase`, `SendWorkflowEventUseCase`, `UpdateContextUseCase`.
- **Data (shared/data/workflow)** — `WorkflowRepository` управляет сессиями и синхронизацией контекста с LCT EFS API.
- **Network (shared/network/workflow)** — `WorkflowApiClient` (Ktor) для взаимодействия с workflow execution engine.
- **Session (shared/util/session)** — `SessionManager` для персистентного хранения `client_session_id`.

## ✨ Ключевые возможности

### Backend-Driven UI
- JSON-схемы разделены на секции (`topBar`/`body`/`bottomBar`) и ноды `SchemaNode` с декларативными стилями.
- Compose Multiplatform-рендерер поддерживает базовые компоненты (text, button, column, row, image) и весовые раскладки.
- Репозиторий работает поверх `RemoteDataSource`, в памяти хранит последнее успешное состояние и возвращает время получения.
- Встроенный мок на случай сетевого сбоя: при ошибке HTTP-клиента возвращается локальная схема-образец.

### LCT EFS Workflow Integration
- **Finite State Machine (FSM)** — навигация через состояния (Technical, Integration, Screen, Service).
- **Session Management** — автоматическое управление `client_session_id` с персистентным хранением.
- **Event-Driven Transitions** — переходы между Screen states через события (`event_name`).
- **Context Synchronization** — stateful workflow с автоматической синхронизацией контекста.
- **Python Expressions** — выполнение бизнес-логики на стороне сервера (Technical states).
- **External API Integration** — вызов внешних API через Integration states.

### Общее
- Общий `SharedJson` конфигурирует сериализацию/десериализацию и переиспользуется во всех слоях.
- **MVI Pattern** — StateFlow-based reactive state management для обоих layers.
- **Clean Architecture** — чёткое разделение Presentation → Domain → Data → Network.

## 🚀 Запуск

### Android

```bash
./gradlew :composeApp:installDebug
```

или выберите `composeApp` ▶︎ **Run** в Android Studio/IntelliJ.

### iOS

1. Откройте `iosApp/iosApp.xcodeproj` в Xcode.
2. Выберите схему `iosApp` и запустите на симуляторе/устройстве.

При первом запуске приложение загрузит JSON-схему `main_screen`, отрисует UI и автоматически попытается синхронизировать отложенные действия.

## 🧪 Тесты

```bash
./gradlew :shared:allTests
```

- `ScreenSchemaParsingTest` проверяет десериализацию новой JSON-схемы.
- `ScreenRepositoryTest` валидирует обновление состояния репозитория без локального кэша.



## 🔧 Расширение

### Backend-Driven UI
- **Новые ноды**: расширьте `SchemaNode` и добавьте обработку в `BackendDrivenRenderer`.
- **Интеграция с реальным API**: подставьте собственный `RemoteDataSource` или настройте `KtorRemoteDataSource` на нужный бэкенд.
- **Внешний API**: замените моковый Ktor-клиент в `createAppDependencies`, передав собственный `RemoteDataSource`.

### LCT EFS Workflow
- **Workflow Definitions**: см. примеры в `examples/workflow-*.json`.
- **Custom States**: создайте собственные workflow с Technical/Integration/Screen states.
- **API Configuration**: настройте `workflowBaseUrl` в `createAppDependencies`.
- **Подробная документация**: см. [WORKFLOW_INTEGRATION.md](./WORKFLOW_INTEGRATION.md).

## 📚 Полезные файлы

### Backend-Driven UI
- `composeApp/src/commonMain/kotlin/ru/skittens/lctmobile/App.kt` — точка входа UI и интеграция контроллера.
- `shared/src/commonMain/kotlin/ru/skittens/shared/data/ScreenRepository.kt` — загрузка схемы с сервера, обновление и трансформация под UI.
- `shared/src/commonMain/kotlin/ru/skittens/shared/model/UiSchema.kt` — описание компонентной модели.

### LCT EFS Workflow
- `shared/src/commonMain/kotlin/ru/skittens/shared/model/workflow/WorkflowModels.kt` — DTOs для Workflow API.
- `shared/src/commonMain/kotlin/ru/skittens/shared/data/workflow/WorkflowRepository.kt` — управление workflow сессиями.
- `composeApp/src/commonMain/kotlin/ru/skittens/lctmobile/presentation/workflow/WorkflowController.kt` — FSM-based навигация.
- `examples/workflow-*.json` — примеры workflow definitions.

## 🤝 Вклад

Pull request'ы и предложения приветствуются. Используйте единый формат JSON-схем и следуйте слоям Clean Architecture при добавлении функциональности.