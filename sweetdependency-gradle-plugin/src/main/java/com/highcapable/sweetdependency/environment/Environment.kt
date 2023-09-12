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
 * This file is created by fankes on 2023/6/28.
 */
package com.highcapable.sweetdependency.environment

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.generated.SweetDependencyProperties
import com.highcapable.sweetdependency.gradle.helper.GradleHelper
import com.highcapable.sweetdependency.utils.toFile
import java.io.File

/**
 * [SweetDependency] 环境工具类
 */
internal object Environment {

    /** [SweetDependency] 缓存存放目录 */
    private const val MEMORY_DIR_PATH = ".gradle/${SweetDependencyProperties.PROJECT_MODULE_NAME}"

    /** [SweetDependency] 功能存放目录 */
    private const val RESOURCES_DIR_PATH = "gradle/${SweetDependencyProperties.PROJECT_MODULE_NAME}"

    /**
     * 获取 [SweetDependency] 缓存存放目录
     * @return [File]
     */
    private val memoryDir get() = "${GradleHelper.rootDir.absolutePath}/$MEMORY_DIR_PATH".toFile().also { if (it.exists().not()) it.mkdirs() }

    /**
     * 获取 [SweetDependency] 功能存放目录
     * @return [File]
     */
    private val resourcesDir get() = "${GradleHelper.rootDir.absolutePath}/$RESOURCES_DIR_PATH".toFile().also { if (it.exists().not()) it.mkdirs() }

    /**
     * 获取系统信息
     * @return [String]
     */
    internal val systemInfo get() = "${System.getProperty("os.name")} ${System.getProperty("os.version")}"

    /**
     * 获取字符集名称
     * @return [String]
     */
    internal val characterEncoding get() = System.getProperty("file.encoding")

    /**
     * 获取 Java 版本
     * @return [String]
     */
    internal val javaVersion get() = System.getProperty("java.version")

    /**
     * 获取 [SweetDependency] 缓存存放目录
     * @param dirOrFileName 子路径目录、文件名称数组
     * @return [File]
     */
    internal fun memoryDir(vararg dirOrFileName: String) = memoryDir.parseDir(*dirOrFileName)

    /**
     * 获取 [SweetDependency] 功能存放目录
     * @param dirOrFileName 子路径目录、文件名称数组
     * @return [File]
     */
    internal fun resourcesDir(vararg dirOrFileName: String) = resourcesDir.parseDir(*dirOrFileName)

    /**
     * 解析 [SweetDependency] 存放目录
     * @param dirOrFileName 子路径目录、文件名称数组
     * @return [File]
     */
    private fun File.parseDir(vararg dirOrFileName: String): File {
        var splitPath = ""
        dirOrFileName.forEach { splitPath += "$it/" }
        return "$absolutePath/${splitPath.ifBlank { "/" }.dropLast(1)}".toFile()
    }
}