package ru.skittens.lctmobile

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import io.ktor.client.engine.darwin.Darwin
import ru.skittens.lctmobile.di.AppEnvironment
import ru.skittens.shared.network.createHttpClient

fun MainViewController() = ComposeUIViewController {
    val httpClient = remember { createHttpClient(Darwin) }
    DisposableEffect(Unit) {
        onDispose {
            httpClient.close()
        }
    }
    App(environment = AppEnvironment(httpClient = httpClient))
}
