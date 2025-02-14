package ink.ptms.chemdah.core.quest.objective.brewery

import com.dre.brewery.api.events.barrel.BarrelDestroyEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objiective.brewery.BBarrelDestroy
 *
 * @author Peng_Lx
 * @since 2021/6/4 23:02 下午
 */
@Dependency("Brewery")
object BBarrelDestroy : ObjectiveCountableI<BarrelDestroyEvent>() {

    override val name = "brewery barrel destroy"
    override val event = BarrelDestroyEvent::class.java

    init {
        handler {
            it.playerOptional
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.broken.location)
        }
        addSimpleCondition("reason") { data, e ->
            data.asList().any { it.equals(e.reason.name, true) }
        }
    }
}