package ink.ptms.chemdah.core.conversation.theme

import taboolib.common.io.newFile
import taboolib.module.chat.colored

class MessageLine(message: String) {
    var currentIndex=0
    var typeWriterStrings: List<String>? = null;
    var maxIndex:Int=0
    fun reset(){
        this.currentIndex=0;
    }

    init {
        typeWriterStrings = splitMessageLine(message)
        maxIndex= typeWriterStrings!!.size-1
    }
    fun stepUp(){
        currentIndex++
    }

    fun currentCharisSpace():Boolean{
        return currentIndex < maxIndex-1 && typeWriterStrings!!.get(currentIndex).last().equals(' ', true)
    }
    fun isDown():Boolean{
        if (currentIndex>maxIndex){
            return true
        }else{
            return false
        }
    }
    fun getCurrent():String{
        return typeWriterStrings!!.get(currentIndex)
    }
    fun getComplate():String{
        return typeWriterStrings!!.last()
    }
    //分割字符串  营造打字机效果
    fun splitMessageLine(message: String): List<String> {
        val src = mutableListOf<String>()  // 使用 MutableList 来允许添加元素
        for (i in 0 until message.length) {
            src.add(message.substring(0, i + 1))  // 从第0个字符到当前字符的子字符串
        }

        return src  // 返回包含前缀的列表
    }
}