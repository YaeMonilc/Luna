package io.github.yaemonilc.reze.plugin.luna.agent

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.chat.completions.ChatCompletionCreateParams
import io.github.yaemonilc.reze.core.util.getLogger
import io.github.yaemonilc.reze.napcat.entity.action.Action
import io.github.yaemonilc.reze.plugin.luna.Luna
import io.github.yaemonilc.reze.plugin.luna.Name
import io.github.yaemonilc.reze.plugin.luna.Role
import io.github.yaemonilc.reze.plugin.luna.util.agentSystemPrompt
import io.github.yaemonilc.reze.plugin.luna.util.json
import kotlin.jvm.optionals.getOrNull

class Agent private constructor(
    private val client: OpenAIClient,
    private val model: String
) {
    companion object {
        private lateinit var instance: Agent

        fun initialize(
            url: String,
            key: String,
            model: String
        ) {
            if (::instance.isInitialized)
                throw IllegalStateException("Agent already initialized")

            instance = Agent(
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
        rejected: Boolean = false
    ): List<Action>? = client.chat().completions().create(
        params = ChatCompletionCreateParams.builder().apply {
            addSystemMessage(
                text = agentSystemPrompt(
                    name = name,
                    role = role,
                    history = history,
                    rejected = rejected
                )
            )

            addUserMessage(
                text = content
            )

            model(model)
            temperature(1.3)
        }.build()
    ).run {
        usage().get().apply {
            getLogger<Luna>().info("Prompt token usage of agent: ${promptTokens()}")
            getLogger<Luna>().info("Total token usage of agent: ${totalTokens()}")
        }

        choices().firstOrNull()?.message()?.content()?.getOrNull()?.let {
            json.decodeFromString<List<Action>>(it)
        }
    }
}