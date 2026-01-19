package io.github.yaemonilc.reze.plugin.luna.util

import io.github.yaemonilc.reze.plugin.luna.Name
import io.github.yaemonilc.reze.plugin.luna.Role
import kotlin.text.lines

private fun readPrompt(
    path: String
): String = readResourceAsText(
    path = path
).let { origin ->
    buildString {
        origin.lines().forEach {
            if (it.startsWith("#include")) {
                val (_, path) = it.split(" ")
                appendLine(
                    value = readPrompt(
                        path = path
                    )
                )
            } else {
                appendLine(it)
            }
        }
    }
}

private fun String.replacePlaceholder(
    match: (String) -> String
): String {
    var text = this

    Regex("#.*?#").findAll(
        input = text
    ).forEach { result ->
        result.value.let {
            text = text.replace(
                oldValue = it,
                newValue = match(it.removePrefix("#").removeSuffix("#"))
            )
        }
    }

    return text
}

fun evaluateSystemPrompt(
    name: Name,
    role: Role,
    history: String? = null,
    notIgnore: Boolean = false
) = readPrompt(
    path = "/prompt/template/evaluate/system"
).replacePlaceholder {
    when (it) {
        "NAME" -> name
        "ROLE" -> role
        "HISTORY" -> history ?: ""
        "NOT_IGNORE" -> "$notIgnore"
        else -> throw IllegalArgumentException()
    }
}

fun agentSystemPrompt(
    name: Name,
    role: Role,
    history: String? = null,
    rejected: Boolean = false
) = readPrompt(
    path = "/prompt/template/agent/system"
).replacePlaceholder {
    when (it) {
        "NAME" -> name
        "ROLE" -> role
        "HISTORY" -> history ?: ""
        "REJECTED" -> "$rejected"
        else -> throw IllegalArgumentException()
    }
}