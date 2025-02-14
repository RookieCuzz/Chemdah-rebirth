package ink.ptms.chemdah.core.quest.objective.bukkit

import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import org.bukkit.event.player.PlayerEditBookEvent

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.objective.bukkit.IPlayerBook
 *
 * @author sky
 * @since 2021/3/2 5:09 下午
 */
@Dependency("minecraft")
object IPlayerBook : ObjectiveCountableI<PlayerEditBookEvent>() {

    override val name = "edit book"
    override val event = PlayerEditBookEvent::class.java

    init {
        handler {
            it.player
        }
        addSimpleCondition("position") { data, e ->
            data.toPosition().inside(e.player.location)
        }
        addSimpleCondition("signing") { data, e ->
            data.toBoolean() == e.isSigning
        }
        addSimpleCondition("page") { data, e ->
            data.toInt() <= e.newBookMeta.pageCount
        }
        addSimpleCondition("title") { data, e ->
            data.toString() in e.newBookMeta.title.toString()
        }
        addSimpleCondition("content") { data, e ->
            data.toString() in e.newBookMeta.pages.toString()
        }
        addConditionVariable("page") {
            it.newBookMeta.pageCount
        }
        addConditionVariable("title") {
            it.newBookMeta.title.toString()
        }
        addConditionVariable("content") {
            it.newBookMeta.pages.toString()
        }
    }
}