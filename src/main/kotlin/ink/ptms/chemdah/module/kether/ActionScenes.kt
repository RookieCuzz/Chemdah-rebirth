package ink.ptms.chemdah.module.kether

import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.adyeshach.common.entity.EntityTypes
import ink.ptms.adyeshach.common.entity.type.AdyFallingBlock
import ink.ptms.chemdah.api.event.collect.PlayerEvents
import ink.ptms.chemdah.module.scenes.ScenesBlockData
import ink.ptms.chemdah.module.scenes.ScenesSystem
import ink.ptms.chemdah.util.getBukkitPlayer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common.util.Vector
import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.library.xseries.parseToMaterial
import taboolib.module.kether.*
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.PacketReceiveEvent
import taboolib.platform.util.toBukkitLocation
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.ActionScenes
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
@Suppress("DuplicatedCode")
class ActionScenes {

    class ScenesBlockSet0(
        val location: ParsedAction<*>,
        val material: Material,
        val data: Byte = 0,
        val falling: Boolean,
        val solid: Boolean
    ) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            return frame.newFrame(location).run<Location>().thenAccept { location ->
                if (falling) {
                    frame.getBukkitPlayer().createScenesFallingBlock(location, material, data, solid)
                } else {
                    frame.getBukkitPlayer().createScenesBlock(location, material, data)
                }
            }
        }

        override fun toString(): String {
            return "ScenesBlockSet0(location=$location, material=$material, data=$data, falling=$falling, solid=$solid)"
        }
    }

    class ScenesBlockSet1(
        val location: ParsedAction<*>,
        val copy: ParsedAction<*>,
        val falling: Boolean,
        val solid: Boolean
    ) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            return frame.newFrame(location).run<Location>().thenAccept { location ->
                frame.newFrame(copy).run<Location>().thenAccept { copy ->
                    val block = copy.block
                    if (falling) {
                        frame.getBukkitPlayer().createScenesFallingBlock(location, block.type, block.data, solid)
                    } else {
                        frame.getBukkitPlayer().createScenesBlock(location, block.type, block.data)
                    }
                }
            }
        }

        override fun toString(): String {
            return "ScenesBlockSet1(location=$location, copy=$copy, falling=$falling, solid=$solid)"
        }
    }

    companion object  {

        /**
         * scenes set [falling [solid]] {token} to {location}
         * scenes copy [falling [solid]] {location} to {location}
         * scenes file {token} [(state|cancel) {int}]
         * scenes reset {location}
         */
        @KetherParser(["scenes"], shared = true)
        fun parser() = scriptParser {
            it.switch {
                case("set") {
                    val falling = try {
                        it.mark()
                        it.expect("falling")
                        true
                    } catch (ex: Exception) {
                        it.reset()
                        false
                    }
                    val solid = if (falling) {
                        try {
                            it.mark()
                            it.expect("solid")
                            true
                        } catch (ex: Exception) {
                            it.reset()
                            false
                        }
                    } else false
                    val block = it.nextToken()
                    val material = block.split(":")[0].parseToMaterial()
                    val data = Coerce.toByte(block.split(":").getOrNull(1))
                    it.expect("to")
                    ScenesBlockSet0(it.next(ArgTypes.ACTION), material, data, falling, solid)
                }
                case("copy") {
                    val falling = try {
                        it.mark()
                        it.expect("falling")
                        true
                    } catch (ex: Exception) {
                        it.reset()
                        false
                    }
                    val solid = if (falling) {
                        try {
                            it.mark()
                            it.expect("solid")
                            true
                        } catch (ex: Exception) {
                            it.reset()
                            false
                        }
                    } else false
                    val location = it.next(ArgTypes.ACTION)
                    it.expect("to")
                    ScenesBlockSet1(it.next(ArgTypes.ACTION), location, falling, solid)
                }
                case("file") {
                    val file = it.nextToken()
                    when (it.expects("state", "cancel")) {
                        "state" -> {
                            val state = it.nextInt()
                            actionNow {
                                ScenesSystem.scenesMap[file]?.state?.firstOrNull { s -> s.index == state }?.send(getBukkitPlayer())
                            }
                        }
                        "cancel" -> {
                            val state = it.nextInt()
                            actionNow {
                                ScenesSystem.scenesMap[file]?.state?.firstOrNull { s -> s.index == state }?.cancel(getBukkitPlayer())
                            }
                        }
                        else -> error("out of case")
                    }
                }
                case("reset") {
                    val loc = it.next(ArgTypes.ACTION)
                    actionNow {
                        newFrame(loc).run<Location>().thenAccept { loc ->
                            getBukkitPlayer().removeScenesBlock(loc)
                        }
                    }
                }
            }
        }

        private val scenesBlocks = ConcurrentHashMap<String, MutableMap<String, MutableMap<Vector, ScenesBlockData>>>()

        @Schedule(period = 40, async = true)
        fun updateScenesBlock40() {
            Bukkit.getOnlinePlayers().forEach { it.updateScenesBlock() }
        }

        @SubscribeEvent
        private fun onPacketReceive(e: PacketReceiveEvent) {
            if (e.packet.name == "PacketPlayInUseItem") {
                val pos = if (MinecraftVersion.isUniversal) {
                    e.packet.read<Any>("a/blockPos")!!
                } else if (MinecraftVersion.majorLegacy >= 11400) {
                    e.packet.read<Any>("a/c")!!
                } else {
                    e.packet.read<Any>("a")!!
                }
                // 1.18 Supported
                val vec = if (MinecraftVersion.major >= 10) {
                    Vector(pos.getProperty<Number>("x")!!.toInt(), pos.getProperty<Number>("y")!!.toInt(), pos.getProperty<Number>("z")!!.toInt())
                } else {
                    // 在老版本中字段名称为 a, b, c
                    Vector(pos.invokeMethod<Number>("getX")!!.toInt(), pos.invokeMethod<Number>("getY")!!.toInt(), pos.invokeMethod<Number>("getZ")!!.toInt())
                }
                val data = scenesBlocks[e.player.name]?.get(e.player.world.name)?.get(vec) ?: return
                PlayerEvents.ScenesBlockInteract(e.player, data).call()
                e.isCancelled = true
            }
            if (e.packet.name == "PacketPlayInBlockDig") {
                val pos = e.packet.read<Any>("a") ?: return
                // 1.18 Supported
                val vec = if (MinecraftVersion.major >= 10) {
                    Vector(pos.getProperty<Number>("x")!!.toInt(), pos.getProperty<Number>("y")!!.toInt(), pos.getProperty<Number>("z")!!.toInt())
                } else {
                    Vector(pos.invokeMethod<Number>("getX")!!.toInt(), pos.invokeMethod<Number>("getY")!!.toInt(), pos.invokeMethod<Number>("getZ")!!.toInt())
                }
                val data = scenesBlocks[e.player.name]?.get(e.player.world.name)?.get(vec) ?: return
                if (e.packet.read<Any>("c").toString() == "STOP_DESTROY_BLOCK") {
                    if (!PlayerEvents.ScenesBlockBreak(e.player, data).call()) {
                        submit(delay = 1) {
                            e.player.createScenesBlock(vec.toLocation(e.player.world.name).toBukkitLocation(), data.material, data.data)
                        }
                        e.isCancelled = true
                    } else {
                        e.player.removeScenesBlock(vec.toLocation(e.player.world.name).toBukkitLocation())
                    }
                }
            }
        }

        @SubscribeEvent
        private fun onTeleport(e: PlayerTeleportEvent) {
            submit(delay = 20) {
                e.player.updateScenesBlock()
            }
        }

        @SubscribeEvent
        private fun onChangeWorld(e: PlayerChangedWorldEvent) {
            submit(delay = 20) {
                e.player.updateScenesBlock()
            }
        }

        @SubscribeEvent
        private fun onReleased(e: PlayerEvents.Released) {
            scenesBlocks.remove(e.player.name)
        }

        fun Player.removeScenesBlock(location: Location) {
            scenesBlocks[name]?.get(world.name)?.remove(Vector(location.x, location.y, location.z))
            if (MinecraftVersion.majorLegacy >= 11300) {
                sendBlockChange(location, location.block.blockData)
            } else {
                sendBlockChange(location, location.block.type, location.block.data)
            }
        }

        fun Player.createScenesBlock(location: Location, material: Material, data: Byte = 0) {
            val worlds = scenesBlocks.computeIfAbsent(name) { ConcurrentHashMap() }
            val blocks = worlds.computeIfAbsent(world.name) { ConcurrentHashMap() }
            blocks[Vector(location.x, location.y, location.z)] = ScenesBlockData(material, data)
            if (MinecraftVersion.majorLegacy >= 11300) {
                sendBlockChange(location, material.createBlockData())
            } else {
                sendBlockChange(location, material, data)
            }
        }

        fun Player.updateScenesBlock() {
            scenesBlocks[name]?.get(world.name)?.forEach {
                val loc = it.key.toLocation(world.name).toBukkitLocation()
                if (loc.distance(location) < 128) {
                    if (MinecraftVersion.majorLegacy >= 11300) {
                        sendBlockChange(loc, it.value.material.createBlockData())
                    } else {
                        sendBlockChange(loc, it.value.material, it.value.data)
                    }
                }
            }
        }

        fun Player.createScenesFallingBlock(location: Location, material: Material, data: Byte = 0, toSolid: Boolean = false) {
            val manager = AdyeshachAPI.getEntityManagerPrivateTemporary(this)
            val npc = manager.create(EntityTypes.FALLING_BLOCK, location) {
                (it as AdyFallingBlock).setMaterial(material, data)
            }
            npc.setTag("chemdah:scenes", if (toSolid) "SOLID" else "NONE")
            npc.registerController(AdyeshachAPI.getControllerGenerator("Gravity")!!.generator.apply(npc))
        }

//        @SubscribeEvent
//        private fun onTick(e: AdyeshachEntityTickEvent) {
//            val entity = e.entity
//            val manager = entity.manager ?: return
//            if (!manager.isPublic()
//                && entity is AdyFallingBlock
//                && entity.hasTag("chemdah:scenes")
//                && entity.getController().any { it is GeneralGravity }
//                && entity.isControllerOnGround()
//            ) {
//                if (entity.getTag("chemdah:scenes") == "SOLID") {
//                    entity.forViewers {
//                        it.createScenesBlock(entity.getLocation(), entity.material, entity.data)
//                    }
//                }
//                entity.removeTag("chemdah:scenes")
//                entity.delete()
//            }
//        }
    }
}