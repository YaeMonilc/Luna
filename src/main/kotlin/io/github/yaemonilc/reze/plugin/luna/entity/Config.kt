package io.github.yaemonilc.reze.plugin.luna.entity

import io.github.yaemonilc.reze.core.Plugin
import io.github.yaemonilc.reze.plugin.luna.Luna
import io.github.yaemonilc.reze.plugin.luna.Name
import io.github.yaemonilc.reze.plugin.luna.Role
import io.github.yaemonilc.reze.plugin.luna.Sign
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

private val json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    prettyPrint = true
}

@Serializable
data class Config(
    val models: Models = Models(),
    val bots: Map<Sign, Name> = mutableMapOf(),
    val roles: Map<String, Role> = mutableMapOf(),
) {
    companion object {
        private lateinit var instance: Config

        fun initialize() {
            if (::instance.isInitialized)
                throw IllegalStateException("Config already initialized")

            File(Plugin.privateDirectory<Luna>(), "config.json").apply {
                json.run {
                    if (!exists()) {
                        createNewFile()

                        instance = Config().also {
                            writeText(
                                text = encodeToString(it)
                            )
                        }
                    } else {
                        instance = decodeFromString<Config>(readText())
                    }
                }
            }
        }

        fun get() = instance
    }

    @Serializable
    data class Models(
        val agent: Model = Model(),
        val evaluate: Model = Model()
    ) {
        @Serializable
        data class Model(
            val url: String = "",
            val key: String = "",
            val model: String = ""
        )
    }
}