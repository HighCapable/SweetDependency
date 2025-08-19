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
 * This file is created by fankes on 2023/5/28.
 */
@file:Suppress("USELESS_ELVIS", "KotlinRedundantDiagnosticSuppress")

package com.highcapable.sweetdependency.gradle.wrapper

import com.highcapable.sweetdependency.document.factory.spliceToDependencyNotation
import com.highcapable.sweetdependency.gradle.delegate.entity.ExternalDependencyDelegate
import com.highcapable.sweetdependency.gradle.entity.DependencyVersion
import com.highcapable.sweetdependency.gradle.wrapper.type.LibraryDependencyType
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ProjectDependency
import java.io.File

/**
 * 库依赖包装实例实现类
 * @param instance 当前实例
 * @param configurationName 配置名称
 */
internal data class LibraryDependencyWrapper internal constructor(private val instance: Dependency, internal val configurationName: String) {

    /**
     * 获取当前依赖类型
     * @return [LibraryDependencyType]
     */
    val type
        get() = when (instance) {
            is ExternalDependencyDelegate -> LibraryDependencyType.EXTERNAL_DELEGATE
            is ExternalDependency -> LibraryDependencyType.EXTERNAL
            is ProjectDependency -> LibraryDependencyType.PROJECT
            is FileCollectionDependency -> LibraryDependencyType.FILES
            else -> LibraryDependencyType.OTHERS
        }

    /**
     * 依赖的文件数组
     *
     * - [type] 需要为 [LibraryDependencyType.FILES] 否则始终为 null
     * @return [MutableSet]<[File]> or null
     */
    val files get() = runCatching { (instance as? FileCollectionDependency?)?.files?.files?.toMutableSet() }.getOrNull()

    /**
     * 依赖的项目
     *
     * - [type] 需要为 [LibraryDependencyType.PROJECT] 否则始终为 null
     * @return [Project] or null
     */
    // FIXME: https://stackoverflow.com/questions/79619019/how-replace-deprecated-getdependencyproject-in-a-backwards-compatible-way
    val project get() = runCatching { (instance as? ProjectDependency?)?.dependencyProject }.getOrNull()

    /**
     * Group ID
     * @return [String]
     */
    val groupId get() = instance.group ?: ""

    /**
     * Artifact ID
     * @return [String]
     */
    val artifactId get() = instance.name ?: ""

    /**
     * 版本
     * @return [DependencyVersion]
     */
    val version get() = DependencyVersion(instance.version ?: "")

    override fun toString() = spliceToDependencyNotation(groupId, artifactId)
}