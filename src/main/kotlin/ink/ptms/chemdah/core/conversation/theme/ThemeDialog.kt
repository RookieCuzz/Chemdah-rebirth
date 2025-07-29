package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.api.ChemdahAPI.conversationSession
import ink.ptms.chemdah.api.event.collect.ConversationEvents
import ink.ptms.chemdah.core.conversation.ConversationManager
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.core.quest.QuestDevelopment
import ink.ptms.chemdah.core.quest.QuestDevelopment.releaseTransmit
import ink.ptms.chemdah.util.thenTrue
import kr.toxicity.hud.api.BetterHudAPI
import kr.toxicity.hud.api.bukkit.event.CustomPopupEvent
import kr.toxicity.hud.api.bukkit.update.BukkitEventUpdateEvent
import kr.toxicity.hud.api.player.HudPlayer
import kr.toxicity.hud.api.popup.Popup
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.scheduler.BukkitRunnable

import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.adaptPlayer
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.module.chat.colored
import taboolib.module.nms.nmsClass
import taboolib.module.nms.sendPacket
import taboolib.platform.util.toProxyLocation
import java.util.concurrent.CompletableFuture
import org.bukkit.event.block.Action
import taboolib.common.util.Vector
import taboolib.library.xseries.XParticle
import java.util.*

object ThemeDialog  : Theme<ThemeDialogSetting>(){
    override fun createConfig(): ThemeDialogSetting {
        return ThemeDialogSetting(ConversationManager.conf.getConfigurationSection("theme-dialog-bar")!!)
    }
    override fun onBegin(session: Session): CompletableFuture<Void> {
        if (session.conversation.noFlag("NO_EFFECT:PARTICLE")) {

            adaptPlayer(session.player).sendParticle(  XParticle.CLOUD.name, session.origin.clone().add(0.0, 0.5, 0.0).toProxyLocation(),
                Vector(1.0, 0.0, 1.0),1,
                0.0,null)
//6.2.3更新
//            ProxyParticle.CLOUD.sendTo(adaptPlayer(session.player), session.origin.clone().add(0.0, 0.5, 0.0).toProxyLocation())
        }
        return super.onBegin(session)
    }

    override fun onReset(session: Session): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        session.conversation.playerSide.checked(session).thenApply {
            // 只有按键触发才存在默认回复修正
            if (!ThemeChat.settings.useScroll) {
                session.playerSide = it.getOrNull(session.player.inventory.heldItemSlot.coerceAtMost(it.size - 1))
            } else {
                session.playerSide = it.getOrNull(0)
            }
            future.complete(null)
        }
        return future
    }

    //展示
    override fun onDisplay(session: Session, message: List<String>, canReply: Boolean): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        // 延迟
        var d = 0L
        // 取消
        var cancel = false
        // 标记为 NPC 正在发言
        session.npcTalking = true
        // 文本动画 —— 打印机效果
        var stb:StringBuilder=StringBuilder()
        val messageAnimated = message.colored().forEach() { if (ThemeChat.settings.animation){
            stb.append(it)

        } else listOf(it) }

        var messageAnimation = splitMessageLine(stb.toString())
        val dialogEvent = CustomPopupEvent(session.player, "test")
        val hudPlayer = BetterHudAPI.inst().playerManager.getHudPlayer(session.player.uniqueId)

        dialogEvent.variables["yosinmessage"] = " "
        dialogEvent.variables["yosinnpcavatar"] = "\uDAC0\uDC01"
        dialogEvent.variables["yosinnpcname"] = "测试NPC"
        var title = session.conversation.option.title
        if (title.contains("@")){
            var nameX = title.split("@");
            dialogEvent.variables["yosinnpcname"]= nameX[0]
            dialogEvent.variables["yosinnpcavatar"] = nameX[1]
        }
        val updateEvent = BukkitEventUpdateEvent(dialogEvent, "test1")
        val dialogPopup = BetterHudAPI.inst().popupManager.getPopup("dialog_popup")
        session.dialogContent=dialogEvent
        session.updateEvent = updateEvent
        session.hudPlayer= hudPlayer;
