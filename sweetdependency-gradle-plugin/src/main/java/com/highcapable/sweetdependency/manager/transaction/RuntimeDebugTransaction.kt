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
 * This file is created by fankes on 2023/6/8.
 */
package com.highcapable.sweetdependency.manager.transaction

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.document.factory.DependencyMap
import com.highcapable.sweetdependency.environment.Environment
import com.highcapable.sweetdependency.gradle.factory.fullName
import com.highcapable.sweetdependency.gradle.factory.libraries
import com.highcapable.sweetdependency.gradle.factory.plugins
import com.highcapable.sweetdependency.gradle.helper.GradleHelper
import com.highcapable.sweetdependency.gradle.wrapper.type.LibraryDependencyType
import com.highcapable.sweetdependency.plugin.config.content.SweetDependencyConfigs
import com.highcapable.sweetdependency.utils.debug.SLog
import com.highcapable.sweetdependency.utils.noBlank
import com.highcapable.sweetdependency.utils.noEmpty

/**
 * 运行时调试管理类
 */
internal object RuntimeDebugTransaction {

    private val configs get() = SweetDependencyConfigs.configs
    private val preferences get() = SweetDependencyConfigs.document.preferences()
    private val repositories get() = SweetDependencyConfigs.document.repositories()
    private val plugins get() = SweetDependencyConfigs.document.plugins(duplicate = true)
    private val libraries get() = SweetDependencyConfigs.document.libraries(duplicate = true)
    private val vfExclusionList get() = preferences.versionFilter.exclusionList().all()

    /** 存储库内存数组 */
    private val repositoriesMap = mutableMapOf<String, Any>()

    /** 插件依赖内存数组 */
    private val pluginsMap = mutableMapOf<String, Any>()

    /** 依赖内存数组 */
    private val librariesMap = mutableMapOf<String, Any>()

    /** 版本过滤器内存数组 */
    private val versionFilterMap = mutableMapOf<String, Any>()

    /** 项目插件依赖内存数组 */
    private val projectPluginsMap = mutableMapOf<String, Any>()

    /** 项目库依赖内存数组 */
    private val projectLibrariesMap = mutableMapOf<String, Any>()

    /** 写出调试信息 */
    internal fun dump() {
        configureMemoryData()
        log(
            """
            
              +--------------------------------------+
              |   SWEET DEPENDENCY MEMORY DATA DUMP  |
              +--------------------------------------+
            
            """
        )
        log(
            "System Environment" value Environment.systemInfo,
            "Character Encoding" value Environment.characterEncoding,
            "Java Version" value Environment.javaVersion,
            "Gradle Version" value GradleHelper.version,
            "Plugin Version" value SweetDependency.VERSION,
            "Plugin Configuration" to mapOf(
                "isEnable" value configs.isEnable,
                "isDebug" value configs.isDebug,
                "configFileName" with "(path) ${configs.configFilePath}" to mapOf(
                    "preferences" to mapOf(
                        "autowire-on-sync-mode" value preferences.autowireOnSyncMode,
                        "repositories-mode" value preferences.repositoriesMode,
                        "dependencies-namespace" to mapOf(
                            "plugins" to mapOf(
                                "enable" value preferences.dependenciesNamespace.plugins.isEnable,
                                "name" value preferences.dependenciesNamespace.plugins.name().ifBlank { NONE }
                            ),
                            "libraries" to mapOf(
                                "enable" value preferences.dependenciesNamespace.libraries.isEnable,
                                "name" value preferences.dependenciesNamespace.libraries.name().ifBlank { NONE }
                            )
                        ),
                        "version-filter" with (if (vfExclusionList.isEmpty()) "(disabled)" else "") to versionFilterMap
                    ),
                    "repositories" to repositoriesMap,
                    "plugins" to pluginsMap,
                    "libraries" to librariesMap
                ),
                "isUseDependencyResolutionManagement" value configs.isUseDependencyResolutionManagement,
                "isEnableDependenciesAutowireLog" value configs.isEnableDependenciesAutowireLog,
                "isEnableVerboseMode" value configs.isEnableVerboseMode
            ),
            "Project Dependencies" to mapOf(
                "Plugins" with (if (projectPluginsMap.isEmpty()) "(load failed)" else "") to projectPluginsMap,
                "Libraries" with (if (projectLibrariesMap.isEmpty()) "(load failed)" else "") to projectLibrariesMap
            )
        )
        log(
            """
            
              All debug information has been dumped, if your project is not working properly, please give us feedback on this report
              For details, please visit: ${SweetDependency.PROJECT_URL}
            
            """
        )
    }

