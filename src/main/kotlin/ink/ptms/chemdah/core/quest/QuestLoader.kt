package ink.ptms.chemdah.core.quest

import ink.ptms.chemdah.api.ChemdahAPI
import ink.ptms.chemdah.api.ChemdahAPI.chemdahProfile
import ink.ptms.chemdah.api.ChemdahAPI.isChemdahProfileLoaded
import ink.ptms.chemdah.api.event.collect.ObjectiveEvents
import ink.ptms.chemdah.api.event.collect.PluginReloadEvent
import ink.ptms.chemdah.api.event.collect.TemplateEvents
import ink.ptms.chemdah.core.PlayerProfile
import ink.ptms.chemdah.core.quest.addon.Addon
import ink.ptms.chemdah.core.quest.meta.Meta
import ink.ptms.chemdah.core.quest.meta.MetaType.Companion.type
import ink.ptms.chemdah.core.quest.objective.Abstract
import ink.ptms.chemdah.core.quest.objective.Dependency
import ink.ptms.chemdah.core.quest.objective.Objective
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.io.runningClasses
import taboolib.common.platform.Awake
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.*
import taboolib.library.reflex.ClassAnnotation
import taboolib.library.reflex.ClassStructure
import taboolib.library.reflex.ReflexClass
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.nms.MinecraftVersion
import java.io.File

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.QuestManager
 *
 * @author sky
 * @since 2021/3/2 1:13 上午
 */
object QuestLoader {
    fun isSubclassOfObjective(clazz:ReflexClass,superclazz:String): Boolean {
        // 如果当前类等于 Objective，或者它的父类链中有 Objective
        if (clazz.name?.contains(superclazz) == true) {
            return true
        }
        return clazz.superclass?.let { isSubclassOfObjective(clazz.superclass!!,superclazz) } ?: false
    }

