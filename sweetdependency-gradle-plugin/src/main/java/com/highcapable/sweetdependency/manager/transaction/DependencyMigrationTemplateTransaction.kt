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
 * This file is created by fankes on 2023/6/1.
 */
package com.highcapable.sweetdependency.manager.transaction

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.document.DependencyDocument
import com.highcapable.sweetdependency.document.RootConfigDocument
import com.highcapable.sweetdependency.gradle.entity.DependencyVersion
import com.highcapable.sweetdependency.gradle.factory.fullName
import com.highcapable.sweetdependency.gradle.factory.libraries
import com.highcapable.sweetdependency.gradle.factory.plugins
import com.highcapable.sweetdependency.gradle.helper.GradleHelper
import com.highcapable.sweetdependency.gradle.wrapper.type.LibraryDependencyType
import com.highcapable.sweetdependency.manager.GradleTaskManager
import com.highcapable.sweetdependency.manager.content.Dependencies
import com.highcapable.sweetdependency.plugin.config.content.SweetDependencyConfigs
import com.highcapable.sweetdependency.utils.debug.SLog
import com.highcapable.sweetdependency.utils.parseFileSeparator
import com.highcapable.sweetdependency.utils.toFile
import com.highcapable.sweetdependency.utils.yaml.Yaml

/**
 * 依赖迁移模版管理类
 */
internal object DependencyMigrationTemplateTransaction {

    /** 模板文件头部内容 */
    private const val TEMPLATE_FILE_HEADER_CONTENT = """
        # SweetDependency project configuration template file
        # Template files are automatically generated using Gradle task "${GradleTaskManager.CREATE_DEPENDENCIES_MIGRATION_TEMPLATE_TASK_NAME}"
        # The automatically generated configuration is determined according to your project
        # Please adjust these contents at any time in actual use, the generated content is for reference only, and its availability is unknown
        # You can copy the content of the corresponding node in the template file to the project configuration file, and then delete this file
        # You can visit ${SweetDependency.PROJECT_URL} for more help
        #
        # SweetDependency 项目配置模板文件
        # 模版文件是使用 Gradle Task "${GradleTaskManager.CREATE_DEPENDENCIES_MIGRATION_TEMPLATE_TASK_NAME}" 自动生成的
        # 自动生成的配置根据你的项目决定，请在实际使用中随时调整这些内容，生成的内容仅供参考，其可用性未知
        # 你可以复制模板文件中对应节点的内容到项目配置文件，然后删除此文件
        # 你可以前往 ${SweetDependency.PROJECT_URL} 以获得更多帮助
        """

    /** 模板文件扩展名 */
    private const val TEMPLATE_FILE_EXT_NAME = "template.yaml"

    /** 模板文件头部内容 */
    private val templateFileHeaderContent = TEMPLATE_FILE_HEADER_CONTENT.trimIndent()

    /** 排除的部分内置插件名称前缀数组 */
    private val exclusionPluginPrefixs = arrayOf("org.gradle", "com.android.internal")

    /** 生成模板使用的文档实例 */
    private val document = RootConfigDocument()

    /** 创建模版 */
    internal fun createTemplate() {
        SLog.info("Starting analyze projects dependencies structure", SLog.ANLZE)
        GradleHelper.allProjects.forEach { subProject ->
            subProject.plugins().onEach {
                if (exclusionPluginPrefixs.any { prefix -> it.id.startsWith(prefix) }) return@onEach
                if (Dependencies.hasPlugin { key, _ -> key.current == it.id }) return@onEach
                if (document.plugins == null) document.plugins = mutableMapOf()
                val declareDocument = DependencyDocument(version = DependencyVersion.AUTOWIRE_VERSION_NAME)
                document.plugins?.set(it.id, declareDocument)
            }.apply { if (isNotEmpty()) SLog.info("Found $size plugins in project \"${subProject.fullName}\"", SLog.LINK) }
            subProject.libraries().onEach {
                if (Dependencies.hasLibrary { key, _ -> key.current == it.toString() }) return@onEach
                if (document.libraries == null) document.libraries = mutableMapOf()
                if (it.type == LibraryDependencyType.EXTERNAL) document.libraries?.also { entities ->
                    if (entities[it.groupId] == null) entities[it.groupId] = mutableMapOf()
                    val declareDocument = DependencyDocument(version = it.version.existed)
                    entities[it.groupId]?.set(it.artifactId, declareDocument)
                }
            }.apply { if (isNotEmpty()) SLog.info("Found $size libraries in project \"${subProject.fullName}\"", SLog.LINK) }
        }; saveTemplateFile()
    }

    /** 保存模版到文件 */
    private fun saveTemplateFile() {
        if (document.plugins?.isEmpty() == true) document.plugins = null
        if (document.libraries?.isEmpty() == true) document.libraries = null
        if (document.plugins?.isNotEmpty() == true || document.libraries?.isNotEmpty() == true) {
            val templateFilePath = SweetDependencyConfigs.configs.configFilePath
                .let { it.toFile().let { e -> "${e.parent}/${e.name.split(".")[0]}.$TEMPLATE_FILE_EXT_NAME" } }.parseFileSeparator()
            Yaml.parseToFile(document, templateFilePath) { "$templateFileHeaderContent\n\n${replace("\"", "")}" }
            SLog.info("Template file is created at $templateFilePath", SLog.DONE)
            document.plugins?.clear()
            document.libraries?.clear()
        } else SLog.info("No suitable dependencies can be found in all projects to create template file, nothing to do", SLog.IGNORE)
    }
}