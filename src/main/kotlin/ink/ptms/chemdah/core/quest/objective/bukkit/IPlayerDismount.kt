package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.entity.Player
import org.spigotmc.event.entity.EntityDismountEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerDismount
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerDismount : ObjectiveCountableI<EntityDismountEvent>() {

    override val name = "entity dismount"
    override val event = EntityDismountEvent::class.java

    init {
        handler {
            it.entity as? Player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.dismounted.location)
        }
        addSimpleCondition("entity") { data, e ->
            data.toInferEntity().isEntity(e.dismounted)
        }
    }
}