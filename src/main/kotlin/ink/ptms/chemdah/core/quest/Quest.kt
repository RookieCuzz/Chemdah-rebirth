package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.event.collect.ObjectiveEvents
import ink.ptms.chemdah.api.event.collect.QuestEvents
import ink.ptms.chemdah.core.DataContainer
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.addon.AddonControl.Companion.control
import ink.ptms.chemdah.core.quest.addon.AddonOptional.Companion.isOptional
import ink.ptms.chemdah.core.quest.addon.AddonRestart.Companion.canRestart
import ink.ptms.chemdah.core.quest.addon.AddonTimeout.Companion.isTimeout
import ink.ptms.chemdah.core.quest.addon.data.ControlTrigger
import ink.ptms.chemdah.util.exceptNull
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.Quest
 *
 * @author sky
 * @since 2021/3/2 12:03 上午
 */
class Quest(val id: String, val profile: PlayerProfile, val persistentDataContainer: DataContainer = DataContainer()) {

    /**
     * 获取任务模板
     */
    val template: Template
        get() = ChemdahAPI.getQuestTemplate(id)!!

    /**
     * 任务是否有效（即模板是否存在）
     */
    val isValid: Boolean
        get() = ChemdahAPI.getQuestTemplate(id) != null

    /**
     * 任务是否完成（完成签名）
     */
    val isCompleted: Boolean
        get() = isValid && template.taskMap.all { it.value.objective.hasCompletedSignature(profile, it.value) }

    /**
     * 获取所有条目
     */
    val tasks: Collection<Task>
        get() = template.taskMap.values

    /**
     * 任务开始时间
     */
    val startTime: Long
        get() = persistentDataContainer["start", 0L].toLong()

    /**
     * 任务是否超时
     */
    val isTimeout: Boolean
        get() = template.isTimeout(startTime)

    /**
     * 是否为新的任务，擅自修改这个属性会导致数据出错
     */
    var newQuest = false

    /**
     * 启用时任务将被锁定
     */
    var lock = false

    init {
        persistentDataContainer["start"] = System.currentTimeMillis()
        profile.persistentDataContainer.remove("quest.complete.$id")
    }

    /**
     * 判断该玩家是否为该任务的拥有者
     */
    fun isOwner(player: Player) = profile.uniqueId == player.uniqueId

    /**
     * 获取条目
     */
    fun getTask(id: String) = tasks.firstOrNull { it.id == id }

    /**
     * 强制完成任务
     */
    fun completeQuest() {
        tasks.forEach { it.objective.setCompletedSignature(profile, it, true) }
        checkCompleteFuture()
    }

    /**
     * 检查任务的所有条目
     * 当所有条目均已完成时任务完成
     */
    @Deprecated("Use checkCompleteFuture", ReplaceWith("checkCompleteFuture()"))
    fun checkComplete() {
        checkCompleteFuture()
    }

    fun checkCompleteFuture(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        template.canRestart(profile).thenAccept { reset ->
            if (reset) {
                restartQuestFuture().thenAccept { future.complete(false) }
            } else {
                // 所有条目均已完成且通过事件检查
                if (tasks.all { it.objective.hasCompletedSignature(profile, it) || it.isOptional() } && QuestEvents.Complete.Pre(this@Quest, profile).call()) {
                    template.agent(profile, AgentType.QUEST_COMPLETE).thenAccept {
                        if (it) {
                            profile.persistentDataContainer["quest.complete.$id"] = System.currentTimeMillis()
                            profile.unregisterQuest(this@Quest)
                            template.control().signature(profile, ControlTrigger.COMPLETE)
                            template.agent(profile, AgentType.QUEST_COMPLETED)
                            QuestEvents.Complete.Post(this@Quest, profile).call()
                        }
                        future.complete(it)
                    }.exceptNull {
                        future.complete(false)
                    }
                } else {
                    future.complete(false)
                }
            }
        }
        return future
    }

    /**
     * 放弃任务
     */
    @Deprecated("Use failQuestFuture", ReplaceWith("failQuestFuture()"))
    fun failQuest() {
        failQuestFuture()
    }

    fun failQuestFuture(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        if (QuestEvents.Fail.Pre(this@Quest, profile).call()) {
            template.agent(profile, AgentType.QUEST_FAIL).thenAccept {
                if (it) {
                    profile.unregisterQuest(this@Quest)
                    template.control().signature(profile, ControlTrigger.FAIL)
                    template.agent(profile, AgentType.QUEST_FAILED)
                    QuestEvents.Fail.Post(this@Quest, profile).call()
                }
                future.complete(it)
            }.exceptNull {
                future.complete(false)
            }
        } else {
            future.complete(false)
        }
        return future
    }

    /**
     * 重置任务
     */
    @Deprecated("Use restartQuestFuture", ReplaceWith("restartQuestFuture()"))
    fun restartQuest() {
        restartQuestFuture()
    }

    fun restartQuestFuture(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        if (QuestEvents.Restart.Pre(this@Quest, profile).call()) {
            template.agent(profile, AgentType.QUEST_RESTART).thenAccept {
                if (it) {
                    tasks.forEach { task ->
                        if (ObjectiveEvents.Restart.Pre(task.objective, task, this@Quest, profile).call()) {
                            task.objective.onReset(profile, task, this@Quest)
                            task.agent(profile, AgentType.TASK_RESTARTED)
                            ObjectiveEvents.Restart.Post(task.objective, task, this@Quest, profile).call()
                        }
                    }
                    persistentDataContainer.clear()
                    template.agent(profile, AgentType.QUEST_RESTARTED)
                    QuestEvents.Restart.Post(this@Quest, profile).call()
                }
                future.complete(it)
            }.exceptNull {
                future.complete(false)
            }
        } else {
            future.complete(false)
        }
        return future
    }
}