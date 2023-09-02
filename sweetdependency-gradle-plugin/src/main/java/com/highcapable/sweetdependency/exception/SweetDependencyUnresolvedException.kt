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
 * This file is Created by fankes on 2023/5/31.
 */
package com.highcapable.sweetdependency.exception

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.utils.dumpToString

/**
 * [SweetDependency] 异常定义类
 * @param msg 异常内容
 * @param parent 父级异常 - 默认空
 */
internal class SweetDependencyUnresolvedException internal constructor(private val msg: String, parent: Throwable? = null) : Exception(
    ("[${SweetDependency.TAG}] The project initialization could not be completed, please check the following for errors\n" +
        "If you need help, visit ${SweetDependency.PROJECT_URL}\n" +
        "* What went wrong:\n" +
        "$msg\n${if (parent != null) (when (parent) {
            is SweetDependencyUnresolvedException -> "* Caused by:"
            else -> "* Exception is:"
        } + "\n${parent.dumpToString()}") else ""}").trim()
) {
    override fun toString() = "${javaClass.simpleName}: $msg"
}