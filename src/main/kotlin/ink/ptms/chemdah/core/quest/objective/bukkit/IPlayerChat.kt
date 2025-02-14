package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.AsyncPlayerChatEvent
import taboolib.common.platform.event.EventPriority

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerChat
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerChat : ObjectiveCountableI<AsyncPlayerChatEvent>() {

    override val name = "player chat"
    override val event = AsyncPlayerChatEvent::class.java
    override val priority = EventPriority.LOWEST

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
        addSimpleCondition("message") { data, e ->
            data.toString() in e.message
        }
        addConditionVariable("message") {
            it.message
        }
    }
}