//        println("debug 玩家回复")
        var playerSide = session.playerSide
//        println(playerSide!!.text)
        val playerReplyForDisplay = session.playerReplyForDisplay
        session.npcTalking = true
//        playerReplyForDisplay.forEach(){
//            println(it)
//        }
        sendDialogBarWithDelay(hudPlayer!!,message,
            messageAnimation, dialogPopup!!,
            dialogEvent, updateEvent,session,future)

//        }

        return future
    }

    /**
     * 取消这个行为会出现客户端显示不同步的错误
     * 以及 mc 无法重复切换相同物品栏
     */
    @Suppress("SpellCheckingInspection")
    @SubscribeEvent(EventPriority.HIGHEST, ignoreCancelled = true)
    private fun onItemHeld(e: PlayerItemHeldEvent) {
        val session = e.player.conversationSession ?: return
//        println("切换手")
        if (session.conversation.option.theme == "dialog") {
//            println("进入dialog if")
            if (session.npcTalking) {
//                println("进入npcTalking if")
                // 是否不允许玩家跳过对话演出效果
                if (session.conversation.hasFlag("NO_SKIP", "FORCE_DISPLAY")) {
                    e.isCancelled = true
//                    println("结束了！")
                    return
                }
                session.npcTalking = false
            }
//            println("进入replies 打印")
            val replies = session.playerReplyForDisplay
//            println("进入replies size:" + replies.size)
//            replies.forEach {
////                println(it.text)
//            }
            if (replies.isNotEmpty()) {
//                println("进入replies if")
                val index = replies.indexOf(session.playerSide)
                var select: Int
                // 使用滚轮
                if (settings.useScroll) {
                    if (e.newSlot < e.previousSlot) { // 修改比较逻辑
                        select = index + 1 // 向下滚动
                        if (select >= replies.size) {
                            select = 0
                        }
                    } else {
                        select = index - 1 // 向上滚动
                        if (select < 0) {
                            select = replies.size - 1
                        }
                    }
                    // 修复客户端显示不同步的问题
                    try {
                        e.player.sendPacket(nmsClass("PacketPlayOutHeldItemSlot").invokeConstructor(e.previousSlot))
                    } catch (ignored: Throwable) {
                    }
                    e.isCancelled = true
                } else {
                    select = e.newSlot.coerceAtMost(replies.size - 1)
                }
                if (select != index) {
                    session.playerSide = replies[select]
                    settings.playSelectSound(session)
                    val dialogPopup = session.dialogPopup
                    val dialogContent = session.dialogContent

                    dialogContent!!.getVariables().replace("selectbuttom", "select" + (select + 1))
                    dialogPopup!!.show(session.updateEvent!!, session.hudPlayer!!)
                }
            }
        }
    }

    private fun paddingToSpaceToLine(messageLine: List<String>): List<String> {
//        return messageLine.map {
//            it.padEnd(30, ' ')
//        }
        return messageLine;
    }


    @SubscribeEvent
    private fun onClosed(e: ConversationEvents.Closed) {

        val session = e.session
      if(session.conversation.option.theme != "dialog"){
          return;
      }
        e.session.dialogPopup!!.hide(e.session.hudPlayer!!)
        e.session.dialogPopup.remove(e.session.hudPlayer!!)
        if (e.refuse && !e.session.npcTalking && QuestDevelopment.enableMessageTransmit) {

            e.session.player.releaseTransmit()
        }
    }
