package ink.ptms.chemdah.core.quest.objective.quickshop

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.maxgamer.quickshop.event.ShopCreateEvent

@Dependency("QuickShop")
object QShopCreate : ObjectiveCountableI<ShopCreateEvent>() {

    override val name = "quickshop create"
    override val event = ShopCreateEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, it ->
            data.toPosition().inside(it.player!!.location)
        }
    }
}