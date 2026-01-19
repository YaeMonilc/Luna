package io.github.yaemonilc.reze.plugin.luna

import io.github.yaemonilc.reze.core.IPlugin
import io.github.yaemonilc.reze.core.subscribeEvent
import io.github.yaemonilc.reze.core.util.getLogger
import io.github.yaemonilc.reze.napcat.Session
import io.github.yaemonilc.reze.napcat.entity.event.Event
import io.github.yaemonilc.reze.napcat.entity.event.message.GroupMessageEvent
import io.github.yaemonilc.reze.napcat.entity.event.message.PrivateMessageEvent
import io.github.yaemonilc.reze.napcat.entity.event.notice.FriendPokeEvent
import io.github.yaemonilc.reze.napcat.entity.event.notice.GroupPokeEvent
import io.github.yaemonilc.reze.plugin.luna.agent.Agent
import io.github.yaemonilc.reze.plugin.luna.entity.Config
import io.github.yaemonilc.reze.plugin.luna.entity.Evaluate
import io.github.yaemonilc.reze.plugin.luna.manager.HistoryManager
import io.github.yaemonilc.reze.plugin.luna.manager.Role
import io.github.yaemonilc.reze.plugin.luna.model.EvaluateModel
import io.github.yaemonilc.reze.plugin.luna.util.json

typealias Sign = String
typealias Name = String
typealias Role = String

class Luna : IPlugin {
    override suspend fun onLoaded() {
        Config.initialize()

        Config.get().apply {
            models.apply {
                agent.apply {
                    Agent.initialize(
                        url = url,
                        key = key,
                        model = model
                    )
                }
                evaluate.apply {
                    EvaluateModel.initialize(
                        url = url,
                        key = key,
                        model = model
                    )
                }
            }
        }

        subscribeEvent().collect { (session, event) ->
            dispatchEvent(
                session = session,
                event = event
            )
        }
    }

    suspend fun dispatchEvent(
        session: Session,
        event: Event
    ) {
        Config.get().apply {
            when (event) {
                is PrivateMessageEvent ->
                    event.sender.userId
                is GroupMessageEvent ->
                    event.groupId
                is GroupPokeEvent ->
                    event.groupId
                is FriendPokeEvent ->
                    event.userId
                else -> return
            }.let { id ->
                json.encodeToString(event).let { content ->
                    (bots[session.sign] ?: return).let { name ->
                        (roles["${session.sign}:$id"] ?: return).let { role ->
                            HistoryManager.encode(id).let { history ->
                                EvaluateModel.get().execute(
                                    content = content,
                                    name = name,
                                    role = role,
                                    history = history
                                )?.let { (action) ->
                                    getLogger<Luna>().info("Evaluate: $action")

                                    if (action == Evaluate.Action.IGNORE)
                                        return

                                    Agent.get().execute(
                                        content = content,
                                        name = name,
                                        role = role,
                                        history = history,
                                        rejected = action == Evaluate.Action.REJECT
                                    )?.forEach { action ->
                                        session.emitAction(
                                            action = action
                                        )

                                        HistoryManager.append(
                                            id = id,
                                            role = Role.AI,
                                            any = action.toString()
                                        )
                                    }

                                    HistoryManager.append(
                                        id = id,
                                        role = Role.USER,
                                        any = event.toString()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}