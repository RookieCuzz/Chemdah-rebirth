package ink.ptms.chemdah.core.quest.objective.rookie

import com.cuzz.rookiecooking.event.CookingEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI


@Dependency("RookieCooking")
object RcookCooking : ObjectiveCountableI<CookingEvent>() {
    override val name = "rcook cooking"
    override val event = CookingEvent::class.java
    init {
        handler {
            it.player
        }
    }
}