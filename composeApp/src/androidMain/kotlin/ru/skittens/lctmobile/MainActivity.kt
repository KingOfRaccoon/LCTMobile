package ru.skittens.lctmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import io.ktor.client.engine.okhttp.OkHttp
import ru.skittens.lctmobile.di.AppEnvironment
import ru.skittens.shared.network.createHttpClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val httpClient = remember { createHttpClient(OkHttp) }
            DisposableEffect(Unit) {
                onDispose {
                    httpClient.close()
                }
            }
            App(environment = AppEnvironment(httpClient = httpClient))
        }
    }
}