    @Config("core/group.yml")
    lateinit var groupConf: Configuration
        private set
    @Suppress("UNCHECKED_CAST")
    @Awake(LifeCycle.ENABLE)
    fun registerAll() {

        val checkDependency = !File(getDataFolder(), "api.json").exists()
        runningClasses.forEach {
            println("======== 进入扫描"+it.name)
                if (it.name != null) {
                    if (!it.name!!.contains("ink.ptms.chemdah.core.quest")){
                        println("跳过"+it.name)
                        return@forEach
                    }
                }
            var has = isSubclassOfObjective(it,"Objective")
            if (has){
                println("======== 父类包含Objective")
            }

            if (isSubclassOfObjective(it,"Objective") && !it.hasAnnotation(Abstract::class.java)) {
                // 检测依赖环境
                if (checkDependency && it.hasAnnotation(Dependency::class.java)) {
                    val dependency = it.getAnnotation(Dependency::class.java)
                    // 不支持的扩展
                    if (dependency.property<String>("plugin") != "minecraft" && dependency.property<String>("plugin")
                            ?.let { it1 -> Bukkit.getPluginManager().getPlugin(it1) } == null) {
                        return@forEach
                    }
                    // 不支持的版本
                    if (MinecraftVersion.majorLegacy < dependency.property<Int>("version") ?: 10700) {
                        return@forEach
                    }
                }
                // 注册目标
                try {
                     // 获取目标实例并注册
                    val instance = it.getInstance()
                    if (instance is Objective<*>) {
                        instance.register()
                        println("${it.name} 成功注册")
                    }

                } catch (ignored: NoClassDefFoundError) {
                    // 例如版本问题导致的错误，无法被精确的判断
                    // ClassNotFoundException: com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
                }
            } else if (it.hasAnnotation(Id::class.java)) {
                val id = it.getAnnotation(Id::class.java).properties().get("id")
                when {
                    isSubclassOfObjective(it,"Meta") -> {
                        println("尝试注册meta"+it.name)
                        ChemdahAPI.questMeta[id.toString()] = it.toClass() as Class<out Meta<*>>
                    }
                    isSubclassOfObjective(it,"Addon") -> {
                        println("尝试注册addon"+it.name)
                        ChemdahAPI.questAddon[id.toString()] = it.toClass()  as Class<out Addon>
                    }
                }
            }
        }
    }

//    @Suppress("UNCHECKED_CAST")
//    @Awake(LifeCycle.ENABLE)
//    fun registerAll() {
//        val checkDependency = !File(getDataFolder(), "api.json").exists()
//
////        try {
////            for (reflexClass in runningClasses) {
////                name= reflexClass.name.toString()
////                println("################ 排除大法！！！！！！！！")
////                println(reflexClass.toClass().name)
////
////            }
////        }catch (e:Exception){
////            println(name+" to class出错")
////            e.printStackTrace();
////        }
//
//        try {
//            for (reflexClass in runningClasses) {
//                var name: String? = reflexClass.name
//                if (name != null) {
//                    if (name.contains("ink.ptms.chemdah.um.impl4")){
//                        println("跳过mm4")
//                        continue
//                    }
//                }
//
//            if (!reflexClass.hasAnnotation(Abstract::class.java))
//                println("###################"+name)
//            // 判断类是否是 Objective 类型，并且没有 Abstract 注解
//                try {
//                    var toClass = reflexClass.toClass()
//                } catch (e: Exception) {
//                    println(reflexClass.name + "BROKEN !!!!!!!!!!!!!!!!!!!!")
//                }
//
//            }
//        }catch (ep:Exception){
//            ep.printStackTrace()
//        }
//    }
////                    if (Objective::class.java.isAssignableFrom(reflexClass.toClass()) && !reflexClass.hasAnnotation(Abstract::class.java)) {
////
////                        // 检测依赖环境
////                        if (checkDependency && reflexClass.hasAnnotation(Dependency::class.java)) {
////                            // 获取 Dependency 注解
////                            val dependency = reflexClass.getAnnotation(Dependency::class.java) as? ClassAnnotation
////                            if (dependency!=null){
////                                val plugin = dependency.property<String>("plugin")
////                                println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@:" + plugin)
////                                val version = dependency.property<Int>("version") ?: 10700
////
////                                // 检查插件是否存在以及版本是否匹配
////                                if (plugin != "minecraft" && plugin?.let { Bukkit.getPluginManager().getPlugin(it) } == null) {
////                                    println("${reflexClass.toClass().name} 插件未找到或版本不匹配，跳过")
////                                    continue  // Use return@forEach to continue to next iteration
////                                }
////
////                                if (MinecraftVersion.majorLegacy < version) {
////                                    println("${reflexClass.toClass().name} 版本不兼容，跳过")
////                                    continue  // Use return@forEach to continue to next iteration
////                                }
////                            }
////                        }
////
////                        // 注册目标
////                        try {
////                            // 获取目标实例并注册
////                            val instance = reflexClass.getInstance()
////                            if (instance is Objective<*>) {
////                                instance.register()
////                                println("${reflexClass.name} 成功注册")
////                            }
////                        } catch (e: NoClassDefFoundError) {
////                            // 捕获 NoClassDefFoundError 错误
////                            println("${reflexClass.name} 类定义未找到，跳过")
////                            e.printStackTrace()  // 打印堆栈跟踪
////                        } catch (e: Exception) {
////                            // 捕获其他异常
////                            println("${reflexClass.name} 注册失败: ${e.message}")
////                            e.printStackTrace()  // 打印堆栈跟踪
////                        }
////                    } else if (reflexClass.hasAnnotation(Id::class.java)) {
////                        // 获取 Id 注解
////                        val idAnnotation = reflexClass.getAnnotation(Id::class.java)
////                        val id = idAnnotation.properties().get("id")
////                        when {
////                            Meta::class.java.isAssignableFrom(reflexClass.toClass()) -> {
////
////                                println("// 将 Meta 类型的类注册到 questMeta")
////                                ChemdahAPI.questMeta[id.toString()] = reflexClass.toClass() as Class<out Meta<*>>
////                            }
////
////                            Addon::class.java.isAssignableFrom(reflexClass.toClass()) -> {
////                                println("// 将 Meta 类型的类注册到 questMeta")
////                                ChemdahAPI.questAddon[id.toString()] = reflexClass.toClass() as Class<out Addon>
////                            }
////                        }
////                    }
////                } catch (e: Exception) {
////                    // 捕获整个循环中的异常并打印错误信息
////                    println("处理类 ${reflexClass.toClass().name} 时发生异常: ${e.message}")
////                    e.printStackTrace()  // 打印堆栈跟踪
////                }
////            }
////        } catch (e: Exception) {
////            // 捕获外层循环的异常并打印
////            println("处理注册过程时发生了异常: ${e.message}")
////            e.printStackTrace()
////        }
////    }



