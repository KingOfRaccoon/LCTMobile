package ru.skittens.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.skittens.shared.model.ScreenSchema
import ru.skittens.shared.util.SharedJson

interface RemoteDataSource {
    suspend fun fetchScreen(screenId: String): ScreenSchema
}

class KtorRemoteDataSource(
    private val client: HttpClient,
    private val baseUrl: String
) : RemoteDataSource {
    override suspend fun fetchScreen(screenId: String): ScreenSchema = withContext(Dispatchers.Default) {
        val response = client.get("$baseUrl/screens/$screenId")
        val body = response.body<String>()
        SharedJson.decodeFromString<ScreenSchema>(body)
    }
}
