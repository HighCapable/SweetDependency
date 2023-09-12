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
 * This file is created by fankes on 2023/5/31.
 */
package com.highcapable.sweetdependency.utils.debug

import com.highcapable.sweetdependency.exception.SweetDependencyUnresolvedException

/**
 * 全局异常管理类
 */
internal object SError {

    /**
     * 抛出异常
     * @param msg 消息内容
     * @throws e 异常内容 - 默认空
     * @throws SweetDependencyUnresolvedException
     */
    internal fun make(msg: String, e: Throwable? = null): Nothing = throw SweetDependencyUnresolvedException(msg, e)
}