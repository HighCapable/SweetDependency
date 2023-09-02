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
 * This file is Created by fankes on 2023/5/27.
 */
@file:Suppress("unused", "USELESS_CAST", "KotlinRedundantDiagnosticSuppress")

package com.highcapable.sweetdependency.gradle.factory

import com.highcapable.sweetdependency.exception.SweetDependencyUnresolvedException
import com.highcapable.sweetdependency.utils.camelcase
import com.highcapable.sweetdependency.utils.debug.SError
import org.gradle.api.Action
import org.gradle.api.plugins.ExtensionAware

/**
 * 创建、获取扩展方法
 * @param name 方法名称 - 自动调用 [toSafeExtName]
 * @param clazz 目标对象 [Class]
 * @param args 方法参数
 * @return [ExtensionAware]
 */
internal fun ExtensionAware.getOrCreate(name: String, clazz: Class<*>, vararg args: Any?) = name.toSafeExtName().let { sName ->
    runCatching { extensions.create(sName, clazz, *args).asExtension() }.getOrElse {
        if ((it is IllegalArgumentException && it.message?.startsWith("Cannot add extension with name") == true).not()) throw it
        runCatching { extensions.getByName(sName).asExtension() }.getOrNull() ?: SError.make("Create or get extension failed with name \"$sName\"")
    }
}

/**
 * 创建、获取扩展方法 - 目标对象 [T]
 * @param name 方法名称 - 自动调用 [toSafeExtName]
 * @param args 方法参数
 * @return [T]
 */
internal inline fun <reified T> ExtensionAware.getOrCreate(name: String, vararg args: Any?) = name.toSafeExtName().let { sName ->
    runCatching { extensions.create(sName, T::class.java, *args) as T }.getOrElse {
        if ((it is IllegalArgumentException && it.message?.startsWith("Cannot add extension with name") == true).not()) throw it
        runCatching { extensions.getByName(sName) as? T? }.getOrNull() ?: SError.make("Create or get extension failed with name \"$sName\"")
    }
}

/**
 * 获取扩展方法
 * @param name 方法名称
 * @return [ExtensionAware]
 */
internal fun ExtensionAware.get(name: String) =
    runCatching { extensions.getByName(name).asExtension() }.getOrNull() ?: SError.make("Could not get extension with name \"$name\"")

/**
 * 获取扩展方法 - 目标对象 [T]
 * @param name 方法名称
 * @return [T]
 */
internal inline fun <reified T> ExtensionAware.get(name: String) =
    runCatching { extensions.getByName(name) as T }.getOrNull() ?: SError.make("Could not get extension with name \"$name\"")

/**
 * 获取扩展方法 - 目标对象 [T]
 * @return [T]
 */
internal inline fun <reified T> ExtensionAware.get() =
    runCatching { extensions.getByType(T::class.java) as T }.getOrNull() ?: SError.make("Could not get extension with type ${T::class.java}")

/**
 * 配置扩展方法 - 目标对象 [T]
 * @param name 方法名称
 * @param configure 配置方法体
 */
internal inline fun <reified T> ExtensionAware.configure(name: String, configure: Action<T>) = extensions.configure(name, configure)

/**
 * 是否存在扩展方法
 * @param name 方法名称
 * @return [Boolean]
 */
internal fun ExtensionAware.hasExtension(name: String) = runCatching { extensions.getByName(name); true }.getOrNull() ?: false

/**
 * 转换到扩展方法类型 [ExtensionAware]
 * @return [ExtensionAware]
 * @throws SweetDependencyUnresolvedException 如果类型不是 [ExtensionAware]
 */
internal fun Any.asExtension() = this as? ExtensionAware? ?: SError.make("This instance \"$this\" is not a valid Extension")

/**
 * 由于 Gradle 存在一个 [ExtensionAware] 的扩展
 *
 * 此功能用于检测当前字符串是否为 Gradle 使用的关键字名称
 * @return [Boolean]
 */
internal fun String.isUnSafeExtName() = camelcase().let { it == "ext" || it == "extra" || it == "extraProperties" || it == "extensions" }

/**
 * 由于 Gradle 存在一个 [ExtensionAware] 的扩展
 *
 * 此功能用于转换不符合规定的字符串到 "{字符串}s"
 * @return [String]
 */
internal fun String.toSafeExtName() = if (isUnSafeExtName()) "${this}s" else this