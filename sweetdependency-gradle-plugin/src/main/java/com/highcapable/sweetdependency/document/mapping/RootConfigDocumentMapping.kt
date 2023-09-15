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
 * This file is created by fankes on 2023/6/1.
 */
package com.highcapable.sweetdependency.document.mapping

import com.highcapable.sweetdependency.document.RootConfigDocument
import com.highcapable.sweetdependency.document.factory.DependencyUpdateMap
import com.highcapable.sweetdependency.document.factory.spliceToDependencyNotation
import com.highcapable.sweetdependency.document.mapping.entity.DependencyMapping
import com.highcapable.sweetdependency.exception.SweetDependencyUnresolvedException
import com.highcapable.sweetdependency.gradle.entity.DependencyName
import com.highcapable.sweetdependency.plugin.config.proxy.ISweetDependencyConfigs
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.debug.SLog
import com.highcapable.sweetdependency.utils.filter
import com.highcapable.sweetdependency.utils.joinToContent
import com.highcapable.sweetdependency.utils.toFile
import com.highcapable.sweetdependency.utils.yaml.Yaml
import com.highcapable.sweetdependency.utils.yaml.factory.asMap
import com.highcapable.sweetdependency.utils.yaml.factory.isKeyExist
import java.io.File

/**
 * [RootConfigDocument] 的测绘实例实现类
 * @param configs 当前配置
 */
internal class RootConfigDocumentMapping internal constructor(private val configs: ISweetDependencyConfigs) {

    private companion object {

        /** 偏好设置节点名称 */
        private const val PREFERENCES_NODE_NAME = "preferences:"

        /** 存储库节点名称 */
        private const val REPOSITORIES_NODE_NAME = "repositories:"

        /** 版本节点名称 */
        private const val VERSIONS_NODE_NAME = "versions:"

        /** 插件依赖节点名称 */
        private const val PLUGINS_NODE_NAME = "plugins:"

        /** 库依赖节点名称 */
        private const val LIBRARIES_NODE_NAME = "libraries:"

        /** 依赖版本起始内容 */
        private const val VERSION_NODE_CONTENT = "version:"

        /** 依赖版本引用节点名称 (不包括结尾的“:”) */
        private const val VERSION_REF_NODE_NAME = "version-ref"

        /** 4 个空格 (缩进) 内容 */
        private const val SPACE_OF_4 = "    "

        /** 6 个空格 (缩进) 内容 */
        private const val SPACE_OF_6 = "      "
    }

    /** 当前配置文件 */
    private var configFile: File? = null

    /** 配置文件行内容数组 */
    private val configFileContents = mutableListOf<String>()

    /** 插件依赖测绘实体数组 */
    private val pluginsMapping = mutableListOf<DependencyMapping>()

    /** 库依赖测绘实体数组 */
    private val librariesMapping = mutableListOf<DependencyMapping>()

    init {
        runCatching { createMapping() }.onFailure {
            when (it) {
                is SweetDependencyUnresolvedException -> throw it
                else -> SLog.error("Failed to create config file's mapping, this will cause problem")
            }
        }
    }

