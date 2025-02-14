package ink.ptms.chemdah.core.quest.selector

import ink.ptms.chemdah.core.bukkit.NMS
import ink.ptms.chemdah.core.quest.selector.Flags.Companion.matchType
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang
import taboolib.module.nms.MinecraftVersion
import taboolib.module.nms.MinecraftVersion.versionId

/**
 * Chemdah
 * ink.ptms.chemdah.util.Mats
 *
 * stone[power=5] —— Matches all blocks of type STONE and which have a power of 5
 * ^stone —— Matches all blocks of type name starts with "stone"
 * stone$ —— Matches all blocks of type name ends with "stone"
 * (stone) —— Matches all blocks of type name contains "stone"
 * %slabs% —— Matches all slabs
 * *[power=5] —— Matches everything and which have a power of 5
 *
 *
 * @author sky
 * @since 2021/3/2 5:41 下午
 */
class InferBlock(val mats: List<Block>) {

    fun isBlock(block: org.bukkit.block.Block): Boolean {
        val type = block.type.name.lowercase()
        println("~~~~~~~~~~~~~~~~~~~~~~~~~type is "+ type)
        val data = NMS.INSTANCE.getBlocKData(block)
        return mats.any { mat -> mat.matchFlags(type) && mat.matchBlockData(data) }
    }

    override fun toString(): String {
        return "InferBlock(mats=$mats)"
    }

    data class Block(val material: String, val flags: List<Flags>, val data: List<DataMatch>) {

        fun matchFlags(type: String): Boolean {
            return flags.any { it.match(type, material) }
        }

        fun matchBlockData(map: Map<String, Any>): Boolean {
            return data.all { it.check(map[it.key] ?: return false) }
        }
    }

    companion object {

        fun List<String>.toInferBlock() = InferBlock(map { it.toInferBlock() })

        @Suppress("DuplicatedCode")
        fun String.toInferBlock(): Block {
            var type: String
            val data = arrayListOf<DataMatch>()
            val flag = arrayListOf<Flags>()
            // 存在 BlockData 选择器
            if (indexOf('[') > -1 && endsWith(']')) {
                type = substring(0, indexOf('['))
                // 只有 1.13+ 才允许加载 BlockData 选择器
                if (versionId >= 11300) {
                    data += substring(indexOf('[') + 1, length - 1).split(',').map { DataMatch.fromString(it.trim()) }
                }
            } else {
                type = this
            }
            // 方块材质不支持命名空间
            if (contains("minecraft:")) {
                type = substringAfter("minecraft:")
                console().sendLang("console-infer-block-not-support", type)
            }
            return Block(type.matchType(flag), flag, data)
        }
    }
}