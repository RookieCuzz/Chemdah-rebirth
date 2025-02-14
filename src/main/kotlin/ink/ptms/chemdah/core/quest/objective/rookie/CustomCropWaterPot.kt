package ink.ptms.chemdah.core.quest.objective.rookie

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import net.momirealms.customcrops.api.event.PotWaterEvent

@Dependency("CustomCrops")
object CustomCropWaterPot : ObjectiveCountableI<PotWaterEvent>() {
    override val name = "customcrops waterpot"
    override val event = PotWaterEvent::class.java
    init {
        handler {
            it.player
        }
    }
}