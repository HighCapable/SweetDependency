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
 * This file is created by fankes on 2023/5/18.
 */
@file:Suppress("unused")

package com.highcapable.sweetdependency.document

import com.highcapable.sweetdependency.gradle.entity.DependencyVersion
import com.highcapable.sweetdependency.manager.content.Repositories
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.toSpaceList
import com.highcapable.sweetdependency.utils.yaml.proxy.IYamlDocument
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 依赖每项文档实体
 * @param alias 别名
 * @param version 版本
 * @param versionRef 版本引用
 * @param versions 版本别名数组
 * @param isAutoUpdate 是否自动更新
 * @param versionFilter 版本过滤器文档实体
 * @param repositories 指定使用的存储库名称
 */
@Serializable
internal data class DependencyDocument(
    @SerialName("alias")
    internal var alias: String = "",
    @SerialName("version")
    internal var version: String = "",
    @SerialName("version-ref")
    internal var versionRef: String = "",
    @SerialName("versions")
    internal var versions: MutableMap<String, String> = mutableMapOf(),
    @SerialName("auto-update")
    internal var isAutoUpdate: Boolean = true,
    @SerialName("version-filter")
    internal var versionFilter: VersionFilterDocument? = null,
    @SerialName("repositories")
    internal var repositories: String = ""
) : IYamlDocument {

    /**
     * 获取版本
     * @return [DependencyVersion]
     */
    internal fun version() = DependencyVersion(version)

    /**
     * 获取版本别名数组
     * @return <[MutableMap]><[String], [DependencyVersion]>
     */
    internal fun versions() = mutableMapOf<String, DependencyVersion>().also {
        versions.forEach { (key, value) -> it[key] = DependencyVersion(value.replace(DependencyVersion.LATEST_VERSION_NAME, version)) }
    }

    /**
     * 更新版本
     * @param newVersion 新版本
     */
    internal fun updateVersion(newVersion: DependencyVersion) {
        version = newVersion.current
    }

    /**
     * 更新版本
     * @param document 当前文档实例
     */
    internal fun updateVersion(document: DependencyDocument) {
        version = document.version
    }

    /**
     * 获取指定使用的存储库数组
     * @return [MutableList]<[RepositoryDocument]>
     */
    internal fun repositories() = mutableListOf<RepositoryDocument>().apply {
        repositories.toSpaceList().forEach {
            add(Repositories.all().firstOrNull { e -> e.nodeName == it } ?: SError.make("Could not found repository with name \"$it\""))
        }
    }.distinctBy { it.url.ifBlank { it.path } }.toMutableList()
}