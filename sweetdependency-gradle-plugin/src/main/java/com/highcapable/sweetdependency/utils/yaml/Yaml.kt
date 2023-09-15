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
 * This file is created by fankes on 2023/5/17.
 */
@file:Suppress("unused")

package com.highcapable.sweetdependency.utils.yaml

import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.yamlMap
import com.highcapable.sweetdependency.gradle.helper.GradleHelper
import com.highcapable.sweetdependency.utils.hasInterpolation
import com.highcapable.sweetdependency.utils.replaceInterpolation
import com.highcapable.sweetdependency.utils.toFile
import com.highcapable.sweetdependency.utils.yaml.factory.YamlMapEntries
import com.highcapable.sweetdependency.utils.yaml.proxy.IYamlDocument
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.serializer
import com.charleskorn.kaml.Yaml as Kaml

/**
 * YAML 文档处理类
 */
internal object Yaml {

    /**
     * 获取 [Kaml] 对象
     * @return [Kaml]
     */
    private val kaml by lazy { Kaml(configuration = YamlConfiguration(encodeDefaults = false)) }

    /**
     * 从文件解析到 [IYamlDocument]
     * @param path 文件路径
     * @return [T]
     */
    internal inline fun <reified T : IYamlDocument> loadFromFile(path: String) = loadFromString<T>(path.toFile().readText())

    /**
     * 从字符串解析到 [IYamlDocument]
     * @param string 字符串
     * @return [T]
     */
    internal inline fun <reified T : IYamlDocument> loadFromString(string: String) = kaml.decodeFromString<T>(string.flattened())

    /**
     * 从文件解析到 [YamlMapEntries]
     * @param path 文件路径
     * @return [YamlMapEntries]
     */
    internal fun loadFromFileAsNode(path: String) = loadFromStringAsNode(path.toFile().readText())

    /**
     * 从字符串解析到 [YamlMapEntries]
     * @param string 字符串
     * @return [YamlMapEntries]
     */
    internal fun loadFromStringAsNode(string: String) = kaml.parseToYamlNode(string.flattened()).yamlMap.entries

    /**
     * 序列化 [IYamlDocument] 到文件
     * @param path 文件路径
     * @param formatter 回调字符串格式化方式
     */
    internal inline fun <reified T : IYamlDocument> parseToFile(doc: T, path: String, formatter: String.() -> String = { this }) =
        path.toFile().writeText(kaml.encodeToString(serializer(), doc).let(formatter))

    /**
     * 字符串平坦化处理
     *
     * - 去除字符串中以 # 开头的注释行并去除空行
     * - 调用 [interpFromEnv] 解析可被插值的字符串
     * @return [String]
     */
    private fun String.flattened() = trimIndent()
        .replace("(^|\\s)#.*".toRegex(), "")
        .replace("(?m)^\\s*$(\\n|\\r\\n?)".toRegex(), "")
        .let { if (it.hasInterpolation()) it.interpFromEnv() else it }
        .trim()

    /**
     * 将系统属性资源值插入到当前字符串中
     *
     * 形如：${...}
     *
     * 会按照以下顺序进行查找 ↓
     *
     * - 项目 properties
     * - 用户 properties
     * - 系统 properties
     * - 系统环境变量
     * @return [String]
     */
    private fun String.interpFromEnv() = replaceInterpolation { key ->
        GradleHelper.projectProperties?.get(key)?.toString()
            ?: GradleHelper.userProperties?.get(key)?.toString()
            ?: runCatching { System.getProperties()[key]?.toString() }.getOrNull()
            ?: runCatching { System.getenv(key) }.getOrNull() ?: ""
    }
}