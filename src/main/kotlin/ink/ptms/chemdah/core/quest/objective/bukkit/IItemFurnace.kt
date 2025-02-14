package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.Task
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.inventory.FurnaceExtractEvent
import org.bukkit.inventory.ItemStack

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IItemFurnace
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IItemFurnace : ObjectiveCountableI<FurnaceExtractEvent>() {

    override val name = "furnace extract"
    override val event = FurnaceExtractEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.block.location)
        }
        addSimpleCondition("item") { data, e ->
            data.toInferItem().isItem(ItemStack(e.itemType))
        }
        addSimpleCondition("exp") { data, e ->
            data.toInt() <= e.expToDrop
        }
        addConditionVariable("exp") {
            it.expToDrop
        }
    }

    override fun getCount(profile: PlayerProfile, task: Task, event: FurnaceExtractEvent): Int {
        return event.itemAmount
    }
}