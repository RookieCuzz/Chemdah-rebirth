package ink.ptms.chemdah.core.quest.objective.rookie

import com.cuzz.rookiecooking.event.PotBreakEvent
import com.cuzz.rookiefreeres.ResEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

@Dependency("RookieFreeRes")
object RresBlockPlace : ObjectiveCountableI<ResEvent>() {
    override val name = "rres blockplace"
    override val event = ResEvent::class.java
    init {
        handler {
            it.player
        }
    }
}