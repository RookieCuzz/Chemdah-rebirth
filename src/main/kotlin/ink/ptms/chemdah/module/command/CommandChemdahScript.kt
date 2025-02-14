package ink.ptms.chemdah.module.command

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.util.namespace
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.adaptCommandSender
import taboolib.expansion.createHelper
import taboolib.module.kether.Kether
import taboolib.module.kether.KetherShell
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.printKetherErrorMessage
import taboolib.platform.util.sendLang

@CommandHeader(name = "ChemdahScript", aliases = ["chs"], permission = "chemdah.command")
object CommandChemdahScript {

    val workspace by lazy { ChemdahAPI.workspace }

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val run = subCommand {
        // script
        dynamic(comment ="file") {
            suggestion<CommandSender> { _, _ ->
                workspace.scripts.map { it.value.id }
            }
            // viewer
            dynamic(comment ="viewer", optional = true) {
                suggestion<CommandSender> { _, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                // ver
                dynamic(comment ="args", optional = true) {
                    execute<CommandSender> { sender, ctx, argument ->
                        commandRun(sender, ctx.argument(-2), ctx.argument(-1), argument.split(" ").toTypedArray())
                    }
                }
                execute<CommandSender> { sender, ctx, argument ->
                    commandRun(sender, ctx.argument(-1), argument)
                }
            }
            execute<CommandSender> { sender, _, argument ->
                commandRun(sender, argument)
            }
        }
    }

    @CommandBody
    val stop = subCommand {
        dynamic(comment ="file", optional = true) {
            suggestion<CommandSender> { _, _ ->
                workspace.scripts.map { it.value.id }
            }
            execute<CommandSender> { sender, _, argument ->
                val script = workspace.getRunningScript().filter { it.quest.id == argument }
                if (script.isNotEmpty()) {
                    script.forEach { workspace.terminateScript(it) }
                } else {
                    sender.sendLang("command-script-not-running")
                }
            }
        }
        execute<CommandSender> { _, _, _ ->
            workspace.getRunningScript().forEach { workspace.terminateScript(it) }
        }
    }

    @CommandBody
    val list = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendLang("command-script-list-all",
                workspace.scripts.map { it.value.id }.joinToString(", "),
                workspace.getRunningScript().joinToString(", ") { it.id }
            )
        }
    }

    @CommandBody
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            workspace.cancelAll()
            workspace.loadAll()
            sender.sendLang("command-script-reload-all")
        }
    }

    @CommandBody
    val debug = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage("§c[System] §7RegisteredActions:")
            Kether.scriptRegistry.registeredNamespace.forEach {
                sender.sendMessage("§c[System] §7  ${it}: §r${Kether.scriptRegistry.getRegisteredActions(it)}")
            }
        }
    }

    @CommandBody
    val invoke = subCommand {
        dynamic(comment ="script") {
            execute<CommandSender> { sender, _, argument ->
                try {
                    KetherShell.eval(argument, namespace = namespace, sender = adaptCommandSender(sender)).thenApply { v ->
                        sender.sendResult(v)
                    }
                } catch (ex: Throwable) {
                    sender.sendMessage("§c[System] §7Error: ${ex.message}")
                    ex.printKetherErrorMessage()
                }
            }
        }
    }

    @CommandBody
    val `invoke-now` = subCommand {
        dynamic(comment ="script") {
            execute<CommandSender> { sender, _, argument ->
                try {
                    sender.sendResult(KetherShell.eval(argument, namespace = namespace, sender = adaptCommandSender(sender)).getNow(null))
                } catch (ex: Throwable) {
                    sender.sendMessage("§c[System] §7Error: ${ex.message}")
                    ex.printKetherErrorMessage()
                }
            }
        }
    }

    @CommandBody
    val `invoke-wait` = subCommand {
        dynamic(comment ="script") {
            execute<CommandSender> { sender, _, argument ->
                try {
                    sender.sendResult(KetherShell.eval(argument, namespace = namespace, sender = adaptCommandSender(sender)).get())
                } catch (ex: Throwable) {
                    sender.sendMessage("§c[System] §7Error: ${ex.message}")
                    ex.printKetherErrorMessage()
                }
            }
        }
    }

    internal fun CommandSender.sendResult(v: Any?) {
        try {
            Class.forName(v.toString().substringBefore('$'))
            sendMessage("§c[System] §7Result: §f${v!!.javaClass.simpleName} §7(Java Object)")
        } catch (_: Throwable) {
            sendMessage("§c[System] §7Result: §f$v")
        }
    }

    internal fun commandRun(sender: CommandSender, file: String, viewer: String? = null, args: Array<String> = emptyArray()) {
        val script = workspace.scripts[file]
        if (script != null) {
            val ctx = ScriptContext.create(script) {
                if (viewer != null) {
                    val player = Bukkit.getPlayerExact(viewer)
                    if (player != null) {
                        this.sender = adaptCommandSender(player)
                    }
                }
                var i = 0
                while (i < args.size) {
                    set("arg${i}", args[i])
                    i++
                }
            }
            try {
                workspace.runScript(file, ctx)
            } catch (t: Throwable) {
                sender.sendLang("command-script-error", t.localizedMessage)
                t.printKetherErrorMessage()
            }
        } else {
            sender.sendLang("command-script-not-found")
        }
    }
}