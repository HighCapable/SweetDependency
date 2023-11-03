/*
 * SweetDependency - An easy autowire and manage dependencies Gradle plugin.
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
 * This file is created by fankes on 2023/5/16.
 */
package com.highcapable.sweetdependency.manager

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.document.PreferencesDocument
import com.highcapable.sweetdependency.document.VersionFilterDocument
import com.highcapable.sweetdependency.document.factory.DependenciesCondition
import com.highcapable.sweetdependency.document.factory.DependencyMap
import com.highcapable.sweetdependency.document.factory.RepositoryList
import com.highcapable.sweetdependency.gradle.entity.DependencyName
import com.highcapable.sweetdependency.gradle.entity.DependencyUpdateMode
import com.highcapable.sweetdependency.gradle.entity.DependencyVersion
import com.highcapable.sweetdependency.gradle.factory.get
import com.highcapable.sweetdependency.gradle.factory.getOrCreate
import com.highcapable.sweetdependency.gradle.factory.waitForPluginAdded
import com.highcapable.sweetdependency.gradle.helper.GradleHelper
import com.highcapable.sweetdependency.manager.content.Dependencies
import com.highcapable.sweetdependency.manager.content.Repositories
import com.highcapable.sweetdependency.manager.helper.DependencyAutowireLogHelper
import com.highcapable.sweetdependency.manager.helper.DependencyDeployHelper
import com.highcapable.sweetdependency.manager.maven.MavenParser
import com.highcapable.sweetdependency.manager.maven.entity.MavenMetadata
import com.highcapable.sweetdependency.plugin.config.content.SweetDependencyConfigs
import com.highcapable.sweetdependency.plugin.extension.dsl.manager.SweetDependencyAutowireExtension
import com.highcapable.sweetdependency.utils.debug.SLog
import com.highcapable.sweetdependency.utils.noEmpty
import com.highcapable.sweetdependency.utils.single
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware

/**
 * 依赖部署、自动装配、更新管理类
 */
internal object DependencyManager {

    /** 生成并应用依赖数组 */
    internal fun generateAndApply() {
        Dependencies.generate(SweetDependencyConfigs.document.plugins(), SweetDependencyConfigs.document.libraries())
        val pluginsSize = Dependencies.plugins().size
        val librariesSize = Dependencies.libraries().size
        if (Dependencies.isNotEmpty()) SLog.verbose(
            "${SweetDependency.TAG} help you autowired ${pluginsSize + librariesSize} dependencies" +
                " (plugins: $pluginsSize, libraries: $librariesSize)", SLog.STRNG
        )
    }

    /**
     * 生成并应用依赖数组
     * @param settings 当前设置
     */
    internal fun generateAndApply(settings: Settings) {
        val isDisableOnSync = SweetDependencyConfigs.document.preferences().autowireOnSyncMode == PreferencesDocument.AutowireOnSyncMode.OFF
        var requiresAutowiringLibrariesSize = 0
        Dependencies.all().forEach { (_, artifact) -> if (artifact.version().isAutowire) requiresAutowiringLibrariesSize++ }
        if (requiresAutowiringLibrariesSize > 0 && GradleHelper.isSyncMode && isDisableOnSync) SLog.warn(
            "Found $requiresAutowiringLibrariesSize dependencies need to be autowired, " +
                "please manually run \"${GradleTaskManager.AUTOWIRE_DEPENDENCIES_TASK_NAME}\" task and re-run Gradle Sync"
        )
        DependencyDeployHelper.generateVersionCatalogs(settings)
    }

    /**
     * 初始化库依赖可访问类
     * @param rootProject 当前根项目
     */
    internal fun resolve(rootProject: Project) = DependencyDeployHelper.resolveAccessors(rootProject)

