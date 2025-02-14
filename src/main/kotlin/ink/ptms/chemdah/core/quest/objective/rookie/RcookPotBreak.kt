package ink.ptms.chemdah.core.quest.objective.rookie

import com.cuzz.rookiecooking.event.PotBreakEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import net.momirealms.customcrops.api.event.CropInteractEvent
@Dependency("RookieCooking")
object RcookPotBreak : ObjectiveCountableI<PotBreakEvent>() {
    override val name = "rcook potbreak"
    override val event = PotBreakEvent::class.java
    init {
        handler {
            it.player
        }
    }
}