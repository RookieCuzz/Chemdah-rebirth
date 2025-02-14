package ink.ptms.chemdah.module.kether.compat


import com.sucy.skill.SkillAPI
import com.sucy.skill.api.player.PlayerData
import ink.ptms.chemdah.util.getBukkitPlayer
import taboolib.module.kether.*
import java.util.concurrent.CompletableFuture
import java.util.function.Function

/**
 * Chemdah
 * ink.ptms.chemdah.module.kether.compat.ActionSkillAPI
 *
 * @author Peng_Lx
 * @since 2021/5/4 16:03 下午
 */
class ActionSkillAPI {

    class Base(val action: Function<PlayerData, Any>) : ScriptAction<Any>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Any> {
            return CompletableFuture.completedFuture(action.apply(SkillAPI.getPlayerData(frame.getBukkitPlayer())))
        }
    }

    companion object {

        /**
         * skillapi class id
         * skillapi class name
         * skillapi level
         * skillapi cast skill1
         */
        @KetherParser(["skillapi"], shared = true)
        fun parser() = scriptParser {
            when (it.expects("class", "skills", "attribute", "level", "exp", "experience", "mana", "cast")) {
                "class" -> {
                    Base(when (it.expects("main", "size", "name", "group")) {
                        "main" -> { data -> data.mainClass }
                        "size" -> { data -> data.classes.size }
                        "name" -> { data -> data.mainClass.data.name }
                        "group" -> { data -> data.mainClass.data.group }
                        else -> error("out of case")
                    })
                }
                "skills" -> {
                    Base(when (it.expects("point")) {
                        "point" -> { data -> data.mainClass.points }
                        else -> error("out of case")
                    })
                }
                "attribute" -> {
                    val attribute = it.nextToken()
                    if (attribute == "point") {
                        Base { data -> data.attributePoints }
                    } else {
                        Base { data -> data.getAttribute(attribute) }
                    }
                }
                "level" -> {
                    try {
                        it.mark()
                        it.expect("maxed")
                        Base { data -> data.mainClass.isLevelMaxed }
                    } catch (ex: Exception) {
                        it.reset()
                        Base { data -> data.mainClass.level }
                    }
                }
                "experience", "exp" -> {
                    Base(when (it.expects("total", "required")) {
                        "total" -> { data -> data.mainClass.totalExp }
                        "require" -> { data -> data.mainClass.requiredExp }
                        else -> error("ouf of case")
                    })
                }
                "mana" -> {
                    Base { data -> data.mainClass.mana }
                }
                "cast" -> {
                    val skill = it.nextToken()
                    Base { data -> data.cast(skill) }
                }
                else -> error("out of case")
            }
        }
    }
}