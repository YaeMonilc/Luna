package io.github.yaemonilc.reze.plugin.luna.model

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.chat.completions.ChatCompletionCreateParams
import io.github.yaemonilc.reze.core.util.getLogger
import io.github.yaemonilc.reze.plugin.luna.Luna
import io.github.yaemonilc.reze.plugin.luna.Name
import io.github.yaemonilc.reze.plugin.luna.Role
import io.github.yaemonilc.reze.plugin.luna.entity.Evaluate
import io.github.yaemonilc.reze.plugin.luna.util.evaluateSystemPrompt
import io.github.yaemonilc.reze.plugin.luna.util.json
import kotlin.jvm.optionals.getOrNull

class EvaluateModel private constructor(
    private val client: OpenAIClient,
    private val model: String
) {
    companion object {
        private lateinit var instance: EvaluateModel

        fun initialize(
            url: String,
            key: String,
            model: String
        ) {
            if (::instance.isInitialized)
                throw IllegalStateException("EvaluateModel already initialized")

            instance = EvaluateModel(
                client = OpenAIOkHttpClient.builder().apply {
                    baseUrl(url)
                    apiKey(key)
                }.build(),
                model = model
            )
        }

        fun get() = instance
    }

    fun execute(
        content: String,
        name: Name,
        role: Role,
        history: String? = null,
        notIgnore: Boolean = false
    ): Evaluate? = client.chat().completions().create(
        params = ChatCompletionCreateParams.builder().apply {
            addSystemMessage(
                text = evaluateSystemPrompt(
                    name = name,
                    role = role,
                    history = history,
                    notIgnore = notIgnore
                )
            )

            addUserMessage(
                text = content
            )

            model(model)
            temperature(1.0)
        }.build()
    ).run {
        usage().get().apply {
            getLogger<Luna>().info("Prompt token usage of evaluate: ${promptTokens()}")
            getLogger<Luna>().info("Total token usage of evaluate: ${totalTokens()}")
        }

        choices().firstOrNull()?.message()?.content()?.getOrNull()?.let {
            json.decodeFromString<Evaluate>(it)
        }
    }
}