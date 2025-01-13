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
 * This file is created by fankes on 2023/6/6.
 */
package com.highcapable.sweetdependency.manager.maven

import com.highcapable.sweetdependency.document.RepositoryDocument
import com.highcapable.sweetdependency.gradle.entity.DependencyName
import com.highcapable.sweetdependency.gradle.entity.DependencyVersion
import com.highcapable.sweetdependency.gradle.helper.GradleHelper
import com.highcapable.sweetdependency.manager.maven.entity.MavenMetadata
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.executeFileBody
import com.highcapable.sweetdependency.utils.executeUrlBody
import com.highcapable.sweetdependency.utils.noEmpty
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Maven 解析器工具类
 */
internal object MavenParser {

    /** 依赖配置文件名 */
    private const val METADATA_FILE_NAME = "maven-metadata.xml"

    /** 依赖配置文件名 (本地) */
    private const val METADATA_LOCAL_FILE_NAME = "maven-metadata-local.xml"

    /**
     * 通过依赖全称使用指定存储库得到 [MavenMetadata] 实体
     * @param dependencyName 依赖名称
     * @param repo 当前存储库实体
     * @param currentVersion 当前依赖版本
     * @return [MavenMetadata]
     */
    internal fun acquire(dependencyName: DependencyName, repo: RepositoryDocument, currentVersion: DependencyVersion): MavenMetadata {
        val headerUrlOrPath = "${repo.url.ifBlank { repo.path }}/${dependencyName.urlName}/"
        val isIncludeScope = repo.isIncludeScope(dependencyName.type == DependencyName.Type.PLUGIN)
        /** 离线模式下不会自动装配、更新在线依赖 */
        if (isIncludeScope && GradleHelper.isOfflineMode) return MavenMetadata()
        var currentUrl: String
        return when {
            repo.url.isNotBlank() -> "$headerUrlOrPath$METADATA_FILE_NAME"
                .apply { currentUrl = this }
                .executeUrlBody(repo.credentials.username, repo.credentials.password)
            repo.path.isNotBlank() -> "$headerUrlOrPath$METADATA_LOCAL_FILE_NAME"
                .apply { currentUrl = this }
                .executeFileBody()
            else -> SError.make("Could not resolve this repository \"${repo.nodeName}\"")
        }.trim().toMetadata(currentUrl, currentVersion)
    }

    /**
     * 解析 [METADATA_FILE_NAME]、[METADATA_LOCAL_FILE_NAME] 内容到 [MavenMetadata] 实体
     * @param url 当前依赖获取 URL
     * @param currentVersion 当前依赖版本
     * @return [MavenMetadata]
     */
    private fun String.toMetadata(url: String, currentVersion: DependencyVersion) = runCatching {
        if (!(contains("<metadata ") || contains("<metadata>")) || !endsWith("</metadata>")) return@runCatching MavenMetadata(url)
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(this))).let { document ->
            val lastUpdated = document.getElementsByTagName("lastUpdated").item(0)?.textContent?.toLongOrNull() ?: 0L
            val versionNodeList = document.getElementsByTagName("version")
            val versions = mutableListOf<DependencyVersion>()
            for (i in 0..versionNodeList.length) versionNodeList.item(i)?.textContent?.also { versions.add(currentVersion.clone(it)) }
            MavenMetadata(url, versions.noEmpty()?.reversed()?.toMutableList() ?: mutableListOf(), lastUpdated)
        }
    }.getOrNull() ?: MavenMetadata(url)
}