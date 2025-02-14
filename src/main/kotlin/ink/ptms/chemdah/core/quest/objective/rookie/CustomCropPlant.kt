package ink.ptms.chemdah.core.quest.objective.rookie

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import net.momirealms.customcrops.api.event.CropPlantEvent
import net.momirealms.customcrops.api.event.PotPlaceEvent

@Dependency("CustomCrops")
object CustomCropPlant : ObjectiveCountableI<CropPlantEvent>() {
    override val name = "customcrops plant"
    override val event = CropPlantEvent::class.java
    init {
        handler {
            it.player
        }
    }
}