//    @SubscribeEvent
//    private fun beginClose(e: ConversationEvents.Close) {
//        println("begin关闭对话")
//        if (e.refuse && !e.session.npcTalking && QuestDevelopment.enableMessageTransmit) {
//            e.session.dialogPopup!!.hide(e.session.hudPlayer!!)
//            e.session.player.releaseTransmit()
//        }
//    }
    fun sendDialogBarWithDelay(
    hudPlayer: HudPlayer,
    messages: List<String>,
    messageCharOneByOne: List<String>,
    dialogPopup: Popup,
    dialogEvent: CustomPopupEvent,
    updateEvent: BukkitEventUpdateEvent,
    session: Session,
    future: CompletableFuture<Void>
    ) {
        //玩家选项
        val replies = session.conversation.playerSide.checked(session).get()

        //换行填充
        val paddingMessages =paddingToSpaceToLine(messages)

        // 判断 messages 的行数，决定是否需要分n次对话展示（目前一次最多展示两行）
        val messageBatches: List<List<String>> = if (paddingMessages.size > 2) {
            // 按每组最多两行进行分批
            messages.chunked(2)
        } else {
            listOf(messages)
        }



    if (messageBatches.isNotEmpty()) {
        object : BukkitRunnable() {
            private var charIndex = 0
            private var currentBatch = 0
            private var currentMessage = 0
            private var tempTypeWriteLine: List<MessageLine> = getMessage2Line(messageBatches[currentBatch])
            private  var waitTick=0
            override fun run() {
                if (session.isClosed) {
                    cancel()
                    return
                }

                if (waitTick > 0) {
                    waitTick--
                    return
                }

                var messageX = tempTypeWriteLine.get(currentMessage)
                // 若当前字符为空格 则跳过空格
                while (messageX.currentCharisSpace()) {
                    messageX.stepUp()
                }
                //完成一行消息
                if (!messageX.isDown()) {
                    // 显示当前字符

                    //分行逻辑  两行消息
                    if (currentMessage==0){
                        dialogEvent.variables["yosinmessage1"] = messageX.getCurrent()
                        dialogEvent.variables["yosinmessage2"] = ""
                    }else{
                        dialogEvent.variables["yosinmessage1"] = tempTypeWriteLine.get(0).getComplate()
                        dialogEvent.variables["yosinmessage2"] = messageX.getCurrent()
                    }

                    dialogEvent.variables["speaking"] = ".".repeat(charIndex % 6)
                    dialogPopup.show(updateEvent, hudPlayer)
                    charIndex++
                    messageX.stepUp()
                } else {

                    //还有一行
                    if (tempTypeWriteLine.size!=1&&currentMessage<tempTypeWriteLine.size-1){
                        currentMessage++
                        return
                    }
                    currentBatch++
                    if (currentBatch < messageBatches.size) {
                        // 当前批次处理完，准备下一个批次
                        waitTick = 30
                        tempTypeWriteLine = getMessage2Line(messageBatches[currentBatch])
                        currentMessage = 0
                        charIndex = 0

                    } else {
                        // 所有批次处理完，显示选项
                        for (i in replies.indices) {
                            dialogEvent.variables["yosinvar${i + 1}"] = replies[i].text.colored()
                        }
                        if (replies.isNotEmpty()) {
                            dialogEvent.variables["selectbuttom"] = "select1"
                            session.playerSide = replies[0]
                        }
                        dialogPopup.show(updateEvent, hudPlayer)
                        session.playerReplyForDisplay.clear()
                        session.playerReplyForDisplay.addAll(replies)
                        future.complete(null)
                        future.thenAccept { session.npcTalking = false }
                        cancel()
                    }
                }
            }
        }.runTaskTimer(Chemdah.plugin, 0, 1L)
    }





//        if (messageBatches.isNotEmpty()) {
//            object : BukkitRunnable() {
//                private var charIndex = 0
//                private var batchs: List<List<String>> = messageBatches
//                private var currentBatch:Int =0;
//                //第一个
//                private var tempTypeWriteLine:List<String> = splitMessageLine(messageBatches.get(0))
//                override fun run() {
//                    //异步初始化处理
//                    if (charIndex >= tempTypeWriteLine.size && currentBatch >=messageBatches.size){
//
//                    }
//
//
//                    if (charIndex < tempTypeWriteLine.size) {
//                        if (session.isClosed) {
//                            cancel(); return
//                        }
//
//                        // 打字完成
//                        if (!session.npcTalking) {
//                            for (i in replies.indices) {
//                                dialogEvent.variables["yosinvar${i + 1}"] = replies[i].text.colored()
//                            }
//                            if (replies.isNotEmpty()) {
//                                dialogEvent.variables["selectbuttom"] = "select1"
//                                session.playerSide = replies[0]
//                            }
//
//                            dialogEvent.variables[" "] = tempTypeWriteLine.last()
//                            dialogPopup.show(updateEvent, hudPlayer)
//                            session.playerReplyForDisplay.clear()
//                            session.playerReplyForDisplay.addAll(replies)
//                            future.complete(null)
//                            future.thenAccept { session.npcTalking = false }
//                            cancel()
//                            return
//                        }
//
//                        while (charIndex < tempTypeWriteLine.size &&
//                            tempTypeWriteLine[charIndex].last().equals(' ', true)) {
//                            charIndex++
//                        }
//
//                        if (charIndex < tempTypeWriteLine.size) {
//                            dialogEvent.variables["yosinmessage"] = tempTypeWriteLine[charIndex]
//                            dialogEvent.variables["speaking"] = ".".repeat(charIndex % 6)
//                            dialogPopup.show(updateEvent, hudPlayer)
//                            charIndex++
//                        }
//                    } else {
//                        // 只有在最后一批才展示选项
//                        if (currentBatch == messageBatches.lastIndex) {
//                            for (i in replies.indices) {
//                                dialogEvent.variables["yosinvar${i + 1}"] = replies[i].text.colored()
//                            }
//                            if (replies.isNotEmpty()) {
//                                dialogEvent.variables["selectbuttom"] = "select1"
//                                session.playerSide = replies[0]
//                            }
//
//                            dialogPopup.show(updateEvent,hudPlayer)
//                            session.playerReplyForDisplay.clear()
//                            session.playerReplyForDisplay.addAll(replies)
//                            future.complete(null)
//                            future.thenAccept { session.npcTalking = false }
//                        }
//
//
//                    }
//
//                    if(){cancel()}
//                }
//
//
//
//            }.runTaskTimerAsynchronously(Chemdah.plugin, 0, 1L)


        }




