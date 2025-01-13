/*
 * SweetDependency - An easy autowire and manage dependencies Gradle plugin.
 * Copyright (C) 2019 HighCapable
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
 * This file is created by fankes on 2023/7/16.
 */
package com.highcapable.sweetdependency.utils

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 创建当前线程池服务
 * @return [ExecutorService]
 */
private val currentThreadPool get() = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

/**
 * 启动 [Thread] 延迟等待 [block] 的结果 [T]
 * @param delayMs 延迟毫秒 - 默认 1 ms
 * @param block 方法块
 * @return [T]
 */
internal inline fun <T> T.await(delayMs: Long = 1, crossinline block: (T) -> Unit): T {
    currentThreadPool.apply {
        execute {
            if (delayMs > 0) Thread.sleep(delayMs)
            block(this@await)
            shutdown()
        }
    }; return this
}