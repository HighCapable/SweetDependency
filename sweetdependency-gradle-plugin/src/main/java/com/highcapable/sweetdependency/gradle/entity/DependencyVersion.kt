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
 * This file is created by fankes on 2023/6/16.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package com.highcapable.sweetdependency.gradle.entity

import com.highcapable.sweetdependency.utils.debug.SError

/**
 * 依赖版本实体类
 * @param actual 当前实际版本 - 如果为 [DependencyVersion.isOptional] 则会显示在第一位
 * @param optionalType 可选更新类型 - 默认为 [DependencyVersion.OptionalUpdateType.NONE]
 */
internal class DependencyVersion(internal var actual: String, optionalType: OptionalUpdateType = OptionalUpdateType.NONE) {

    internal companion object {

        /** 标识自动装配版本的名称 */
        const val AUTOWIRE_VERSION_NAME = "+"

        /** 标识不指定版本的名称 */
        const val NO_SPECIFIC_VERSION_NAME = "<no-spec>"

        /** 标识当前最新版本的名称 */
        const val LATEST_VERSION_NAME = "<latest>"

        /** 标识常规可选更新版本前缀名称 */
        private const val OPTIONAL_VERSION_NORMAL_PREFIX = "^"

        /** 标识常驻可选更新版本前缀名称 */
        private const val OPTIONAL_VERSION_PERMANENT_PREFIX = "^^"
    }

    init {
        if (current.startsWith("<") && current.endsWith(">"))
            if (current != NO_SPECIFIC_VERSION_NAME && current != LATEST_VERSION_NAME)
                SError.make("The parameter \"$current\" is not recognized as any available function")
        if (!isOptional) when (optionalType) {
            OptionalUpdateType.NONE -> {}
            OptionalUpdateType.NORMAL -> actual = "$OPTIONAL_VERSION_NORMAL_PREFIX$actual"
            OptionalUpdateType.PERMANENT -> actual = "$OPTIONAL_VERSION_PERMANENT_PREFIX$actual"
        }
    }

    /**
     * 获取当前版本
     * @return [String]
     */
    internal val current get() = actual.replace(OPTIONAL_VERSION_PERMANENT_PREFIX, "").replace(OPTIONAL_VERSION_NORMAL_PREFIX, "")

    /**
     * 获取当前测绘数据使用的版本
     *
     * 它会自动识别 [optionalType] 决定是否继续保留可选更新的符号
     * @return [String]
     */
    internal val mapped get() = when (optionalType) {
        OptionalUpdateType.PERMANENT -> actual
        else -> current
    }

    /**
     * 获取固定存在的版本
     *
     * 满足以下情况
     *
     * - [isBlank]
     * - [isNoSpecific]
     *
     * 这些情况都会返回 [AUTOWIRE_VERSION_NAME]
     *
     * 其余情况会返回 [current]
     * @return [String]
     */
    internal val fixed get() = when {
        isBlank || isNoSpecific -> AUTOWIRE_VERSION_NAME
        else -> current
    }

    /**
     * 获取部署版本
     *
     * 如果为 [isNoSpecific] 则会返回空
     * @return [String]
     */
    internal val deployed get() = current.takeIf { !isNoSpecific } ?: ""

    /**
     * 获取存在版本
     *
     * 如果为空则会返回 [NO_SPECIFIC_VERSION_NAME]
     * @return [String]
     */
    internal val existed get() = current.ifBlank { NO_SPECIFIC_VERSION_NAME }

    /**
     * 是否为空白
     * @return [Boolean]
     */
    internal val isBlank get() = current.isBlank()

    /**
     * 是否为自动装配版本
     * @return [Boolean]
     */
    internal val isAutowire get() = current == AUTOWIRE_VERSION_NAME

    /**
     * 是否为不指定版本
     * @return [Boolean]
     */
    internal val isNoSpecific get() = current == NO_SPECIFIC_VERSION_NAME

    /**
     * 是否为可选更新版本
     * @return [Boolean]
     */
    internal val isOptional get() = optionalType != OptionalUpdateType.NONE

    /**
     * 获取当前可选更新类型
     * @return [OptionalUpdateType]
     */
    internal val optionalType get() = when {
        actual.startsWith(OPTIONAL_VERSION_PERMANENT_PREFIX) -> OptionalUpdateType.PERMANENT
        actual.startsWith(OPTIONAL_VERSION_NORMAL_PREFIX) -> OptionalUpdateType.NORMAL
        else -> OptionalUpdateType.NONE
    }

    /**
     * 克隆为新的 [DependencyVersion] 实体
     * @param version 当前版本
     * @return [DependencyVersion]
     */
    internal fun clone(version: String) = DependencyVersion(version, optionalType)

    override fun equals(other: Any?) = other.toString() == toString()

    override fun hashCode() = toString().hashCode()

    override fun toString() = current

    /**
     * 可选更新类型定义类
     */
    internal enum class OptionalUpdateType {
        /** 无 */
        NONE,

        /** 常规 */
        NORMAL,

        /** 常驻 */
        PERMANENT,
    }
}