    /**
     * 部署依赖
     * @param rootProject 当前根项目
     */
    internal fun deploy(rootProject: Project) {
        /**
         * 为自动装配创建扩展方法
         * @param extension 当前扩展实例
         * @param isGroovyOnly 是否仅为 Groovy 创建 - 默认是
         */
        fun Project.deployAutowire(extension: ExtensionAware, isGroovyOnly: Boolean = true) {
            if (!isGroovyOnly || buildFile.name.endsWith(".gradle"))
                extension.getOrCreate<SweetDependencyAutowireExtension>(SweetDependencyAutowireExtension.NAME, this)
        }

        /**
         * 部署到当前项目
         * @param extension 当前扩展实例
         */
        fun Project.deployEach(extension: ExtensionAware) = DependencyDeployHelper.deployAccessors(project = this, extension)

        /** 适配 Kotlin Multiplatform */
        fun Project.deployForKotlinMultiplatform() =
            waitForPluginAdded("org.jetbrains.kotlin.multiplatform") {
                get("kotlin").also { extension ->
                    deployAutowire(extension, isGroovyOnly = false)
                    deployEach(extension)
                }
            }

        /** 部署到当前项目 */
        fun Project.deploy() {
            deployAutowire(dependencies)
            deployEach(dependencies)
            deployForKotlinMultiplatform()
        }
        rootProject.deploy()
        rootProject.subprojects.forEach { it.deploy() }
    }

