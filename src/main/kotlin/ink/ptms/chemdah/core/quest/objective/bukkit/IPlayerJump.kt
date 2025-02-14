package ink.ptms.chemdah.core.quest.objective.bukkit

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerJump
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerJump : ObjectiveCountableI<PlayerJumpEvent>() {

    override val name = "player jump"
    override val event = PlayerJumpEvent::class.java
    override val isAsync = true

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
    }
}