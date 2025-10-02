package ru.skittens.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import ru.skittens.shared.util.SharedJson

fun createHttpClient(
    engineFactory: HttpClientEngineFactory<*>,
    enableLogging: Boolean = true
): HttpClient = HttpClient(engineFactory) {
    install(ContentNegotiation) {
        json(SharedJson)
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 15_000
    }
    if (enableLogging) {
        install(Logging) {
            level = LogLevel.INFO
        }
    }
}
