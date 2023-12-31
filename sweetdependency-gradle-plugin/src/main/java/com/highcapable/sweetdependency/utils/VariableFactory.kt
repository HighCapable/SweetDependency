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
 * This file is created by fankes on 2023/6/14.
 */
package com.highcapable.sweetdependency.utils

/**
 * 允许 [MutableList] 进行 [orEmpty] 操作后返回 [MutableList]
 * @return [MutableList]<[T]>
 */
internal fun <T> MutableList<T>?.orEmpty() = this ?: emptyList<T>().toMutableList()

/**
 * 允许 [MutableList] 进行 [filter] 操作后返回 [MutableList]
 * @param predicate 方法体
 * @return [MutableList]<[T]>
 */
internal inline fun <T> MutableList<out T>.filter(predicate: (T) -> Boolean) = filterTo(mutableListOf(), predicate).toMutableList()

/**
 * 允许 [MutableMap] 进行 [filter] 操作后返回 [MutableMap]
 * @param predicate 方法体
 * @return [MutableMap]<[K], [V]>
 */
internal inline fun <K, V> MutableMap<out K, V>.filter(predicate: (Map.Entry<K, V>) -> Boolean) = filterTo(mutableMapOf(), predicate).toMutableMap()

/**
 * 获取 [MutableMap] 第一位元素 (数组为空返回 null)
 * @return [MutableMap.MutableEntry]<[K], [V]> or null
 */
internal fun <K, V> MutableMap<K, V>.single() = entries.firstOrNull()

/**
 * 当数组不为空时返回非空
 * @return [T] or null
 */
internal inline fun <reified T : Collection<*>> T.noEmpty() = takeIf { it.isNotEmpty() }

/**
 * 当字符串不为空白时返回非空
 * @return [T] or null
 */
internal inline fun <reified T : CharSequence> T.noBlank() = takeIf { it.isNotBlank() }

/**
 * 判断数组中是否存在重复元素
 * @return [Boolean]
 */
internal fun List<*>.hasDuplicate() = distinct().size != size

/**
 * 查找数组中的重复元素
 * @return [List]<[T]>
 */
internal inline fun <reified T> List<T>.findDuplicates() = distinct().filter { e -> count { it == e } > 1 }.distinct()

/**
 * 字符串数组转换为内容字符串
 * @return [String]
 */
internal fun List<String>.joinToContent() = joinToString("\n").trim()

/**
 * 空格字符串数组转换为 [MutableList]
 * @return [MutableList]<[String]>
 */
internal fun String.toSpaceList() = when {
    contains(" ") -> replace("\\s+".toRegex(), " ").split(" ").toMutableList()
    isNotBlank() -> mutableListOf(this)
    else -> mutableListOf()
}

/**
 * 下划线、分隔线、点、空格命名字符串转小驼峰命名字符串
 * @return [String]
 */
internal fun String.camelcase() = runCatching {
    split("_", ".", "-", " ").map { it.replaceFirstChar { e -> e.titlecase() } }.let { words ->
        words.first().replaceFirstChar { it.lowercase() } + words.drop(1).joinToString("")
    }
}.getOrNull() ?: this

/**
 * 下划线、分隔线、点、空格命名字符串转大驼峰命名字符串
 * @return [String]
 */
internal fun String.uppercamelcase() = camelcase().capitalize()

/**
 * 字符串首字母大写
 * @return [String]
 */
internal fun String.capitalize() = replaceFirstChar { it.uppercaseChar() }

/**
 * 字符串首字母小写
 * @return [String]
 */
internal fun String.uncapitalize() = replaceFirstChar { it.lowercaseChar() }

/**
 * 转换字符串第一位数字到外观近似大写字母
 * @return [String]
 */
internal fun String.firstNumberToLetter() =
    if (isNotBlank()) (mapOf(
        '0' to 'O', '1' to 'I',
        '2' to 'Z', '3' to 'E',
        '4' to 'A', '5' to 'S',
        '6' to 'G', '7' to 'T',
        '8' to 'B', '9' to 'P'
    )[first()] ?: first()) + substring(1)
    else this

/**
 * 转换字符串为非 Java 关键方法引用名称
 * @return [String]
 */
internal fun String.toNonJavaName() = if (lowercase() == "class") replace("lass", "lazz") else this

/**
 * 字符串中是否存在插值符号 ${...}
 * @return [Boolean]
 */
internal fun String.hasInterpolation() = contains("\${") && contains("}")

/**
 * 替换字符串中的插值符号 ${...}
 * @param result 回调结果
 * @return [String]
 */
internal fun String.replaceInterpolation(result: (groupValue: String) -> CharSequence) =
    "\\$\\{(.+?)}".toRegex().replace(this) { result(it.groupValues[1]) }