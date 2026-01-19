package io.github.yaemonilc.reze.plugin.luna.util

import kotlinx.serialization.json.Json

val json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}