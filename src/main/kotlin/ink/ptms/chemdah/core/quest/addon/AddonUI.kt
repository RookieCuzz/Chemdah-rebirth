package ink.ptms.chemdah.core.quest.addon

import ink.ptms.chemdah.core.quest.Id
import ink.ptms.chemdah.core.quest.Option
import ink.ptms.chemdah.core.quest.QuestContainer
import ink.ptms.chemdah.core.quest.Template
import taboolib.common.util.asList
import taboolib.library.configuration.ConfigurationSection

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.addon.AddonUI
 *
 * @author sky
 * @since 2021/3/11 9:05 上午
 */
@Id("ui")
@Option(Option.Type.SECTION)
class AddonUI(root: ConfigurationSection, questContainer: QuestContainer) : Addon(root, questContainer) {

    /**
     * 持续在 UI 中显示
     * 启用后则可能显示为 can-start 或 cannot-start
     */
    val visibleStart = root.getBoolean("visible.start", false)

    /**
     * 任务完成后显示为 complete 已完成状态
     */
    val visibleComplete = root.getBoolean("visible.complete", true)

    /**
     * 显示图标
     */
    val icon: String? = root.getString("icon")

    /**
     * 显示介绍，适配 Chemdah Lab
     */
    val description = root["description"]?.asList()?.flatMap { it.lines() }

    companion object {

        /** 获取 UI 组件 */
        fun Template.ui() = addon<AddonUI>("ui")
    }
}