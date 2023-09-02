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
 * This file is Created by fankes on 2023/5/17.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package com.highcapable.sweetdependency.document

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.document.factory.DependencyMap
import com.highcapable.sweetdependency.document.factory.RepositoryList
import com.highcapable.sweetdependency.document.factory.checkingName
import com.highcapable.sweetdependency.document.factory.convertToDependencyAmbiguousName
import com.highcapable.sweetdependency.gradle.entity.DependencyName
import com.highcapable.sweetdependency.gradle.entity.DependencyVersion
import com.highcapable.sweetdependency.utils.capitalize
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.findDuplicates
import com.highcapable.sweetdependency.utils.hasDuplicate
import com.highcapable.sweetdependency.utils.yaml.proxy.IYamlDocument
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 项目 (根节点) 配置文档实体
 * @param preferences 偏好配置项文档实体
 * @param repositories 每项存储库配置项文档实体
 * @param plugins 每项插件依赖文档实体
 * @param libraries 每项库依赖文档实体
 * @param versions 每项版本定义数组
 */
@Serializable
internal data class RootConfigDocument(
    @SerialName("preferences")
    internal var preferences: PreferencesDocument? = PreferencesDocument(),
    @SerialName("repositories")
    internal var repositories: MutableMap<String, RepositoryDocument?>? = null,
    @SerialName("plugins")
    internal var plugins: MutableMap<String, DependencyDocument>? = null,
    @SerialName("libraries")
    internal var libraries: MutableMap<String, MutableMap<String, DependencyDocument>>? = null,
    @SerialName("versions")
    internal var versions: MutableMap<String, String>? = null
) : IYamlDocument {

    internal companion object {

        /** 默认文档内容 */
        private const val DEFAULT_CONTENT = """
            # SweetDependency project configuration file
            # You can adjust your custom configuration to your liking here
            # You can visit ${SweetDependency.PROJECT_URL} for more help
            #
            # SweetDependency 项目配置文件
            # 你可以在这里调整你喜欢的自定义配置
            # 你可以前往 ${SweetDependency.PROJECT_URL} 以获得更多帮助
            
            # Configure preferences
            # 配置偏好设置
            preferences:
              autowire-on-sync-mode: UPDATE_OPTIONAL_DEPENDENCIES
              repositories-mode: FAIL_ON_PROJECT_REPOS
            
            # Configure repositories used by dependencies
            # 配置依赖使用的存储库
            repositories:
              gradle-plugin-portal:
                scope: PLUGINS
              google:
              maven-central:
            
            # Configure plugins that need to be used
            # For example:
            # plugins:
            #   org.jetbrains.kotlin.jvm:
            #     version: +
            #
            # 配置需要使用的插件依赖
            # 例如:
            # plugins:
            #   org.jetbrains.kotlin.jvm:
            #     version: +
            plugins:
              
            # Configure libraries that need to be used
            # For example:
            # libraries:
            #   com.google.code.gson:
            #     gson:
            #       version: +
            #
            # 配置需要使用的库依赖
            # 例如:
            # libraries:
            #   com.google.code.gson:
            #     gson:
            #       version: +
            libraries:
              
            """

        /** 默认文档内容 */
        internal val defaultContent = DEFAULT_CONTENT.trimIndent()
    }

    /**
     * 获取当前偏好配置项文档实体
     * @return [PreferencesDocument]
     */
    internal fun preferences() = preferences ?: PreferencesDocument()

    /**
     * 获取当前存储库配置项文档实体
     * @return [RepositoryList]
     */
    internal fun repositories() = repositories?.let {
        mutableListOf<RepositoryDocument>().apply {
            it.forEach { (name, repository) -> (repository ?: RepositoryDocument()).build(name).also { if (it.isEnable) add(it) } }
        }
    } ?: mutableListOf()

    /**
     * 获取当前插件依赖数组
     * @param duplicate 允许重复 - 忽略处理后版本重复的异常 - 默认否
     * @return [DependencyMap]
     */
    internal fun plugins(duplicate: Boolean = false) = createPlugins().resolveDependencies(typeName = "plugin", duplicate)

    /**
     * 获取当前库依赖数组
     * @param duplicate 允许重复 - 忽略处理后版本重复的异常 - 默认否
     * @return [DependencyMap]
     */
    internal fun libraries(duplicate: Boolean = false) = createLibraries().resolveDependencies(typeName = "library", duplicate)

    /**
     * 处理依赖数组
     * @param typeName 依赖类型名称
     * @param duplicate 允许重复 - 忽略处理后版本重复的异常 - 默认否
     */
    private fun DependencyMap.resolveDependencies(typeName: String, duplicate: Boolean = false) = apply {
        val firstTypeName = typeName.capitalize()
        val checkDuplicateAlias = mutableMapOf<String, String>()
        val refLibraries = mutableListOf<Triple<DependencyName, String, DependencyVersion>>()
        val ambiguousNames = mutableListOf<String>()
        eachDependencies { dependencyName, artifact ->
            artifact.alias.checkingName("$typeName \"$dependencyName\" alias", isCheckMultiName = true)
            artifact.versions().forEach { (name, _) -> name.checkingName("$typeName \"$dependencyName\" version alias") }
            if (artifact.alias.isNotBlank())
                if (checkDuplicateAlias.contains(artifact.alias).not())
                    checkDuplicateAlias[artifact.alias] = dependencyName.current
                else SError.make(
                    "Duplicated alias \"${artifact.alias}\", " +
                        "already declared in $typeName \"${checkDuplicateAlias[artifact.alias]}\""
                )
            if (artifact.version().isNoSpecific && (artifact.versions().isNotEmpty() || artifact.versionRef.isNotBlank()))
                SError.make(
                    "$firstTypeName \"$dependencyName\" has declared that it does not specify a version, " +
                        "so it cannot use \"versions\" or \"version-ref\""
                )
            if (artifact.versionRef.isNotBlank() && artifact.versionRef.startsWith("<this>::"))
                artifact.versionRef = artifact.versionRef.replace("<this>:", dependencyName.groupId)
            refLibraries.add(Triple(dependencyName, artifact.alias, artifact.version()))
        }
        eachDependencies { dependencyName, artifact ->
            /** 处理版本引用 */
            fun resolveVersionRef() {
                refLibraries.firstOrNull { artifact.versionRef.let { e -> e == it.first.current || e == it.second } }?.also {
                    if (dependencyName == it.first || dependencyName.current == it.second)
                        SError.make("$firstTypeName \"$dependencyName\" declared \"version-ref\" from itself (recursive call found)")
                    when {
                        it.third.isNoSpecific -> SError.make(
                            "$firstTypeName \"${it.first}\" does not specify a version, so it can no longer be " +
                                "declared as \"version-ref\" by $typeName \"$dependencyName\""
                        )
                        it.third.isBlank -> SError.make(
                            "$firstTypeName \"${it.first}\" already has \"version-ref\" declared, so it can no longer" +
                                " be declared as \"version-ref\" by $typeName \"$dependencyName\" (recursive call found)"
                        )
                    }; artifact.updateVersion(it.third)
                } ?: SError.make(
                    "Could not found any versions or dependencies associated with " +
                        "version-ref \"${artifact.versionRef}\" of $typeName \"$dependencyName\""
                )
            }
            if (artifact.version().isNoSpecific) return@eachDependencies
            if (artifact.version().isBlank)
                if (artifact.versionRef.isNotBlank())
                    versions()[artifact.versionRef]?.also { artifact.version = it } ?: resolveVersionRef()
                else SError.make("Missing declared version when configuring $typeName \"$dependencyName\"")
            else if (artifact.version().isBlank.not() && artifact.versionRef.isNotBlank() && duplicate.not())
                SError.make("$firstTypeName \"$dependencyName\" can only have one \"version\" or \"version-ref\" node, please delete one")
        }
        eachDependencies { dependencyName, artifact ->
            ambiguousNames.add(dependencyName.ambiguousName())
            if (artifact.alias.isNotBlank()) {
                artifact.alias.checkingName("$typeName \"$dependencyName\" alias", isCheckMultiName = true)
                ambiguousNames.add(artifact.alias.convertToDependencyAmbiguousName())
            }; this[dependencyName] = artifact
        }
        if (ambiguousNames.hasDuplicate()) ambiguousNames.findDuplicates().forEach {
            SError.make("Found ambiguous name \"$it\" in declared dependencies, please checking your $typeName aliases that your declared")
        } else ambiguousNames.clear()
    }

    /**
     * 获取当前版本定义数组
     * @return [MutableMap]<[String], [String]>
     */
    internal fun versions() = versions?.onEach { (name, _) -> name.checkingName("versions name") } ?: mutableMapOf()

    /**
     * 重新创建 [plugins]
     * @return [DependencyMap]
     */
    private fun createPlugins() = mutableMapOf<DependencyName, DependencyDocument>().apply {
        plugins?.forEach { (notation, artifact) -> this[DependencyName.plugin(notation)] = artifact }
    }

    /**
     * 重新创建 [libraries]
     * @return [DependencyMap]
     */
    private fun createLibraries() = mutableMapOf<DependencyName, DependencyDocument>().apply {
        libraries?.forEach { (groupId, libraries) ->
            libraries.forEach { (artifactId, artifact) -> this[DependencyName.library(groupId, artifactId)] = artifact }
        }
    }

    /**
     * 循环每项 [plugins]、[libraries]
     * @param result 回调每项结果
     */
    private inline fun DependencyMap.eachDependencies(result: (dependencyName: DependencyName, artifact: DependencyDocument) -> Unit) =
        forEach { (dependencyName, artifact) -> result(dependencyName, artifact) }
}