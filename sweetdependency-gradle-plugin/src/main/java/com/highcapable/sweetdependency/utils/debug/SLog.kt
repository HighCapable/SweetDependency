/*
 * SweetDependency - An easy autowire and manage dependencies Gradle plugin
 * Copyright (C) 2019-2023 HighCapable
 * https://github.com/HighCapable/SweetDependency
 *
 * Apache License Version 2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file is created by fankes on 2023/5/25.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package com.highcapable.sweetdependency.utils.debug

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.utils.await
import org.apache.log4j.Logger

/**
 * 全局 Log 管理类
 */
internal object SLog {

    internal const val DONE = "✅"
    internal const val IGNORE = "❎"
    internal const val ERROR = "❌"
    internal const val WARN = "⚠️"
    internal const val LINK = "➡️"
    internal const val WIRE = "⚙️"
    internal const val UP = "⬆️"
    internal const val ROTATE = "\uD83D\uDD04"
    internal const val ANLZE = "\uD83D\uDD0D"
    internal const val STRNG = "\uD83D\uDCAA"

    /** 当前日志输出对象 */
    private val logger = Logger.getLogger(SLog::class.java)

    /** 短时间内不重复的日志内容 */
    private var noRepeatInThisTimeContent = ""

    /** 是否启用详细模式 */
    internal var isVerboseMode = true

    /**
     * 打印 Verbose 级别 Log (无颜色)
     * @param msg 消息内容
     * @param symbol 前缀符号 - 仅限非 [noTag] - 默认无
     * @param noTag 无标签 - 默认否
     * @param noRepeat 短时间内不重复输出 - 默认否
     */
    internal fun verbose(msg: Any, symbol: String = "", noTag: Boolean = false, noRepeat: Boolean = false) {
        if (isVerboseMode) info(msg, symbol, noTag, noRepeat)
    }

    /**
     * 打印 Info (提醒) 级别 Log (绿色)
     * @param msg 消息内容
     * @param symbol 前缀符号 - 仅限非 [noTag] - 默认无
     * @param noTag 无标签 - 默认否
     * @param noRepeat 短时间内不重复输出 - 默认否
     */
    internal fun note(msg: Any, symbol: String = "", noTag: Boolean = false, noRepeat: Boolean = false) =
        log(if (noTag) msg else msg.createSymbolMsg(symbol), color = "38;5;10", noRepeat = noRepeat)

    /**
     * 打印 Info 级别 Log (无颜色)
     * @param msg 消息内容
     * @param symbol 前缀符号 - 仅限非 [noTag] - 默认无
     * @param noTag 无标签 - 默认否
     * @param noRepeat 短时间内不重复输出 - 默认否
     */
    internal fun info(msg: Any, symbol: String = "", noTag: Boolean = false, noRepeat: Boolean = false) =
        log(if (noTag) msg else msg.createSymbolMsg(symbol), noRepeat = noRepeat)

    /**
     * 打印 Warn 级别 Log (黄色)
     * @param msg 消息内容
     * @param symbol 前缀符号 - 仅限非 [noTag] - 默认 [WARN]
     * @param noTag 无标签 - 默认否
     * @param noRepeat 短时间内不重复输出 - 默认否
     */
    internal fun warn(msg: Any, symbol: String = WARN, noTag: Boolean = false, noRepeat: Boolean = false) =
        log(if (noTag) msg else msg.createSymbolMsg(symbol), color = "33", noRepeat = noRepeat)

    /**
     * 打印 Error 级别 Log (红色)
     * @param msg 消息内容
     * @param symbol 前缀符号 - 仅限非 [noTag] - 默认 [ERROR]
     * @param noTag 无标签 - 默认否
     * @param noRepeat 短时间内不重复输出 - 默认否
     */
    internal fun error(msg: Any, symbol: String = ERROR, noTag: Boolean = false, noRepeat: Boolean = false) =
        log(if (noTag) msg else msg.createSymbolMsg(symbol), isError = true, noRepeat = noRepeat)

    /**
     * 创建符号消息内容
     * @param symbol 前缀符号
     * @return [String]
     */
    private fun Any.createSymbolMsg(symbol: String) =
        if (symbol.isNotBlank()) "[${SweetDependency.TAG}] $symbol $this" else "[${SweetDependency.TAG}] $this"

    /**
     * 打印 Log
     * @param msg 消息内容
     * @param color 颜色代码 - 默认无颜色
     * @param isError 是否强制为错误日志 - 默认否
     * @param noRepeat 短时间内不重复输出 - 默认否
     */
    private fun log(msg: Any, color: String = "0", isError: Boolean = false, noRepeat: Boolean = false) {
        if (noRepeat && noRepeatInThisTimeContent == msg.toString()) return
        noRepeatInThisTimeContent = msg.toString()
        when {
            isError -> logger.error(msg)
            color != "0" -> println("\u001B[${color}m$msg\u001B[0m")
            else -> println(msg)
        }
        if (noRepeat) await(1500) { noRepeatInThisTimeContent = "" } else noRepeatInThisTimeContent = ""
    }
}