package ru.skittens.lctmobile.presentation.state

import ru.skittens.shared.data.ScreenResult
import ru.skittens.shared.model.ScreenSchema

data class BackendDrivenUiState(
    val screenId: String,
    val isLoading: Boolean = true,
    val schema: ScreenSchema? = null,
    val lastUpdated: Long? = null,
    val error: String? = null
)

fun BackendDrivenUiState.reduce(result: ScreenResult): BackendDrivenUiState = when (result) {
    ScreenResult.Idle -> this
    ScreenResult.Loading -> copy(isLoading = true, error = null)
    is ScreenResult.Success -> copy(
        isLoading = false,
        schema = result.schema,
        lastUpdated = result.receivedAt,
        error = null
    )
    is ScreenResult.Error -> copy(isLoading = false, error = result.throwable.message)
}
