package ink.ptms.chemdah.core.quest.objective.rookie

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import net.momirealms.customcrops.api.event.PotPlaceEvent
@Dependency("CustomCrops")
object CustomCropPlacePot : ObjectiveCountableI<PotPlaceEvent>() {
    override val name = "customcrops placepot"
    override val event = PotPlaceEvent::class.java
    init {
        handler {
            it.player
        }
    }
}
