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
 * This file is created by fankes on 2023/5/17.
 */
@file:Suppress("unused")

package com.highcapable.sweetdependency.utils

import com.highcapable.sweetdependency.utils.debug.SError
import java.io.File
import java.nio.file.Paths
import java.util.zip.ZipFile

/**
 * 字符串路径转换为文件
 *
 * 自动调用 [parseFileSeparator]
 * @return [File]
 */
internal fun String.toFile() = File(parseFileSeparator())

/**
 * 格式化到当前操作系统的文件分隔符
 * @return [String]
 */
internal fun String.parseFileSeparator() = replace("/", File.separator).replace("\\", File.separator)

/**
 * 格式化到 Unix 操作系统的文件分隔符
 * @return [String]
 */
internal fun String.parseUnixFileSeparator() = replace("\\", "/")

/**
 * 字符串文件路径转换到相对文件路径
 * @param basePath 基于路径
 * @param rootPath 根路径 - 不填将不校验完整路径
 * @return [String]
 */
internal fun String.toRelativeFilePath(basePath: String, rootPath: String = "") =
    parseFileSeparator().runCatching {
        if (rootPath.isNotBlank() && !contains(rootPath)) return this
        return Paths.get(basePath).relativize(Paths.get(this)).toString()
    }.getOrNull() ?: parseFileSeparator()

/**
 * 字符串文件路径转换到绝对文件路径
 * @param basePath 基于路径
 * @return [String]
 */
internal fun String.toAbsoluteFilePath(basePath: String) =
    parseFileSeparator().runCatching {
        if (Paths.get(this).isAbsolute) return this
        Paths.get(basePath).resolve(Paths.get(this)).normalize().toString()
    }.getOrNull() ?: parseFileSeparator()

/**
 * 字符串文件路径转换到绝对文件路径数组
 * @param basePath 基于路径
 * @return [MutableList]<[String]>
 */
internal fun String.toAbsoluteFilePaths(basePath: String) =
    toAbsoluteFilePath(basePath).let { path ->
        mutableListOf<String>().apply {
            when {
                path.toFile().let { it.exists() && it.isFile } -> add(path)
                path.toFile().let { it.exists() && it.isDirectory } -> SError.make("The file path $path is a directory")
                else -> {
                    /**
                     * 是否匹配文件扩展名
                     * @param condition 条件
                     * @return [Boolean]
                     */
                    fun String.isMatch(condition: String) =
                        condition.let { if (it == "*") "*.*" else it }.replace(".", "\\.").replace("*", ".*").toRegex().matches(this)
                    val condition = path.split(File.separator)
                    if (path.contains(File.separator) && condition[condition.lastIndex].contains("*"))
                        path.toFile().parentFile?.listFiles()?.forEach { if (it.name.isMatch(condition[condition.lastIndex])) add(it.absolutePath) }
                    else SError.make("Could not resolve file path $path")
                }
            }
        }
    }

/**
 * 检查文件是否为合法的压缩包文件
 *
 * - 如果不是文件 (可能是目录) - 返回 true
 * - 如果文件不存在 - 返回 false
 * @return [Boolean]
 */
internal fun File.isValidZip(): Boolean {
    if (!isFile) return true
    if (!exists()) return false
    return runCatching { ZipFile(this).use {}; true }.getOrNull() ?: false
}

/**
 * 检查目录是否为空
 *
 * - 如果不是目录 (可能是文件) - 返回 true
 * - 如果文件不存在 - 返回 true
 * @return [Boolean]
 */
internal fun File.isEmpty() = !exists() || !isDirectory || listFiles().isNullOrEmpty()

/** 删除目录下的空子目录 */
internal fun File.deleteEmptyRecursively() {
    listFiles { file -> file.isDirectory }?.forEach { subDir ->
        subDir.deleteEmptyRecursively()
        if (subDir.listFiles()?.isEmpty() == true) subDir.delete()
    }
}

/**
 * 获取当前文件内容的字符串内容 (同步)
 * @return [String]
 */
internal fun String.executeFileBody() = runCatching { toFile().readText() }.getOrNull() ?: ""