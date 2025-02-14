package ink.ptms.chemdah.library.redlib

import ink.ptms.adyeshach.taboolib.common.TabooLibCommon
import ink.ptms.chemdah.core.quest.QuestDevelopment
import ink.ptms.chemdah.library.redlib.event.DataBlockMoveEvent
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.event.block.BlockPistonEvent
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import taboolib.common.LifeCycle
import taboolib.common.TabooLib
import taboolib.common.io.unzip
import taboolib.common.io.zip
import taboolib.common.platform.Awake
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getDataFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.CompletableFuture

/**
 * Artifex
 * ink.ptms.chemdah.library.redlib.BlockDataContainer
 *
 * @author 坏黑
 * @since 2022/5/25 18:18
 */
object BlockDataContainer {

    var manager: BlockDataManager? = null
        private set

    @Awake(LifeCycle.LOAD)
    private fun init() {
        // 在 ENABLE 之前初始化 BlockDataManager，否则脚本将无法正常调用
        TabooLib.registerLifeCycleTask(LifeCycle.ENABLE,1,) {
            if (QuestDevelopment.enableBlockContainer || QuestDevelopment.enableUniqueBlock) {
                val path = getDataFolder().toPath().resolve("blocks.db")
                // 加载不同模式的 BlockDataManager
                manager = when (QuestDevelopment.uniqueBlockMode) {
                    QuestDevelopment.UniqueBlockMode.AUTO -> BlockDataManager.createAuto(path, true, true)
                    QuestDevelopment.UniqueBlockMode.SQLITE -> BlockDataManager.createSQLite(path, true, true)
                    QuestDevelopment.UniqueBlockMode.PDC -> BlockDataManager.createPDC(true, true)
                }
            }
        }


    }

    @Awake(LifeCycle.DISABLE)
    private fun close() {
        manager?.saveAndClose();
    }

    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun onBlockMove(e: DataBlockMoveEvent) {
        if (e.parent is BlockPistonEvent && !e.block.isAllowPistonMove) {
            e.isCancelled = true
        }
    }
}

val blockDataContainer: BlockDataManager
    get() = BlockDataContainer.manager ?: error("BlockDataManager has not been initialized")

val World.dataContainers: List<DataBlock>
    get() = blockDataContainer.allLoaded.filter { it.chunkPosition.world.name == name }

var Block.isAllowPistonMove: Boolean
    set(it) {
        getDataContainer(create = true)!!["general.pistonMove"] = it
    }
    get() {
        return getDataContainer(create = false)?.getBoolean("general.pistonMove") == true
    }

fun Block.getDataContainer(create: Boolean = true): DataBlock? {
    return blockDataContainer.getDataBlock(this, create)
}

fun Block.getDataContainerAsync(create: Boolean = true): CompletableFuture<DataBlock> {
    return blockDataContainer.getDataBlockAsync(this, create)
}

fun Block.deleteDataContainer() {
    getDataContainer(create = false)?.let { blockDataContainer.remove(it) }
}

fun Block.hasDataContainer(): Boolean {
    return getDataContainer(create = false) != null
}

@Suppress("UNCHECKED_CAST")
fun ByteArray.deserializeToMap(zipped: Boolean = true): Map<String, Any> {
    ByteArrayInputStream(if (zipped) unzip() else this).use { byteArrayInputStream ->
        BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream ->
            return bukkitObjectInputStream.readObject() as Map<String, Any>
        }
    }
}

fun Map<String, Any>.serializeToByteArray(zipped: Boolean = true): ByteArray {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        BukkitObjectOutputStream(byteArrayOutputStream).use { bukkitObjectOutputStream ->
            bukkitObjectOutputStream.writeObject(this)
            val bytes = byteArrayOutputStream.toByteArray()
            return if (zipped) bytes.zip() else bytes
        }
    }
}