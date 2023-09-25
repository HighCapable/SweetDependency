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
 * This file is created by fankes on 2023/7/30.
 */
package com.highcapable.sweetdependency.plugin.helper

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.generated.SweetDependencyProperties
import com.highcapable.sweetdependency.gradle.helper.GradleHelper
import com.highcapable.sweetdependency.utils.debug.SLog
import com.highcapable.sweetdependency.utils.executeUrlBody
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

/**
 * 插件自身检查更新工具类
 */
internal object PluginUpdateHelper {

    /** OSS Release URL 地址 */
    private const val SONATYPE_OSS_RELEASES_URL = "https://s01.oss.sonatype.org/content/repositories/releases"

    /** 依赖配置文件名 */
    private const val METADATA_FILE_NAME = "maven-metadata.xml"

    /** 插件自身依赖 URL 名称 */
    private val groupUrlNotation =
        "${SweetDependencyProperties.PROJECT_GROUP_NAME.replace(".","/")}/${SweetDependencyProperties.GRADLE_PLUGIN_MODULE_NAME}"

    /** 检查更新 URL 地址 */
    private val releaseUrl = "$SONATYPE_OSS_RELEASES_URL/$groupUrlNotation/$METADATA_FILE_NAME"

    /** 检查更新 */
    internal fun checkingForUpdate() {
        if (GradleHelper.isOfflineMode) return
        val latestVersion = releaseUrl.executeUrlBody(isShowFailure = false).trim().findLatest()
        if (latestVersion.isNotBlank() && latestVersion != SweetDependency.VERSION) SLog.note(
            """
              Plugin update is available, the current version is ${SweetDependency.VERSION}, please update to $latestVersion
              You can modify your plugin version in your project's settings.gradle / settings.gradle.kts
              plugins {
                  id("${SweetDependencyProperties.PROJECT_GROUP_NAME}") version "$latestVersion"
                  ...
              }
              For more information, you can visit ${SweetDependency.PROJECT_URL}
            """.trimIndent(), SLog.UP
        )
    }

    /**
     * 解析 [METADATA_FILE_NAME] 内容并获取 "latest"
     * @return [String]
     */
    private fun String.findLatest() = runCatching {
        if ((contains("<metadata ") || contains("<metadata>")).not() || endsWith("</metadata>").not()) return@runCatching ""
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(this))).let { document ->
            document.getElementsByTagName("latest")?.let { if (it.length > 0) it.item(0)?.textContent ?: "" else "" }
        }
    }.getOrNull() ?: ""
}