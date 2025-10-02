package ru.skittens.shared.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun providePlatformHttpEngine(): HttpClientEngineFactory<*> = Darwin