////        val hudPlayer = BetterHudAPI.inst().playerManager.getHudPlayer(player.uniqueId)
//        // 确保 hudPlayer 和 strings 非空
//        if (messageCharOneByOne.isNotEmpty()) {
//            object : BukkitRunnable() {
//                private var index = 0  // 用来记录当前发送的消息的索引
//
//                override fun run() {
//                    if (index < messageCharOneByOne.size) {
//                        if (session.isClosed){
//                            cancel()
//                            return
//                        }
//                        //如果跳过打字机效果
//                        if(!session.npcTalking){
//                            dialogEvent.variables[" "] = messageCharOneByOne[messageCharOneByOne.size-1]
//                            // 所有消息发送完毕后，停止任务
//                            for (i in 0 until replies.size) {
//                                println("yosinvar"+(i+1)+"  :" +replies[i].text)
//                                dialogEvent.variables["yosinvar"+(i+1)] = replies[i].text
//                            }
//                            if (replies.isNotEmpty()){
//                                println("刷新选择按钮")
//                                dialogEvent.variables.put("selectbuttom", "select1")
//                                session.playerSide = replies[0]
//                            }
//                            println("文本为: "+dialogEvent.variables.get("yosinvar"+1))
//                            dialogPopup.show(updateEvent, hudPlayer)
//                            session.playerReplyForDisplay.clear()
//                            session.playerReplyForDisplay.addAll(replies)
//                            future.complete(null)
//                            // 消息发送结束后解除标签
//                            future.thenAccept {
//                                session.npcTalking = false
//                            }
//                            cancel()
//                            return
//                        }
//
//                        // 对于空格直接跳过,
//                        while(messageCharOneByOne[index].last().equals(' ',true)){
//                            // 增加索引，指向下一个消息
//                            index++
//                        }
//                        dialogEvent.variables["yosinmessage"] = messageCharOneByOne[index]
//                        dialogEvent.variables["speaking"] = ".".repeat(index%6)
//                        // 更新弹窗（Popup）
//                        val show = dialogPopup.show(updateEvent, hudPlayer)
//
//                        // 增加索引，指向下一个消息
//                        index++
//                    } else {
//                        // 所有消息发送完毕后，停止任务
//                        for (i in 0 until replies.size) {
//                            println("yosinvar"+(i+1)+"  :" +replies[i].text)
//                            dialogEvent.variables["yosinvar"+(i+1)] = replies[i].text
//                        }
//                        if (replies.isNotEmpty()){
//                            println("刷新选择按钮")
//                            dialogEvent.variables.put("selectbuttom", "select1")
//                            session.playerSide = replies[0]
//                        }
//                        println("文本为: "+dialogEvent.variables.get("yosinvar"+1))
//                        dialogPopup.show(updateEvent, hudPlayer)
//                        session.playerReplyForDisplay.clear()
//                        session.playerReplyForDisplay.addAll(replies)
//                        future.complete(null)
//                        // 消息发送结束后解除标签
//                        future.thenAccept {
//                            session.npcTalking = false
//                        }
//
//                        cancel()
//                    }
//                }
//            }.runTaskTimerAsynchronously(Chemdah.plugin, 0L, 1L)  // 每隔 3 tick 发送一个消息
//        }
//    }






    //按Q退出
    @SubscribeEvent
    private fun onQ(e:PlayerDropItemEvent){
        val session = e.player.conversationSession ?: return
        if (session.conversation.option.theme == "dialog") {
            session.dialogPopup!!.hide(session.hudPlayer!!)
            session.dialogPopup!!.remove(session.hudPlayer!!)
            e.isCancelled = true
            session.npcTalking = false
            session.isClosed=true
            println("尝试关闭对话")
            session.close(true)
        }
    }
    //左键 确定选项
    @SubscribeEvent
    private fun onClick(e: PlayerInteractEvent) {
        val session = e.player.conversationSession ?: return
        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK){
            if (session.conversation.option.theme == "dialog") {
                e.isCancelled = true
                if (session.npcTalking) {
                    return
                    // 是否不允许玩家跳过对话演出效果
//                if (session.conversation.hasFlag("NO_SKIP", "FORCE_DISPLAY")) {

//                }
//                session.npcTalking = false
                } else {
                    session.playerSide?.run { check(session).thenTrue { select(session) } }
                }
            }
        }

    }
    //按F跳过对话
    @SubscribeEvent
    private fun onSwap(e: PlayerSwapHandItemsEvent) {
        val session = e.player.conversationSession ?: return
        if (session.conversation.option.theme == "dialog") {
            e.isCancelled = true
            session.npcTalking = false
        }
    }