    /**
     * 注册任务目标
     */
    fun <T : Any> Objective<T>.register() {
        println("$$$$$$$$$$$$$$$$$$\$尝试注册"+name)
        ChemdahAPI.questObjective[name] = this
        // 是否注册监听器
        if (isListener) {
            // 对该条目注册独立监听器
            registerBukkitListener(event, EventPriority.values()[priority.ordinal], ignoreCancelled) { e ->
                // 回调事件
                ChemdahAPI.eventFactory.callObjectiveCall(this@register, e)
                // 若该事件被任何任务使用
                if (using) {
                    // 获取该监听器中的玩家对象
                    val player = handler.apply(e) ?: return@registerBukkitListener
                    if (player.isChemdahProfileLoaded) {
                        if (isAsync) {
                            submitAsync { handleEvent(player, e) }
                        } else {
                            handleEvent(player, e)
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理事件所对应的条目类型
     * 会进行完成检测
     *
     * @param player 玩家
     * @param event 事件
     */
    fun <T : Any> Objective<T>.handleEvent(player: Player, event: T) {
        if (player.isChemdahProfileLoaded) {
            player.chemdahProfile.also { profile ->
                // 通过事件获取所有正在进行的任务条目
                profile.tasks(this) { (quest, task) -> handleTask(profile, task, quest, event) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> handleTask(profile: PlayerProfile, task: Task, quest: Quest, event: T) {
        val objective: Objective<T> = task.objective as Objective<T>
        // 如果含有完成标记，则不在进行该条目
        if (objective.hasCompletedSignature(profile, task)) {
            return
        }
        // 判断条件并进行该条目
        if (ObjectiveEvents.Continue.Pre(objective, task, quest, profile).call()) {
            objective.checkCondition(profile, task, quest, event).thenAccept { cond ->
                if (cond) {
                    objective.onContinue(profile, task, quest, event)
                    task.agent(quest.profile, AgentType.TASK_CONTINUED)
                    ObjectiveEvents.Continue.Post(objective, task, quest, profile).call()
                    // 检查条目
                    objective.checkComplete(profile, task, quest).thenAccept {
                        // 检查任务
                        quest.checkCompleteFuture()
                    }
                }
            }
        }
    }

    /**
     * 刷新任务目标缓存
     */
    fun refreshCache() {
        ChemdahAPI.questObjective.forEach { it.value.using = false }
        ChemdahAPI.questTemplate.forEach { t -> t.value.taskMap.forEach { it.value.objective.using = true } }
    }

    @Awake(LifeCycle.ACTIVE)
    fun loadAll() {
        loadTemplate()
        loadTemplateGroup()
        PluginReloadEvent.Quest().call()
    }

    /**
     * 载入所有任务模板
     */
    fun loadTemplate() {
        val file = File(getDataFolder(), "core/quest")
        if (!file.exists()) {
            releaseResourceFile("core/quest/example.yml")
        }
        val templates = loadTemplate(file)
        ChemdahAPI.questTemplate.clear()
        ChemdahAPI.questTemplate.putAll(templates.map { it.id to it })
        refreshCache()
        info("${ChemdahAPI.questTemplate.size} templates loaded.")
        // 重复检查
        templates.groupBy { it.id }.forEach { (id, c) ->
            if (c.size > 1) {
                warning("${c.size} templates use duplicate id: $id")
            }
        }
    }

    /**
     * 载入任务模板
     */
    fun loadTemplate(file: File): List<Template> {
        return when {
            // 文件夹
            file.isDirectory -> file.listFiles()?.flatMap { loadTemplate(it) }?.toList() ?: emptyList()
            // 仅支持 yml 和 json
            file.extension == "yml" || file.extension == "json" -> {
                Configuration.loadFromFile(file).run {
                    getKeys(false).mapNotNull {
                        val section = getConfigurationSection(it)!!
                        if (TemplateEvents.Load(file, it, section).call()) {
                            Template(it, section)
                        } else {
                            null
                        }
                    }
                }
            }
            // 不支持的文件格式
            else -> emptyList()
        }
    }

    /**
     * 加载任务组
     */
    fun loadTemplateGroup() {
        ChemdahAPI.questTemplateGroup.clear()
        groupConf.getConfigurationSection("group")?.getKeys(false)?.forEach { group ->
            val groupList = HashSet<Template>()
            groupConf.getStringList("group.$group").forEach {
                when {
                    // 以 type: 开头的字符串
                    it.startsWith("type:") -> {
                        groupList += ChemdahAPI.questTemplate.values.filter { tem -> it.substringAfter("type:") in tem.type() }
                    }
                    // 其他
                    else -> {
                        val template = ChemdahAPI.getQuestTemplate(it)
                        if (template != null) {
                            groupList += template
                        }
                    }
                }
            }
            ChemdahAPI.questTemplateGroup[group] = TemplateGroup(group, groupList)
        }
    }
}