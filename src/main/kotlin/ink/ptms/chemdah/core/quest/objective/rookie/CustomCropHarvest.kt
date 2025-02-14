package ink.ptms.chemdah.core.quest.objective.rookie

import com.cuzz.rookieblacksmith.events.ForgeEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import net.momirealms.customcrops.api.event.CropInteractEvent

@Dependency("CustomCrops")
object CustomCropHarvest : ObjectiveCountableI<CropInteractEvent>() {
    override val name = "customcrops harvest"
    override val event = CropInteractEvent::class.java
    init {
        handler {
            it.player
        }
        addSimpleCondition("cropid") { data, e ->
            data.toString().equals( e.cropItemID,true)&& (e.itemInHand.type.isAir)
        }
    }
}
