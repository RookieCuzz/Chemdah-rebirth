package ink.ptms.chemdah.core.quest.objective.rookie

import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import net.momirealms.customcrops.api.event.CropInteractEvent
import net.momirealms.customcrops.api.event.PotPlaceEvent
@Dependency("ItemsAdder")
object ItemsAdderBlockPlace : ObjectiveCountableI<CustomBlockPlaceEvent>(){
    override val name = "iablock place"
    override val event = CustomBlockPlaceEvent::class.java
    init {
        handler {
            it.player
        }
        addSimpleCondition("cmd") { data, e ->
            data.toDouble() == e.itemInHand.itemMeta?.customModelData?.toDouble()
        }
    }
}