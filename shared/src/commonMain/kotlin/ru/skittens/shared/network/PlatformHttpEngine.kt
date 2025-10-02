package ru.skittens.shared.network

import io.ktor.client.engine.HttpClientEngineFactory

expect fun providePlatformHttpEngine(): HttpClientEngineFactory<*>
