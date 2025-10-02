package ru.skittens.lctmobile.di

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ru.skittens.lctmobile.presentation.BackendDrivenController
import ru.skittens.lctmobile.presentation.workflow.WorkflowController
import ru.skittens.shared.data.ScreenRepository
import ru.skittens.shared.data.workflow.WorkflowRepository
import ru.skittens.shared.data.workflow.WorkflowRepositoryImpl
import ru.skittens.shared.domain.LoadScreenUseCase
import ru.skittens.shared.domain.workflow.*
import ru.skittens.shared.model.ScreenSchema
import ru.skittens.shared.network.KtorRemoteDataSource
import ru.skittens.shared.network.MockRemoteDataSource
import ru.skittens.shared.network.RemoteDataSource
import ru.skittens.shared.network.workflow.KtorWorkflowApiClient
import ru.skittens.shared.network.workflow.WorkflowApiClient
import ru.skittens.shared.util.SharedJson
import ru.skittens.shared.util.session.SessionManager

class AppDependencies(
    val controller: BackendDrivenController,
    val workflowController: WorkflowController,
    val repository: ScreenRepository,
    val workflowRepository: WorkflowRepository,
    val remoteDataSource: RemoteDataSource
)

fun createAppDependencies(
    httpClient: HttpClient,
    remoteOverride: RemoteDataSource? = null,
    workflowBaseUrl: String = "http://localhost:8000",
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
): AppDependencies {
    // Screen (Backend-Driven UI) dependencies
    val remote = remoteOverride ?: defaultRemoteDataSource(httpClient)
    val repository = ScreenRepository(remote = remote, coroutineScope = coroutineScope)
    val controller = BackendDrivenController(
        loadScreenUseCase = LoadScreenUseCase(repository),
        scope = coroutineScope
    )

    // Workflow (LCT EFS) dependencies
    val settings = Settings()
    val sessionManager = SessionManager(settings)
    val workflowApiClient: WorkflowApiClient = KtorWorkflowApiClient(httpClient, workflowBaseUrl)
    val workflowRepository: WorkflowRepository = WorkflowRepositoryImpl(workflowApiClient, sessionManager)
    
    val startWorkflowUseCase = StartWorkflowUseCase(workflowRepository)
    val sendEventUseCase = SendWorkflowEventUseCase(workflowRepository)
    val updateContextUseCase = UpdateContextUseCase(workflowRepository)
    val observeWorkflowStateUseCase = ObserveWorkflowStateUseCase(workflowRepository)
    
    val workflowController = WorkflowController(
        startWorkflow = startWorkflowUseCase,
        sendEvent = sendEventUseCase,
        updateContext = updateContextUseCase,
        observeWorkflowState = observeWorkflowStateUseCase,
        scope = coroutineScope
    )

    return AppDependencies(
        controller = controller,
        workflowController = workflowController,
        repository = repository,
        workflowRepository = workflowRepository,
        remoteDataSource = remote
    )
}

private fun defaultRemoteDataSource(httpClient: HttpClient): RemoteDataSource {
    val sampleJson = """
        {
          "document": {
            "documentId": "demo",
            "name": "Demo screen",
            "exportedAt": "2025-01-01T00:00:00Z"
          },
          "screen": {
            "id": "main_screen",
            "type": "screen",
            "name": "Главная",
            "sections": {
              "topBar": {
                "type": "text",
                "content": "LCT Mobile",
                "style": {"padding": 12, "backgroundColor": "#FFEEEEEE"}
              },
              "body": {
                "type": "column",
                "style": {"padding": 16, "spacing": 12},
                "children": [
                  {
                    "type": "text",
                    "content": "Добро пожаловать!"
                  },
                  {
                    "type": "button",
                    "content": "Обновить",
                    "style": {"paddingHorizontal": 12, "paddingVertical": 8}
                  }
                ]
              }
            },
            "references": {}
          }
        }
    """.trimIndent()

    val schema = SharedJson.decodeFromString<ScreenSchema>(sampleJson)
    val remote = KtorRemoteDataSource(httpClient, baseUrl = "https://example.com")
    val mock = MockRemoteDataSource(mapOf(schema.screen.id to schema))
    return object : RemoteDataSource {
        override suspend fun fetchScreen(screenId: String): ScreenSchema =
            runCatching { remote.fetchScreen(screenId) }
                .getOrElse { mock.fetchScreen(screenId) }
    }
}
