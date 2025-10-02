package ru.skittens.lctmobile.di

import io.ktor.client.HttpClient
import ru.skittens.shared.network.RemoteDataSource

data class AppEnvironment(
    val httpClient: HttpClient,
    val remoteDataSource: RemoteDataSource? = null
)
