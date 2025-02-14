package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.PlayerReply
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.util.namespace
import ink.ptms.chemdah.util.replace
import ink.ptms.chemdah.util.setIcon
import ink.ptms.chemdah.util.thenTrue
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.util.asList
import taboolib.common5.cint
import taboolib.module.chat.colored
import taboolib.module.kether.KetherFunction
import taboolib.module.kether.KetherShell
import taboolib.module.kether.extend
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.util.modifyMeta
import java.util.concurrent.CompletableFuture

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ThemeChest
 *
 * @author sky
 * @since 2021/2/12 2:08 上午
 */
object ThemeChest : Theme<ThemeChestSetting>() {

    override fun createConfig(): ThemeChestSetting {
        return ThemeChestSetting(ConversationManager.conf.getConfigurationSection("theme-chest")!!)
    }

    override fun allowFarewell(): Boolean {
        return false
    }

    override fun onDisplay(session: Session, message: List<String>, canReply: Boolean): CompletableFuture<Void> {
        var end = false
        return session.createDisplay { replies ->
            rows(session.player, replies.size).thenAccept { rows ->
                session.player.openMenu<Basic>(settings.title.toTitle(session)) {
                    rows(rows)
                    onBuild(async = true) { _, inventory ->
                        replies.forEachIndexed { index, reply ->
                            if (index < settings.playerSlot.size) {
                                val rep = if (!reply.isPlayerSelected(session.player)) settings.playerItem else settings.playerItemSelected
                                inventory.setItem(settings.playerSlot[index], rep.buildItem(session, reply, index + 1))
                            }
                        }
                        inventory.setItem(settings.npcSlot, settings.npcItem.buildItem(session, message))
                        // 唤起事件
                        ConversationEvents.ChestThemeBuild(session, message, canReply, inventory)
                    }
                    onClick(lock = true) { event ->
                        replies.getOrNull(settings.playerSlot.indexOf(event.rawSlot))?.run {
                            check(session).thenTrue {
                                end = true
                                select(session).thenAccept {
                                    // 若未进行页面切换则关闭页面
                                    if (session.player.openInventory.topInventory == event.inventory) {
                                        session.player.closeInventory()
                                    }
                                }
                            }
                        }
                    }
                    onClose {
                        if (!end) {
                            session.close(refuse = true)
                        }
                    }
                }
            }
        }
    }


    private fun ItemStack.buildItem(session: Session, reply: PlayerReply, index: Int): ItemStack {
        val icon = reply.root["icon"]?.toString()
        if (icon != null) {
            setIcon(icon)
        }
        val build = reply.build(session)
        return modifyMeta<ItemMeta> {
            setDisplayName(displayName.replace("index" to index.toString(), "player_side" to build, "playerSide" to build))
            lore = lore?.map { line ->
                val str = line.replace("index" to index.toString(), "player_side" to build, "playerSide" to build)
                KetherFunction.parse(str, sender = adaptPlayer(session.player), namespace = namespace)
            }
        }
    }

    private fun ItemStack.buildItem(session: Session, message: List<String>): ItemStack {
        val icon = session.conversation.root.getString("npc icon")
        if (icon != null) {
            setIcon(icon)
        }
        return modifyMeta<ItemMeta> {
            setDisplayName(displayName.toTitle(session))
            lore = lore?.flatMap { line ->
                val str = KetherFunction.parse(line, sender = adaptPlayer(session.player), namespace = namespace)
                if (str.contains("npc_side") || str.contains("npcSide")) {
                    message.map { str.replace("npc_side" to it, "npcSide" to it) }
                } else {
                    str.toTitle(session).asList()
                }
            }
        }
    }

    private fun String.toTitle(session: Session): String {
        val str = replace("title" to session.conversation.option.title.replace("name" to session.source.name)).colored()
        return KetherFunction.parse(str, sender = adaptPlayer(session.player), namespace = namespace)
    }

    private fun rows(player: Player, size: Int): CompletableFuture<Int> {
        return try {
            KetherShell.eval(settings.rows, sender = adaptPlayer(player), namespace = namespace) {
                extend(mapOf("size" to size))
            }.thenApply {
                it.cint
            }
        } catch (ex: Exception) {
            ex.printKetherErrorMessage()
            CompletableFuture.completedFuture(1)
        }
    }
}