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
 * This file is created by fankes on 2023/6/23.
 */
package com.highcapable.sweetdependency.gradle.entity

/**
 * 外部存储库依赖实体类
 * @param dependencyName 依赖名称
 * @param version 版本
 */
internal data class ExternalDependency(private val dependencyName: DependencyName, internal val version: DependencyVersion) {

    /**
     * 获取 Group ID
     * @return [String]
     */
    internal val groupId get() = dependencyName.groupId

    /**
     * 获取 Artifact ID
     * @return [String]
     */
    internal val artifactId get() = dependencyName.artifactId

    override fun toString() = dependencyName.current
}