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
 * This file is created by fankes on 2023/6/9.
 */
package com.highcapable.sweetdependency.document

import com.highcapable.sweetdependency.gradle.entity.DependencyVersion
import com.highcapable.sweetdependency.manager.content.Repositories
import com.highcapable.sweetdependency.utils.filter
import com.highcapable.sweetdependency.utils.toSpaceList
import com.highcapable.sweetdependency.utils.yaml.proxy.IYamlDocument
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 版本过滤器文档实体
 * @param isUseInternal 使用内置过滤器
 * @param exclusionList 排除列表
 */
@Serializable
internal data class VersionFilterDocument(
    @SerialName("use-internal")
    internal var isUseInternal: Boolean = true,
    @SerialName("exclusion-list")
    internal var exclusionList: String = ""
) : IYamlDocument {

    /**
     * 版本排除列表实体
     * @param list 当前排除列表数组
     */
    internal class ExclusionList internal constructor(private val list: MutableList<String>) {

        /**
         * 获取当前排除列表数组
         * @return [MutableList]<[String]>
         */
        internal fun all() = list

        /**
         * 当前是否存在排除列表
         * @return [Boolean]
         */
        internal fun isEmpty() = all().isEmpty()

        /**
         * 当前是否不存在排除列表
         * @return [Boolean]
         */
        internal fun isNotEmpty() = isEmpty().not()

        /**
         * 依赖于当前 [version] 提供的版本并在 [all] 中排除 (不区分大小写)
         *
         * 此操作会调用 [clone] 创建一个新实例并返回
         * @param version 当前版本
         * @return [ExclusionList]
         */
        internal fun depends(version: DependencyVersion) = clone().apply {
            if (version.isAutowire.not() && version.isBlank.not()) all().removeAll { version.deployed.lowercase().contains(it.lowercase()) }
        }

        /**
         * 使用 [all] 过滤当前版本字符串 (不区分大小写)
         * @param versions 当前版本字符串数组
         * @return [MutableList]<[DependencyVersion]>
         */
        internal fun filter(versions: MutableList<DependencyVersion>) =
            if (all().isEmpty()) versions else versions.filter { version -> all().none { version.current.lowercase().contains(it.lowercase()) } }

        /**
         * 克隆并创建一个新实例
         * @return [ExclusionList]
         */
        private fun clone() = ExclusionList(mutableListOf<String>().apply { addAll(all()) })

        override fun toString() = all().toString()
    }

    /**
     * 获取排除列表
     * @return [ExclusionList]
     */
    internal fun exclusionList() = ExclusionList(mutableListOf<String>().apply {
        if (isUseInternal) addAll(Repositories.defaultVersionFilterExclusionList)
        addAll(exclusionList.toSpaceList())
    })
}