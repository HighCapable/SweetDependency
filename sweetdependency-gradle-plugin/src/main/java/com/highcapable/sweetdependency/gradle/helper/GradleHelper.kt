/*
 * SweetDependency - An easy autowire and manage dependencies Gradle plugin.
 * Copyright (C) 2019-2024 HighCapable
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
 * This file is created by fankes on 2023/5/19.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package com.highcapable.sweetdependency.gradle.helper

import com.highcapable.sweetdependency.gradle.factory.libraries
import com.highcapable.sweetdependency.gradle.factory.plugins
import com.highcapable.sweetdependency.gradle.wrapper.LibraryDependencyWrapper
import com.highcapable.sweetdependency.gradle.wrapper.PluginDependencyWrapper
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.parseFileSeparator
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import java.io.File
import java.io.FileReader
import java.util.*

/**
 * Gradle 工具类
 */
internal object GradleHelper {

    /** Gradle 配置文件名称 */
    private const val GRADLE_PROPERTIES_FILE_NAME = "gradle.properties"

    /** 当前 [Gradle] 静态实例 */
    private var instance: Gradle? = null

    /** 当前 [Settings] 静态实例 */
    private var settings: Settings? = null

    /** 当前装载的所有项目 */
    internal val allProjects = mutableSetOf<Project>()

    /** 当前装载的项目插件依赖数组 */
    internal val projectPlugins = mutableMapOf<Project, MutableList<PluginDependencyWrapper>>()

    /** 当前装载的项目库依赖数组 */
    internal val projectLibraries = mutableMapOf<Project, MutableList<LibraryDependencyWrapper>>()

    /**
     * 绑定当前使用的 [Settings] 静态实例
     * @param settings 当前设置
     */
    internal fun attach(settings: Settings) {
        instance = settings.also { this.settings = it }.gradle
    }

    /**
     * 缓存所有项目列表
     * @param rootProject 当前根项目
     */
    internal fun cachingProjectList(rootProject: Project) {
        allProjects.clear()
        allProjects.addAll(rootProject.allprojects)
    }

    /**
     * 缓存所有依赖列表
     * @param project 当前项目
     * @param isRoot 是否为根项目
     */
    internal fun cachingDependencyList(project: Project, isRoot: Boolean) {
        if (isRoot) {
            projectPlugins.clear()
            projectLibraries.clear()
        }
        project.plugins(isUseCache = false).forEach {
            if (projectPlugins[project] == null) projectPlugins[project] = mutableListOf()
            projectPlugins[project]?.add(it)
        }
        project.libraries(isUseCache = false).forEach {
            if (projectLibraries[project] == null) projectLibraries[project] = mutableListOf()
            projectLibraries[project]?.add(it)
        }
    }

    /**
     * 获取用户目录的 [Properties]
     * @return [Properties] or null
     */
    internal val userProperties get() = createProperties(instance?.gradleUserHomeDir)

    /**
     * 获取当前项目的 [Properties]
     * @return [Properties] or null
     */
    internal val projectProperties get() = createProperties(settings?.rootDir)

    /**
     * 获取当前 Gradle 项目的根目录
     * @return [File]
     */
    internal val rootDir get() = rootProject?.projectDir ?: settings?.rootDir ?: SError.make("Gradle is unavailable")

    /**
     * 获取当前 Gradle 项目的根项目 (Root Project)
     * @return [Project] or null
     */
    internal val rootProject get() = runCatching { instance?.rootProject }.getOrNull()

    /**
     * 获取当前 Gradle 版本
     * @return [String]
     */
    internal val version get() = instance?.gradle?.gradleVersion ?: ""

    /**
     * 获取当前 Gradle 是否处于离线模式
     * @return [Boolean]
     */
    internal val isOfflineMode get() = instance?.startParameter?.isOffline == true

    /**
     * 获取当前 Gradle 是否处于同步模式
     * @return [Boolean]
     */
    internal val isSyncMode get() = runningTaskNames.isNullOrEmpty()

    /**
     * 获取当前正在运行的 Task 名称数组
     * @return [MutableList]<[String]> or null
     */
    internal val runningTaskNames get() = instance?.startParameter?.taskRequests?.getOrNull(0)?.args

    /**
     * 创建新的 [Properties]
     * @param dir 当前目录
     * @return [Properties] or null
     */
    private fun createProperties(dir: File?) = runCatching {
        Properties().apply { load(FileReader(dir?.resolve(GRADLE_PROPERTIES_FILE_NAME)?.absolutePath?.parseFileSeparator() ?: "")) }
    }.getOrNull()
}