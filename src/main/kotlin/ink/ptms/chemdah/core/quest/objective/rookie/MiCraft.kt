package ink.ptms.chemdah.core.quest.objective.rookie

import com.cuzz.rookieblacksmith.events.ForgeEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import net.Indyuce.mmoitems.CraftEvent

@Dependency("MMOItems")
object MiCraft : ObjectiveCountableI<CraftEvent>() {
    override val name = "mi craft"
    override val event = CraftEvent::class.java
    init {
        handler {
            it.player
        }
        addSimpleCondition("recipeID") { data, e ->
            data.toString().equals( e.itemID,true)
        }
    }
}