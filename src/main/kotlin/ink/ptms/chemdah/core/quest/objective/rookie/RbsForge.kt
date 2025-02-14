package ink.ptms.chemdah.core.quest.objective.rookie

import com.cuzz.rookieblacksmith.events.ForgeEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
@Dependency("RookieBlackSmith")
object RbsForge : ObjectiveCountableI<ForgeEvent>() {
    override val name = "rbs forge"
    override val event = ForgeEvent::class.java
    init {
        handler {
            it.player
        }
    }
}