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
 * This file is created by fankes on 2023/5/21.
 */
package com.highcapable.sweetdependency.manager.helper

import com.highcapable.sweetdependency.environment.Environment
import com.highcapable.sweetdependency.gradle.entity.DependencyName
import com.highcapable.sweetdependency.gradle.entity.DependencyVersion
import com.highcapable.sweetdependency.plugin.config.content.SweetDependencyConfigs
import com.highcapable.sweetdependency.utils.debug.SLog
import java.text.SimpleDateFormat
import java.util.*

/**
 * 依赖自动装配日志工具类
 */
internal object DependencyAutowireLogHelper {

    /** 当前日志文件名 */
    private const val LOG_FILE_NAME = "dependencies-autowire.log"

    /**
     * 当前日志文件
     * @return [String]
     */
    internal val logFile get() = Environment.memoryDir(LOG_FILE_NAME)

    /**
     * 记录当前依赖改变
     * @param dependencyName 依赖名称
     * @param fromVersion 起始版本
     * @param toVersion 最终版本
     */
    internal fun record(dependencyName: DependencyName, fromVersion: DependencyVersion, toVersion: DependencyVersion) {
        if (!SweetDependencyConfigs.configs.isEnableDependenciesAutowireLog) return
        val versionInfo = if (fromVersion.isAutowire)
            "autowire version \"$toVersion\""
        else "update version \"$fromVersion\" -> \"$toVersion\""
        logFile.runCatching {
            appendText("[${SimpleDateFormat.getDateTimeInstance().format(Date())}] ${dependencyName.description} $versionInfo\n")
        }.onFailure { SLog.error("Failed to written log file \"$logFile\"\n$it") }
    }
}