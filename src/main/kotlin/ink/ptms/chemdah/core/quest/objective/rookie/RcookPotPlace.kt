package ink.ptms.chemdah.core.quest.objective.rookie

import com.cuzz.rookiecooking.event.PotBreakEvent
import com.cuzz.rookiecooking.event.PotPlaceEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

@Dependency("RookieCooking")
object RcookPotPlace : ObjectiveCountableI<PotPlaceEvent>() {
    override val name = "rcook potplace"
    override val event = PotPlaceEvent::class.java
    init {
        handler {
            it.player
        }
    }
}