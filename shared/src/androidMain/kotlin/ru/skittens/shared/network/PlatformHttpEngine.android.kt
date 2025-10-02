package ru.skittens.shared.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual fun providePlatformHttpEngine(): HttpClientEngineFactory<*> = OkHttp
