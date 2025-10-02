package ru.skittens.shared.network

import kotlinx.coroutines.delay
import ru.skittens.shared.model.ScreenSchema

class MockRemoteDataSource(
    private val schemas: Map<String, ScreenSchema>,
    private val delayMillis: Long = 250
) : RemoteDataSource {
    override suspend fun fetchScreen(screenId: String): ScreenSchema {
        delay(delayMillis)
        return schemas[screenId] ?: error("Screen $screenId not found")
    }
}
