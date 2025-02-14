package ink.ptms.chemdah.core.quest.objective.rookie

import com.cuzz.rookiecropshop.ShopEvent
import com.cuzz.rookiefreeres.ResEvent
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

@Dependency("RookieCropShop")
object RCShop  : ObjectiveCountableI<ShopEvent>()  {
    override val name = "rcshop sell"
    override val event = ShopEvent::class.java
    init {
        handler {
            it.player
        }
    }
}