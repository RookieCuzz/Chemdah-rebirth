package ink.ptms.chemdah.core.quest.addon

import com.google.common.base.Enums
import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.core.quest.*
import ink.ptms.chemdah.core.quest.addon.data.*
import org.bukkit.Bukkit
import taboolib.common.platform.Schedule
import taboolib.common.platform.function.console
import taboolib.common5.RealTime
import taboolib.common5.cint
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.lang.sendLang
import java.util.*
import kotlin.random.Random

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonAutomation
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("automation")
@Option(Option.Type.SECTION)
class AddonAutomation(source: ConfigurationSection, questContainer: QuestContainer) : Addon(source, questContainer) {

    val isAutoAccept = source.getBoolean("auto-accept")

    val plan = if (source.contains("plan")) {
        val method = Enums.getIfPresent(RealTime::class.java, source.getString("plan.method").toString().uppercase()).or(RealTime.START_IN_MONDAY)
        val args = source.getString("plan.type").toString().lowercase().split(" ")
        val type = when (args[0]) {
            "hour" -> PlanTypeHour(
                method,
                RealTime.Type.HOUR,
                args[1].cint,
            )
            "day", "daily" -> PlanTypeDaily(
                method,
                RealTime.Type.DAY,
                args[1].cint,
                args.getOrNull(2)?.cint ?: 6,
                args.getOrNull(3)?.cint ?: 0
            )
            "week", "weekly" -> PlanTypeWeekly(
                method,
                RealTime.Type.WEEK,
                args[1].cint,
                args.getOrNull(2)?.cint ?: 6,
                args.getOrNull(3)?.cint ?: 0,
                args.getOrNull(4)?.cint ?: 0
            )
            else -> null
        }
        if (type != null) {
            if (type.value == 0) {
                type.value = 1
                console().sendLang("console-automation-plan-error", questContainer.id, source)
            }
            Plan(type, source.getInt("plan.count", 1), source.getString("plan.group"))
        } else {
            null
        }
    } else {
        null
    }

    val planGroup: String? = source.getString("plan.group")

    companion object {

        /** 是否自动接受 */
        fun Template.isAutoAccept() = addon<AddonAutomation>("automation")?.isAutoAccept ?: false

        /** 获取计划组件 */
        fun Template.plan() = addon<AddonAutomation>("automation")?.plan

        /** 获取计划组 */
        fun Template.planGroup() = addon<AddonAutomation>("automation")?.planGroup

        @Schedule(period = 40, async = true)
        fun automation40() {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return
            }
            val groups = HashMap<String, PlanGroup>()
            val autoAccept = ArrayList<Template>()
            // 优先加载拥有主动逻辑的 Plan 计划
            ChemdahAPI.questTemplate.forEach { (_, quest) ->
                if (quest.isAutoAccept()) {
                    autoAccept.add(quest)
                } else {
                    val plan = quest.plan()
                    if (plan != null) {
                        val id = if (plan.group != null) "@${plan.group}" else quest.id
                        val group = groups.computeIfAbsent(id) { PlanGroup(id, plan) }
                        group.quests.add(quest)
                    }
                }
            }
            // 加载没有主动逻辑的被 Plan Group 收录的任务
            ChemdahAPI.questTemplate.forEach { (_, quest) ->
                if (quest.plan() == null) {
                    val group = quest.planGroup()
                    if (group != null && groups.containsKey("@$group")) {
                        groups["@$group"]!!.quests.add(quest)
                    }
                }
            }
            if (groups.isEmpty() && autoAccept.isEmpty()) {
                return
            }
            Bukkit.getOnlinePlayers().filter { it.isChemdahProfileLoaded }.forEach { player ->
                val profile = player.chemdahProfile
                // 自动接受的任务
                autoAccept.forEach {
                    if (profile.getQuestById(it.id, openAPI = false) == null) {
                        it.acceptTo(profile)
                    }
                }
                // 定时计划
                groups.forEach self@{ (id, group) ->
                    val nextTime = profile.persistentDataContainer["quest.automation.$id.next", 0L].toLong()
                    if (nextTime < System.currentTimeMillis()) {
                        val newTime = group.plan.nextTime
                        if (newTime < System.currentTimeMillis()) {
                            val now = Date(System.currentTimeMillis())
                            console().sendLang("console-automation-plan-out-of-date", id, Date(newTime), now, group.plan.debug)
                            return@self
                        }
                        profile.persistentDataContainer["quest.automation.$id.next"] = newTime
                        val pool = group.quests.toMutableList()
                        var i = group.plan.count
                        fun process() {
                            if (i > 0 && pool.isNotEmpty()) {
                                pool.removeAt(Random.nextInt(pool.size)).acceptTo(profile).thenAccept {
                                    if (it.type == AcceptResult.Type.SUCCESSFUL) {
                                        i--
                                    }
                                    process()
                                }
                            }
                        }
                        process()
                    }
                }
            }
        }
    }
}