    /** 配置内存数据 */
    private fun configureMemoryData() {
        if (repositoriesMap.isNotEmpty() &&
            pluginsMap.isNotEmpty() &&
            librariesMap.isNotEmpty() &&
            versionFilterMap.isNotEmpty() &&
            projectLibrariesMap.isNotEmpty()
        ) return
        repositoriesMap.clear()
        pluginsMap.clear()
        librariesMap.clear()
        versionFilterMap.clear()
        projectLibrariesMap.clear()
        repositories.forEach { repo ->
            val hasCredentials = repo.credentials.let { it.username.isNotBlank() || it.password.isNotBlank() }
            val repoMap = mutableMapOf<String, Any>(
                "enable" value repo.isEnable,
                "url" value repo.url.ifBlank { NONE },
                "path" value repo.path.ifBlank { NONE }
            )
            if (hasCredentials) repoMap["credentials"] = mapOf(
                "username" value repo.credentials.username,
                "password" value repo.credentials.password
            )
            repositoriesMap[repo.nodeName] = repoMap
        }
        plugins.resolveDependencies(pluginsMap)
        libraries.resolveDependencies(librariesMap)
        if (vfExclusionList.isNotEmpty()) versionFilterMap["exclusionList"] = mutableMapOf<String, Any>()
        vfExclusionList.forEach { versionFilterMap["exclusionList"]?.addAsMap(it) }
        GradleHelper.allProjects.forEach { subProject ->
            projectPluginsMap[subProject.fullName()] = mutableMapOf<String, Any>()
            projectLibrariesMap[subProject.fullName()] = mutableMapOf<String, Any>()
            subProject.plugins().forEach { projectPluginsMap[subProject.fullName()]?.addAsMap(it.id) }
            subProject.libraries().forEach {
                val prefix = "(${it.configurationName})"
                when (it.type) {
                    LibraryDependencyType.EXTERNAL, LibraryDependencyType.EXTERNAL_DELEGATE -> {
                        val suffix = it.version.deployed.noBlank()?.let { e -> ":$e" } ?: ""
                        projectLibrariesMap[subProject.fullName()]?.addAsMap("$prefix ${it.groupId}:${it.artifactId}$suffix")
                    }
                    LibraryDependencyType.PROJECT -> projectLibrariesMap[subProject.fullName()]?.addAsMap("$prefix (project) ${it.project?.fullName()}")
                    LibraryDependencyType.FILES -> {
                        val filesMap = mutableMapOf<String, String>()
                        it.files?.noEmpty()?.forEach { e -> filesMap.addAsMap(e.absolutePath) }?.also {
                            projectLibrariesMap[subProject.fullName()] = mapOf("$prefix (files)" to filesMap)
                        } ?: projectLibrariesMap[subProject.fullName()]?.addAsMap("$prefix (files) not found or empty folder")
                    }
                    LibraryDependencyType.OTHERS -> projectLibrariesMap[subProject.fullName()]?.addAsMap("$prefix unknown type dependency")
                }
            }
        }
    }

    /**
     * 处理依赖数组
     * @param dependenciesMap 依赖内存数组
     */
    private fun DependencyMap.resolveDependencies(dependenciesMap: MutableMap<String, Any>) =
        forEach { (dependencyName, artifact) ->
            val repoMap = mutableMapOf<String, Any>()
            val childVersionFilterMap = mutableMapOf<String, Any>()
            val childVfExclusionList = artifact.versionFilter?.exclusionList()?.all()
            if (childVfExclusionList?.isNotEmpty() == true) childVersionFilterMap["exclusionList"] = mutableMapOf<String, Any>()
            childVfExclusionList?.forEach { childVersionFilterMap["exclusionList"]?.addAsMap(it) }
            artifact.repositories().forEach { repoMap.addAsMap(it.nodeName) }
            dependenciesMap[dependencyName.current] = mapOf(
                "alias" value artifact.alias.ifBlank { NONE },
                "version" value artifact.version().let { if (it.isNoSpecific) "(no specific)" else it.current },
                "auto-update" value artifact.isAutoUpdate,
                "version-filter" with (if (childVfExclusionList?.isEmpty() == true) "(disabled)" else "") to childVersionFilterMap,
                "repositories" to repoMap,
            )
        }

    /**
     * 生成单边 [Pair]
     * @param value 键值内容
     * @return [Pair]<[String], [String]>
     */
    private infix fun String.value(value: Any) = Pair(with(value), "")

    /**
     * 生成冒号键值对字符串
     * @param value 键值内容
     * @return [String]
     */
    private infix fun String.with(value: Any) = if (value != NONE) "$this${if (value.toString().isBlank()) "" else ": $value"}" else ""

    /**
     * 任意类型转换为 [MutableMap] 并设置空键值内容
     * @param key 键值名称
     */
    private fun Any.addAsMap(key: String) {
        @Suppress("UNCHECKED_CAST")
        (this as MutableMap<String, Any>)[key] = ""
    }

    /**
     * 创建 [MutableMap]
     * @param pairs 键值对数组
     * @return [MutableMap]<[String], [Any]>
     */
    private fun mapOf(vararg pairs: Pair<String, Any>) = mutableMapOf(*pairs)

    /**
     * 根据 [Map] 生成键值对树图形字符串
     * @return [String]
     */
    private fun Map<*, *>.genMapTree(): String {
        /**
         * 生成子项目
         * @param prefix 前缀
         * @return [String]
         */
        fun Map<*, *>.genChild(prefix: String = ""): String {
            val currentMap = filterKeys { it.toString().isNotBlank() }.filterValues { it !is Map<*, *> || it.isNotEmpty() }
            val builder = StringBuilder()
            currentMap.keys.forEachIndexed { index, key ->
                val value = currentMap[key]
                val isLast = index == currentMap.keys.size - 1
                val branch = if (isLast) "└─ " else "├─ "
                val newPrefix = if (isLast) "$prefix   " else "$prefix│  "
                builder.append("$prefix$branch$key\n")
                if (value is Map<*, *>) builder.append(value.genChild(newPrefix))
            }; return builder.toString()
        }; return "${SweetDependency.TAG}\n${genChild()}"
    }

    /**
     * 打印日志
     * @param pairs 键值对数组
     */
    private fun log(vararg pairs: Pair<String, Any>) = log(mapOf(*pairs).genMapTree())

    /**
     * 打印日志
     * @param any 任意内容
     */
    private fun log(any: Any) = SLog.info(any.toString().trimIndent(), noTag = true)

    /** 标识当前值为空 */
    private const val NONE = "/*-none-*/"
}