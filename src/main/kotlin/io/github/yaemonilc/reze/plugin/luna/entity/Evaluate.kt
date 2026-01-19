package io.github.yaemonilc.reze.plugin.luna.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Evaluate(
    val action: Action
) {
    @Serializable
    enum class Action {
        @SerialName("ignore")
        IGNORE,
        @SerialName("reject")
        REJECT,
        @SerialName("approved")
        APPROVED
    }
}
