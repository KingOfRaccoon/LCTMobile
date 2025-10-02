package ru.skittens.shared.util

import kotlinx.serialization.json.Json

val SharedJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    prettyPrint = false
    explicitNulls = false
}