    /** 创建测绘数据 */
    private fun createMapping() {
        configFileContents.clear()
        configFileContents.addAll(configs.configFilePath.toFile().apply { configFile = this }.readText().split("\n"))
        var isFoundPluginsStartLine = false
        var pluginsStartLine = -1
        var pluginsLine = 0
        var isFoundLibrariesStartLine = false
        var librariesStartLine = -1
        var librariesLine = 0
        val pluginsContents = mutableListOf<String>()
        val librariesContents = mutableListOf<String>()
        configFileContents.forEachIndexed { index, content ->
            if (content.contains("\"\"")) SError.make("Character declared like -> \"\" <- are not allowed, detected at line ${index + 1}")
        }
        configFileContents.forEachIndexed { index, content ->
            if (content.startsWith(PREFERENCES_NODE_NAME) ||
                content.startsWith(REPOSITORIES_NODE_NAME) ||
                content.startsWith(VERSIONS_NODE_NAME) ||
                content.startsWith(LIBRARIES_NODE_NAME)
            ) {
                isFoundPluginsStartLine = false
                return@forEachIndexed
            }
            if (content.startsWith(PLUGINS_NODE_NAME)) {
                isFoundPluginsStartLine = true
                pluginsStartLine = index
            }
            if (isFoundPluginsStartLine) pluginsContents.add(content)
        }
        configFileContents.forEachIndexed { index, content ->
            if (content.startsWith(PREFERENCES_NODE_NAME) ||
                content.startsWith(REPOSITORIES_NODE_NAME) ||
                content.startsWith(VERSIONS_NODE_NAME) ||
                content.startsWith(PLUGINS_NODE_NAME)
            ) {
                isFoundLibrariesStartLine = false
                return@forEachIndexed
            }
            if (content.startsWith(LIBRARIES_NODE_NAME)) {
                isFoundLibrariesStartLine = true
                librariesStartLine = index
            }
            if (isFoundLibrariesStartLine) librariesContents.add(content)
        }
        if (pluginsContents.isNotEmpty())
            Yaml.loadFromStringAsNode(pluginsContents.joinToContent()).forEach { (_, rootNode) ->
                rootNode.asMap()?.forEach { (notation, artifactNode) ->
                    if (artifactNode.asMap()?.isKeyExist(VERSION_REF_NODE_NAME) == false)
                        pluginsMapping.add(DependencyMapping(notation.content))
                }
            }
        if (librariesContents.isNotEmpty())
            Yaml.loadFromStringAsNode(librariesContents.joinToContent()).forEach { (_, rootNode) ->
                rootNode.asMap()?.forEach { (groupId, libraryNode) ->
                    libraryNode.asMap()?.forEach { (artifactId, artifactNode) ->
                        val notation = spliceToDependencyNotation(groupId.content, artifactId.content)
                        if (artifactNode.asMap()?.isKeyExist(VERSION_REF_NODE_NAME) == false)
                            librariesMapping.add(DependencyMapping(notation))
                    }
                }
            }
        pluginsContents.onEachIndexed { index, content ->
            if ((content.trim().startsWith(VERSION_NODE_CONTENT) && content.startsWith(SPACE_OF_4)).not()) return@onEachIndexed
            pluginsMapping[pluginsLine].versionLine = index + pluginsStartLine
            pluginsLine++
        }.clear()
        librariesContents.onEachIndexed { index, content ->
            if ((content.trim().startsWith(VERSION_NODE_CONTENT) && content.startsWith(SPACE_OF_6)).not()) return@onEachIndexed
            librariesMapping[librariesLine].versionLine = index + librariesStartLine
            librariesLine++
        }.clear()
    }

    /**
     * 使用测绘数据更新依赖版本内容
     * @param dependencies 需要更新的依赖名称和版本数组
     */
    internal fun updateDependencies(dependencies: DependencyUpdateMap) {
        /**
         * 写入更新的依赖数据到文件内容
         * @param dependencies 依赖数组
         * @param spaceContent 空格内容
         */
        fun List<DependencyMapping>.dumpToContents(dependencies: DependencyUpdateMap, spaceContent: String) =
            filter { dependencies.containsKey(it.notation) }.forEach {
                var codeNote = ""
                val originContent = configFileContents[it.versionLine]
                if (originContent.contains("#")) originContent.indexOf("#")
                    .also { e -> if (e > 0) codeNote = originContent.substring(e - 1..originContent.lastIndex) }
                configFileContents[it.versionLine] = "$spaceContent$VERSION_NODE_CONTENT ${dependencies[it.notation]?.second?.mapped}$codeNote"
            }

        val plugins = dependencies.filter { it.value.first.type == DependencyName.Type.PLUGIN }
        val libraries = dependencies.filter { it.value.first.type == DependencyName.Type.LIBRARY }
        if (plugins.isNotEmpty()) pluginsMapping.dumpToContents(plugins, SPACE_OF_4)
        if (libraries.isNotEmpty()) librariesMapping.dumpToContents(libraries, SPACE_OF_6)
        if (configFileContents.isNotEmpty()) configFile?.writeText(buildString { configFileContents.forEach { append("$it\n") } }.trim())
    }
}