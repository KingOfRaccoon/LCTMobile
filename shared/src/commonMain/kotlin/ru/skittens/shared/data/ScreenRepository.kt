package ru.skittens.shared.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ru.skittens.shared.model.ScreenSchema
import ru.skittens.shared.network.RemoteDataSource

class ScreenRepository(
    private val remote: RemoteDataSource,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val stateFlows = mutableMapOf<String, MutableStateFlow<ScreenResult>>()
    private val scope = coroutineScope

    fun streamScreen(screenId: String, refresh: Boolean = true): Flow<ScreenResult> {
        val stateFlow = stateFlows.getOrPut(screenId) { MutableStateFlow<ScreenResult>(ScreenResult.Idle) }
        if (refresh) {
            refresh(screenId)
        }
        return stateFlow
    }

    fun refresh(screenId: String) {
        val stateFlow = stateFlows.getOrPut(screenId) { MutableStateFlow<ScreenResult>(ScreenResult.Idle) }
        scope.launch {
            stateFlow.emit(ScreenResult.Loading)
            val result = runCatching { remote.fetchScreen(screenId) }
            result.onSuccess { schema ->
                stateFlow.emit(
                    ScreenResult.Success(
                        schema = schema,
                        receivedAt = Clock.System.now().toEpochMilliseconds()
                    )
                )
            }.onFailure { throwable ->
                stateFlow.emit(ScreenResult.Error(throwable))
            }
        }
    }

    fun getCurrent(screenId: String): ScreenResult? = stateFlows[screenId]?.value
}

sealed class ScreenResult {
    data object Idle : ScreenResult()
    data object Loading : ScreenResult()
    data class Success(
        val schema: ScreenSchema,
        val receivedAt: Long
    ) : ScreenResult()
    data class Error(val throwable: Throwable) : ScreenResult()
}
