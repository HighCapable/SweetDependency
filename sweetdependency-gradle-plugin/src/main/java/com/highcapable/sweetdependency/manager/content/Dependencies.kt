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
 * This file is Created by fankes on 2023/6/25.
 */
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.highcapable.sweetdependency.manager.content

import com.highcapable.sweetdependency.document.DependencyDocument
import com.highcapable.sweetdependency.document.factory.DependenciesCondition
import com.highcapable.sweetdependency.document.factory.DependencyMap
import com.highcapable.sweetdependency.gradle.entity.DependencyName
import com.highcapable.sweetdependency.utils.filter

/**
 * 已添加的依赖管理类
 */
internal object Dependencies {

    /** 当前已添加的全部插件依赖数组 */
    private val pluginEntries = mutableMapOf<DependencyName, DependencyDocument>()

    /** 当前已添加的全部库依赖数组 */
    private val libraryEntries = mutableMapOf<DependencyName, DependencyDocument>()

    /** 标识当前是否为已过期状态 */
    private var isMarkedOutdate = true

    /**
     * 获取当前过期状态
     * @return [Boolean]
     */
    internal val isOutdate get() = isMarkedOutdate

    /**
     * 刷新当前缓存数据状态
     * @param isOutdate 是否标识为已过期
     */
    internal fun refreshState(isOutdate: Boolean) {
        isMarkedOutdate = isOutdate
    }

    /**
     * 获取当前全部数组
     * @return [DependencyMap]
     */
    internal fun all() = (pluginEntries + libraryEntries).toMutableMap()

    /**
     * 获取当前插件依赖数组
     * @return [DependencyMap]
     */
    internal fun plugins() = pluginEntries

    /**
     * 获取当前库依赖数组
     * @return [DependencyMap]
     */
    internal fun libraries() = libraryEntries

    /**
     * 当前是否存在依赖
     * @return [Boolean]
     */
    internal fun isEmpty() = all().isEmpty()

    /**
     * 当前是否不存在依赖
     * @return [Boolean]
     */
    internal fun isNotEmpty() = isEmpty().not()

    /**
     * 查找是否存在指定的依赖
     * @param condition 条件方法体
     * @return [Boolean]
     */
    internal inline fun hasAll(condition: DependenciesCondition) = findAll { key, value -> condition(key, value) }.isNotEmpty()

    /**
     * 查找是否存在指定的插件依赖
     * @param condition 条件方法体
     * @return [Boolean]
     */
    internal inline fun hasPlugin(condition: DependenciesCondition) = findPlugins { key, value -> condition(key, value) }.isNotEmpty()

    /**
     * 查找是否存在指定的库依赖
     * @param condition 条件方法体
     * @return [Boolean]
     */
    internal inline fun hasLibrary(condition: DependenciesCondition) = findLibraries { key, value -> condition(key, value) }.isNotEmpty()

    /**
     * 查找指定条件的依赖数组
     * @param condition 条件方法体
     * @return [DependencyMap]
     */
    internal inline fun findAll(condition: DependenciesCondition) = all().filter { condition(it.key, it.value) }

    /**
     * 查找指定条件的插件依赖数组
     * @param condition 条件方法体
     * @return [DependencyMap]
     */
    internal inline fun findPlugins(condition: DependenciesCondition) = plugins().filter { condition(it.key, it.value) }

    /**
     * 查找指定条件的库依赖数组
     * @param condition 条件方法体
     * @return [DependencyMap]
     */
    internal inline fun findLibraries(condition: DependenciesCondition) = libraries().filter { condition(it.key, it.value) }

    /**
     * 生成依赖数组
     * @param plugins 插件依赖数组
     * @param libraries 依赖数组
     */
    internal fun generate(plugins: DependencyMap, libraries: DependencyMap) {
        if (plugins == plugins() && libraries == libraries()) return refreshState(isOutdate = false)
        resetData()
        plugins().putAll(plugins)
        libraries().putAll(libraries)
    }

    /** 重置 (清空) 当前依赖数组 */
    private fun resetData() {
        pluginEntries.clear()
        libraryEntries.clear()
        refreshState(isOutdate = true)
    }
}