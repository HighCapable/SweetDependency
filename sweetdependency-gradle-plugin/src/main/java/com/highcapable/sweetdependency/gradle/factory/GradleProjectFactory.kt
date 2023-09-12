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
 * This file is created by fankes on 2023/6/2.
 */
@file:Suppress("USELESS_ELVIS", "KotlinRedundantDiagnosticSuppress", "UselessCallOnNotNull")

package com.highcapable.sweetdependency.gradle.factory

import com.highcapable.sweetdependency.gradle.helper.GradleHelper
import com.highcapable.sweetdependency.gradle.wrapper.LibraryDependencyWrapper
import com.highcapable.sweetdependency.gradle.wrapper.PluginDependencyWrapper
import com.highcapable.sweetdependency.plugin.task.base.BaseTask
import com.highcapable.sweetdependency.utils.code.entity.MavenPomData
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.noBlank
import com.highcapable.sweetdependency.utils.orEmpty
import com.highcapable.sweetdependency.utils.toFile
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.internal.GeneratedSubclasses
import org.gradle.api.internal.plugins.PluginManagerInternal
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible
import org.gradle.kotlin.dsl.buildscript
import org.gradle.kotlin.dsl.repositories
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependency

/**
 * 获取指定项目的完整名称
 * @return [String]
 */
internal val Project.fullName
    get(): String {
        val baseNames = mutableListOf<String>()

        /**
         * 递归子项目
         * @param project 当前项目
         */
        fun fetchChild(project: Project) {
            project.parent?.also { if (it != it.rootProject) fetchChild(it) }
            baseNames.add(project.name)
        }
        fetchChild(project = this)
        return buildString { baseNames.onEach { append(":$it") }.clear() }.drop(1)
    }

/**
 * 向构建脚本添加自定义依赖
 * @param repositoryPath 存储库路径
 * @param pomData Maven POM 实体
 */
internal fun Project.addDependencyToBuildScript(repositoryPath: String, pomData: MavenPomData) =
    buildscript {
        repositories {
            maven {
                url = repositoryPath.toFile().toURI()
                mavenContent { includeGroup(pomData.groupId) }
            }
        }; dependencies { classpath("${pomData.groupId}:${pomData.artifactId}:${pomData.version}") }
    }

/**
 * 装载构建脚本的 [Class]
 * @param name [Class] 完整名称
 * @return [Class] or null
 */
internal fun Project.loadBuildScriptClass(name: String) = runCatching { buildscript.classLoader.loadClass(name) }.getOrNull()

/**
 * 获取指定项目部署的插件依赖数组 (实时)
 *
 * @param isUseCache 是否使用缓存 - 默认启用 - 启用后将使用 [GradleHelper.projectPlugins] 进行获取
 * @return [MutableList]<[PluginDependencyWrapper]>
 */
internal fun Project.plugins(isUseCache: Boolean = true) =
    if (isUseCache) GradleHelper.projectPlugins[this].orEmpty() else mutableListOf<PluginDependencyWrapper>().apply {
        plugins.configureEach {
            pluginManager.findPluginId(this).noBlank()?.also { add(PluginDependencyWrapper(instance = this, it)) }
        }
    }

/**
 * 获取指定项目部署的库依赖数组 (实时)
 *
 * @param isUseCache 是否使用缓存 - 默认启用 - 启用后将使用  [GradleHelper.projectLibraries] 进行获取
 * @return [MutableList]<[LibraryDependencyWrapper]>
 */
internal fun Project.libraries(isUseCache: Boolean = true) =
    if (isUseCache) GradleHelper.projectLibraries[this].orEmpty() else mutableListOf<LibraryDependencyWrapper>().apply {
        /**
         * 检查依赖是否有效
         * @return [Boolean]
         */
        fun Dependency.checkingValid() = when (this) {
            is ExternalDependency -> group.isNullOrBlank().not() && name.isNullOrBlank().not()
            is FileCollectionDependency -> runCatching { files.files.isNotEmpty() }.getOrNull() ?: false
            else -> true
        }
        /** 在一些项目 (例如 Kotlin Multiplatform 中会发生异常 [java.util.ConcurrentModificationException] - 这里直接做拦截处理 */
        runCatching {
            configurations.forEach { config ->
                config.dependencies.forEach { if (it.checkingValid()) add(LibraryDependencyWrapper(it, config.name ?: "")) }
            }
        }
    }

/**
 * 等待并监听当指定插件被添加时回调
 * @param id 插件 ID
 * @param action 回调插件实例
 */
internal fun Project.waitForPluginAdded(id: String, action: (Plugin<*>) -> Unit) {
    plugins.whenPluginAdded { if (pluginManager.findPluginId(this) == id) action(this) }
}

/**
 * 创建 Gradle Task [T]
 * @param group Task 分组
 * @param name Task 名称
 * @return [T]
 */
internal inline fun <reified T : BaseTask> Project.createTask(group: String, name: String) = runCatching {
    T::class.java.getConstructor().newInstance().also { instance ->
        task(name) {
            this.group = group
            outputs.upToDateWhen { false }
            doFirst { instance.onTransaction() }
        }
    }
}.getOrNull() ?: SError.make("Gradle task \"$name\" with group \"$group\" create failed")

/**
 * 应用插件
 * @param id 插件 ID
 * @param version 版本
 */
internal fun PluginDependenciesSpec.applyPlugin(id: String, version: String) =
    id(id).apply { if (version.isNotBlank()) version(version) } ?: SError.make("Plugin \"$id\" not apply")

/**
 * 应用插件
 * @param alias 别名实例
 */
internal fun PluginDependenciesSpec.applyPlugin(alias: Any) = when (alias) {
    is Provider<*> ->
        @Suppress("UNCHECKED_CAST")
        alias(alias as? Provider<PluginDependency> ?: SError.make("The $alias is not a valid plugin"))
            ?: SError.make("Plugin $alias not apply")
    is ProviderConvertible<*> ->
        @Suppress("UNCHECKED_CAST")
        alias(alias as? ProviderConvertible<PluginDependency> ?: SError.make("The $alias is not a valid plugin"))
            ?: SError.make("Plugin $alias not apply")
    else -> SError.make("The $alias is not a valid plugin (unknown type)")
}

/**
 * 通过 [PluginManager] 查找 [Plugin] 真实 ID
 *
 * 如果找不到会返回空字符串
 * @param plugin 当前实例
 * @return [String]
 */
private fun PluginManager.findPluginId(plugin: Plugin<*>) = runCatching {
    @Suppress("UNCHECKED_CAST")
    val pluginIds = (this as PluginManagerInternal).findPluginIdForClass(GeneratedSubclasses.unpackType(plugin) as Class<Plugin<*>>)
    if (pluginIds.isEmpty.not()) pluginIds.get() else null
}.getOrNull()?.id ?: ""