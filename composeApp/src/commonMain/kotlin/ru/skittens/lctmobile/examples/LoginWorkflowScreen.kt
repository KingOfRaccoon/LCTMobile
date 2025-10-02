package ru.skittens.lctmobile.examples

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.skittens.lctmobile.presentation.workflow.render.WorkflowScreenRenderer
import ru.skittens.shared.model.workflow.StateType

/**
 * Пример Composable экрана для workflow-driven Login Flow
 * 
 * Этот экран автоматически адаптируется к структуре экранов,
 * определенных на backend (через Workflow Engine).
 * 
 * Использование:
 * ```kotlin
 * val viewModel = remember { LoginWorkflowViewModel(workflowManager, scope) }
 * 
 * LaunchedEffect(Unit) {
 *     viewModel.startLoginFlow()
 * }
 * 
 * LoginWorkflowScreen(viewModel)
 * ```
 */
@Composable
fun LoginWorkflowScreen(
    viewModel: LoginWorkflowViewModel,
    onLoginSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Автоматическая навигация при успешной авторизации
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onLoginSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Вход") },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Loading indicator
                uiState.isLoading && uiState.currentScreen == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Динамический рендеринг экрана из workflow
                uiState.currentScreen != null -> {
                    WorkflowScreenRenderer(
                        screenData = uiState.currentScreen!!,
                        context = emptyMap(), // Context управляется сервером
                        onEvent = { eventName, data ->
                            // Обработка событий на основе имени
                            when (eventName) {
                                "submit" -> {
                                    val email = data["email"]?.toString() ?: ""
                                    val password = data["password"]?.toString() ?: ""
                                    viewModel.submitCredentials(email, password)
                                }
                                "submit_2fa" -> {
                                    val code = data["two_factor_code"]?.toString() ?: ""
                                    viewModel.submitTwoFactorCode(code)
                                }
                                "forgot_password" -> {
                                    viewModel.forgotPassword()
                                }
                                else -> {
                                    println("Unknown event: $eventName")
                                }
                            }
                        }
                    )

                    // Loading overlay для асинхронных операций
                    if (uiState.isLoading) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colors.background.copy(alpha = 0.7f)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }

                // Technical/Integration states (не должны отображаться)
                uiState.stateType == StateType.TECHNICAL ||
                uiState.stateType == StateType.INTEGRATION -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Обработка...", style = MaterialTheme.typography.body2)
                    }
                }

                // Ошибка или пустое состояние
                else -> {
                    Text(
                        text = "Загрузка...",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.body1
                    )
                }
            }

            // Snackbar для ошибок
            uiState.error?.let { errorMessage ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("ОК")
                        }
                    }
                ) {
                    Text(errorMessage)
                }
            }
        }
    }
}

/**
 * Превью для Android Studio (опционально)
 */
// @Preview(showBackground = true)
// @Composable
// fun LoginWorkflowScreenPreview() {
//     MaterialTheme {
//         // Mock ViewModel для preview
//         LoginWorkflowScreen(
//             viewModel = mockLoginViewModel()
//         )
//     }
// }
