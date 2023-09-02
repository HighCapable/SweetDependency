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
 * This file is Created by fankes on 2023/5/29.
 */
package com.highcapable.sweetdependency.gradle.delegate

import org.gradle.api.Project
import kotlin.properties.Delegates

/**
 * 项目事务实例实现类
 */
internal class ProjectTransaction {

    internal companion object {

        /** 当前项目 (当前生命周期静态) */
        internal var current by Delegates.notNull<Project>()

        /** 是否为根项目 (当前生命周期静态) */
        internal var isRoot by Delegates.notNull<Boolean>()
    }

    /** 当前装载实例方法体数组 */
    internal val evaluateCallbacks = mutableSetOf<((Project, Boolean) -> Unit)>()

    /**
     * 获取当前项目
     * @return [Project]
     */
    internal val current get() = Companion.current

    /**
     * 获取是否为根项目
     * @return [Boolean]
     */
    internal val isRoot get() = Companion.isRoot

    /**
     * 创建装载实例监听
     * @param evaluate 回调装载监听 - ([Project] 当前项目,[Boolean] 师傅为根项目)
     */
    internal fun evaluation(evaluate: (project: Project, isRoot: Boolean) -> Unit) {
        evaluateCallbacks.add(evaluate)
    }
}