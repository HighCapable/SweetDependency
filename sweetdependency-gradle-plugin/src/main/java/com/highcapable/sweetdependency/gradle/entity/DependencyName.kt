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
 * This file is created by fankes on 2023/8/16.
 */
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.highcapable.sweetdependency.gradle.entity

import com.highcapable.sweetdependency.document.factory.convertToDependencyAmbiguousName
import com.highcapable.sweetdependency.document.factory.convertToDependencyUrlName
import com.highcapable.sweetdependency.document.factory.spliceToDependencyNotation
import com.highcapable.sweetdependency.document.factory.splitToDependencyNames
import com.highcapable.sweetdependency.utils.firstNumberToLetter

/**
 * 依赖名称实体类
 * @param type 名称类型
 * @param groupId Group ID
 * @param artifactId Artifact ID
 */
internal class DependencyName private constructor(internal val type: Type, internal val groupId: String, internal val artifactId: String) {

    internal companion object {

        /** 标识 Gradle 插件后缀名称 */
        private const val GRADLE_PLUGIN_SUFFIX = "gradle.plugin"

        /**
         * 创建为插件依赖名称
         * @param notation 完整名称
         */
        internal fun plugin(notation: String) = DependencyName(Type.PLUGIN, notation, "$notation.$GRADLE_PLUGIN_SUFFIX")

        /**
         * 创建为库依赖名称
         * @param notation 完整名称
         */
        internal fun library(notation: String) = notation.splitToDependencyNames().let { names -> DependencyName(Type.LIBRARY, names[0], names[1]) }

        /**
         * 创建为库依赖名称
         * @param groupId Group ID
         * @param artifactId Artifact ID
         */
        internal fun library(groupId: String, artifactId: String) = DependencyName(Type.LIBRARY, groupId, artifactId)
    }

    /**
     * 获取当前模糊分离名称 (使用 [symbol] 进行分离)
     * @param symbol 分隔符 - 默认 "."
     * @param isReplaceFirstChar 是否使用 [firstNumberToLetter] 替换每一段第一个字符 - 默认否
     * @param isLowerCase 是否全部转换为小写 - 默认是
     * @return [String]
     */
    internal fun ambiguousName(symbol: String = ".", isReplaceFirstChar: Boolean = false, isLowerCase: Boolean = true) =
        current.convertToDependencyAmbiguousName(symbol, isReplaceFirstChar, isLowerCase)

    /**
     * 获取当前 URL 名称
     * @return [String]
     */
    internal val urlName get() = notation.convertToDependencyUrlName()

    /**
     * 获取当前描述内容
     * @return [String]
     */
    internal val description get() = "$typeName \"$current\""

    /**
     * 获取当前类型名称
     * @return [String]
     */
    internal val typeName get() = when (type) {
        Type.PLUGIN -> "Plugin"
        Type.LIBRARY -> "Library"
    }

    /**
     * 获取当前名称
     * @return [String]
     */
    internal val current get() = when (type) {
        Type.PLUGIN -> groupId
        Type.LIBRARY -> notation
    }

    /**
     * 获取当前完整名称
     * @return [String]
     */
    internal val notation get() = spliceToDependencyNotation(groupId, artifactId)

    override fun equals(other: Any?) = other.toString() == toString()

    override fun hashCode() = toString().hashCode()

    override fun toString() = current

    /**
     * 名称类型定义类
     */
    internal enum class Type {
        /** 插件依赖 */
        PLUGIN,

        /** 库依赖 */
        LIBRARY
    }
}