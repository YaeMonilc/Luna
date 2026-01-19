package io.github.yaemonilc.reze.plugin.luna.manager

import io.github.yaemonilc.reze.plugin.luna.util.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    @SerialName("user")
    USER,
    @SerialName("ai")
    AI
}

object HistoryManager {
    private val histories = mutableMapOf<Long, MutableList<Pair<Role, String>>>()

    fun append(
        id: Long,
        role: Role,
        any: String
    ) = Pair(role, any).let { pair ->
        histories.getOrPut(id) {
            mutableListOf(pair)
        }.add(pair)
    }

    fun encode(
        id: Long
    ) = json.encodeToString(histories[id])
}