//    @SubscribeEvent
//    private fun onSwap(e: PlayerSwapHandItemsEvent) {
//        val session = e.player.conversationSession ?: return
//        if (session.conversation.option.theme == "dialog") {
//            e.isCancelled = true
//            if (session.npcTalking) {
//                return
//                // 是否不允许玩家跳过对话演出效果
////                if (session.conversation.hasFlag("NO_SKIP", "FORCE_DISPLAY")) {
//
////                }
////                session.npcTalking = false
//            } else {
//                session.playerSide?.run { check(session).thenTrue { select(session) } }
//            }
//        }
//    }

    //分割字符串  营造打字机效果
    fun splitMessageLine(message: String): List<String> {
        val src = mutableListOf<String>()  // 使用 MutableList 来允许添加元素
        for (i in 0 until message.length) {
            src.add(message.substring(0, i + 1))  // 从第0个字符到当前字符的子字符串
        }
        return src  // 返回包含前缀的列表
    }
    //营造打字机效果  两行文本
    //分别进行构造按字符递增的string
    //


    fun getMessage2Line(messages: List<String>): List<MessageLine>{
        val src = mutableListOf<MessageLine>()  // 使用 MutableList 来允许添加元素
        for (message in messages){
            src.add(MessageLine(message))
        }
        return src  // 返回包含前缀的列表
    }


    fun splitMessageLine(message: List<String>): List<String> {

        val mergeMessage = if (ThemeChat.settings.animation) {
            message.colored().joinToString("")
        } else {
            message.colored().joinToString("")
        }


        val src = mutableListOf<String>()  // 使用 MutableList 来允许添加元素
        for (i in 0 until mergeMessage.length) {
            src.add(mergeMessage.substring(0, i + 1))  // 从第0个字符到当前字符的子字符串
        }
        return src  // 返回包含前缀的列表
    }
}