    /**
     * 自动装配、更新依赖
     * @param updateMode 更新模式
     * @param isRunningOnSync 是否在 Gradle Sync 时运行 - 默认是
     */
    internal fun autowireAndUpdate(updateMode: DependencyUpdateMode, isRunningOnSync: Boolean = true) {
        /**
         * 在适当时候打印 Log
         * @param msg 消息内容
         * @param symbol 前缀符号
         */
        fun logIfNeeded(msg: String, symbol: String) = if (isRunningOnSync) SLog.verbose(msg, symbol) else SLog.info(msg, symbol)

        /**
         * 通过指定类型和条件查找依赖数组
         * @param condition 条件方法体
         * @return [DependencyMap]
         */
        fun findByType(condition: DependenciesCondition) = when (updateMode.dependencyType) {
            DependencyUpdateMode.DependencyType.ALL -> Dependencies.findAll(condition)
            DependencyUpdateMode.DependencyType.PLUGINS -> Dependencies.findPlugins(condition)
            DependencyUpdateMode.DependencyType.LIBRARIES -> Dependencies.findLibraries(condition)
        }
        if (Repositories.isEmpty()) return SLog.warn(
            """
              Repositories is empty, ${SweetDependency.TAG} will stop auto update dependencies
              This will cause Gradle fail to load dependencies, you must add some repositories and enable them
            """.trimIndent()
        )
        if (GradleHelper.isOfflineMode) SLog.warn("Gradle is in offline mode, some dependencies on online repositories will be ignored")
        val isOnlyAutowireMode = updateMode.updateType == DependencyUpdateMode.UpdateType.ONLY_AUTOWIRE
        var currentIndex = 0
        var updPluginsCount = 0
        var updLbrariesCount = 0
        val needUpdateDependencies = mutableMapOf<String, Pair<DependencyName, DependencyVersion>>()
        findByType { _, artifact ->
            !artifact.version().isNoSpecific && (artifact.versionRef.isBlank() &&
                ((updateMode.updateType == DependencyUpdateMode.UpdateType.UPDATE_ALL ||
                    (updateMode.updateType == DependencyUpdateMode.UpdateType.UPDATE_OPTIONAL &&
                        (artifact.version().isOptional || artifact.version().isAutowire)) ||
                    (updateMode.updateType == DependencyUpdateMode.UpdateType.ONLY_AUTOWIRE && artifact.version().isAutowire))))
        }.apply {
            if (isNotEmpty()) SLog.info("Starting ${when (updateMode.updateType) {
                DependencyUpdateMode.UpdateType.UPDATE_OPTIONAL -> "autowire and update $size optional dependencies"
                DependencyUpdateMode.UpdateType.UPDATE_ALL -> "autowire and update all $size dependencies"
                DependencyUpdateMode.UpdateType.ONLY_AUTOWIRE -> "autowire $size dependencies"
            }}", SLog.ANLZE)
            forEach { (dependencyName, artifact) ->
                val versionFilterExclusionList = artifact.versionFilter?.exclusionList()
                    ?: SweetDependencyConfigs.document.preferences().versionFilter.exclusionList()
                fetchUpdate(
                    positionTagName = "${++currentIndex}/$size",
                    dependencyName = dependencyName,
                    currentVersion = artifact.version(),
                    alternateVersions = artifact.versions().values.toMutableList(),
                    isAutoUpdate = artifact.isAutoUpdate,
                    versionFilterExclusionList = versionFilterExclusionList,
                    repositories = artifact.repositories()
                ) { newVersion ->
                    DependencyAutowireLogHelper.record(dependencyName, artifact.version(), newVersion)
                    artifact.updateVersion(newVersion)
                    needUpdateDependencies[dependencyName.current] = dependencyName to newVersion
                    when (dependencyName.type) {
                        DependencyName.Type.PLUGIN -> updPluginsCount++
                        DependencyName.Type.LIBRARY -> updLbrariesCount++
                    }
                }
            }
        }
        /** 找出所有版本引用依赖 - 为其设置版本 */
        needUpdateDependencies.forEach { (notation, dependencyData) ->
            val alias = findByType { key, _ -> dependencyData.first == key }.single()?.value?.alias ?: ""
            findByType { _, artifact ->
                artifact.versionRef.isNotBlank() && (artifact.versionRef == notation || artifact.versionRef == alias)
            }.forEach { (dependencyName, artifact) ->
                artifact.updateVersion(dependencyData.second)
                SLog.info("Link ${dependencyName.description} to ${dependencyData.first.description} version ${dependencyData.second}", SLog.LINK)
            }
        }
        if (needUpdateDependencies.isNotEmpty()) {
            SLog.info(
                (if (isOnlyAutowireMode) "Autowired" else "Autowired and updated") +
                    " ${needUpdateDependencies.size} dependencies (plugins: $updPluginsCount, libraries: $updLbrariesCount)", SLog.DONE
            )
            if (SweetDependencyConfigs.configs.isEnableDependenciesAutowireLog)
                logIfNeeded(msg = "Autowiring logs have been automatically written to: ${DependencyAutowireLogHelper.logFile}", SLog.LINK)
            SweetDependencyConfigs.documentMapping.updateDependencies(needUpdateDependencies)
            if (!isRunningOnSync) SLog.warn(
                """
                  **************************** NOTICE ****************************
                   ${needUpdateDependencies.size} dependencies (plugins: $updPluginsCount, libraries: $updLbrariesCount) has been changed
                   You must to manually re-run Gradle Sync to apply those changes
                  **************************** NOTICE ****************************
                """.trimIndent(), noTag = true
            )
            Dependencies.refreshState(isOutdate = true)
        } else logIfNeeded(msg = "No dependencies need to ${if (isOnlyAutowireMode) "autowire" else "autowire and update"}", SLog.DONE)
    }

    /**
     * 自动装配或更新当前依赖
     * @param positionTagName 当前位置标签名称
     * @param dependencyName 依赖名称
     * @param currentVersion 当前依赖版本
     * @param alternateVersions 备选依赖版本数组
     * @param isAutoUpdate 是否自动更新
     * @param versionFilterExclusionList 版本管理器排除列表
     * @param repositories 使用的存储库数组
     * @param result 回调新版本
     */
    private inline fun fetchUpdate(
        positionTagName: String,
        dependencyName: DependencyName,
        currentVersion: DependencyVersion,
        alternateVersions: MutableList<DependencyVersion>,
        isAutoUpdate: Boolean,
        versionFilterExclusionList: VersionFilterDocument.ExclusionList,
        repositories: RepositoryList,
        result: (newVersion: DependencyVersion) -> Unit
    ) {
        val poms = mutableListOf<MavenMetadata>()
        val headerInfo = if (GradleHelper.isOfflineMode) "$positionTagName > OFFLINE" else "$positionTagName > NOT-FOUND"
        val displayInfo = "${dependencyName.description} ${currentVersion.let{ if (it.isAutowire) "" else "version $it" }}"
        (repositories.noEmpty() ?: Repositories.all()).apply {
            forEachIndexed { index, entry ->
                val currentVersionFilterExclusionList = versionFilterExclusionList.depends(currentVersion)
                val availableVersions = mutableListOf<DependencyVersion>()
                poms.add(MavenParser.acquire(dependencyName, entry, currentVersion))
                if (index == lastIndex) poms.noEmpty()
                    ?.sortedByDescending { it.lastUpdated }
                    ?.let { if (it.all { e -> e.lastUpdated <= 0L }) it.sortedByDescending { e -> e.versions.size } else it }
                    ?.filter { it.versions.isNotEmpty() }
                    ?.let { linkedSetOf<DependencyVersion>().apply { it.forEach { entity -> addAll(entity.versions) } }.toMutableList() }
                    ?.let { availableVersions.addAll(it); currentVersionFilterExclusionList.filter(it) }
                    ?.also {
                        if (currentVersionFilterExclusionList.isNotEmpty() && availableVersions.isNotEmpty() && it.isEmpty()) SLog.warn(
                            """
                              ${dependencyName.description} available versions exclusion to nothing by version filter
                              All available versions have been filtered, if this is wrong, please reconfigure your version filter
                              You can disable internal version filter like following:
                            """.trimIndent() + "\n" + when (dependencyName.type) {
                                DependencyName.Type.PLUGIN -> """
                                  ${dependencyName.groupId}:
                                    version-filter:
                                      use-internal: false
                                    ...
                                """.trimIndent()
                                DependencyName.Type.LIBRARY -> """
                                  ${dependencyName.groupId}:
                                    ${dependencyName.artifactId}:
                                      version-filter:
                                        use-internal: false
                                      ...
                                """.trimIndent()
                            } + "\n" + """
                              Available versions: $availableVersions
                              Version filter: $currentVersionFilterExclusionList
                            """.trimIndent()
                        )
                    }?.noEmpty()?.also { versions ->
                        resolveEachUpdate(
                            positionTagName, dependencyName, versions, currentVersion,
                            versions.first(), alternateVersions, isAutoUpdate, result
                        )
                    } ?: if (GradleHelper.isOfflineMode) SLog.warn("$headerInfo $displayInfo") else SLog.error("$headerInfo $displayInfo")
            }
        }
    }

    /**
     * 自动装配或更新每项依赖
     * @param positionTagName 当前位置标签名称
     * @param dependencyName 依赖名称
     * @param versions 全部可用依赖版本数组
     * @param currentVersion 当前依赖版本
     * @param latestVersion 最新依赖版本
     * @param alternateVersions 备选依赖版本数组
     * @param isAutoUpdate 是否自动更新
     * @param result 回调新版本
     */
    private inline fun resolveEachUpdate(
        positionTagName: String,
        dependencyName: DependencyName,
        versions: MutableList<DependencyVersion>,
        currentVersion: DependencyVersion,
        latestVersion: DependencyVersion,
        alternateVersions: MutableList<DependencyVersion>,
        isAutoUpdate: Boolean,
        result: (newVersion: DependencyVersion) -> Unit
    ) = when {
        !currentVersion.isAutowire && !versions.contains(currentVersion) ->
            SLog.warn("$positionTagName > MISSING ${dependencyName.description} version $currentVersion, available are $versions")
        !currentVersion.isAutowire && alternateVersions.isNotEmpty() && !alternateVersions.all { versions.contains(it) } ->
            SLog.warn("$positionTagName > MISSING ${dependencyName.description} version alias $alternateVersions, available are $versions")
        latestVersion != currentVersion -> when {
            currentVersion.isAutowire -> {
                SLog.info("$positionTagName > AUTOWIRE ${dependencyName.description} version $latestVersion", SLog.WIRE)
                result(latestVersion)
            }
            isAutoUpdate -> {
                SLog.info("$positionTagName > AUTO-UPDATE ${dependencyName.description} version $currentVersion -> $latestVersion", SLog.UP)
                result(latestVersion)
            }
            else -> SLog.note("$positionTagName > UPDATE-AVAILABLE ${dependencyName.description} version $currentVersion -> $latestVersion", SLog.ROTATE)
        }
        else -> SLog.info("$positionTagName > UP-TO-DATE ${dependencyName.description} version $currentVersion", SLog.DONE)
    }
}