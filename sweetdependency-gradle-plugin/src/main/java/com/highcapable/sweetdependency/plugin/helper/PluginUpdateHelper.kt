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
 * This file is created by fankes on 2023/7/30.
 */
package com.highcapable.sweetdependency.plugin.helper

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.generated.SweetDependencyProperties
import com.highcapable.sweetdependency.gradle.helper.GradleHelper
import com.highcapable.sweetdependency.utils.debug.SLog
import com.highcapable.sweetdependency.utils.executeUrlBody

/**
 * 插件自身检查更新工具类
 */
internal object PluginUpdateHelper {

    private const val REPO_NAME = SweetDependencyProperties.PROJECT_NAME
    private const val AUTHOR_NAME = SweetDependencyProperties.PROJECT_DEVELOPER_NAME

    /** 检查更新 URL 地址 */
    private const val RELEASE_URL = "https://github.com/$AUTHOR_NAME/$REPO_NAME"

    /** 检查更新 */
    internal fun checkingForUpdate() {
        if (GradleHelper.isOfflineMode) return
        val latestVersion = RELEASE_URL.executeUrlBody(isShowFailure = false).findVersionName()
        if (latestVersion.isNotBlank() && latestVersion != SweetDependency.VERSION)
            SLog.note(
                """
                  Plugin update is available, the current version is ${SweetDependency.VERSION}, please update to $latestVersion
                  You can modify your plugin version in your project's settings.gradle / settings.gradle.kts
                  plugins {
                      id("${SweetDependencyProperties.PROJECT_GROUP_NAME}") version "$latestVersion"
                      ...
                  }
                  For more information, you can visit ${SweetDependency.PROJECT_URL}
                """.trimIndent(), SLog.UP
            )
    }

    /**
     * 解析 JSON 并查找字符串版本 "name"
     * @return [String]
     */
    private fun String.findVersionName() =
        runCatching { trim().split("href=\"/$AUTHOR_NAME/$REPO_NAME/releases/tag/")[1].split("\"")[0] }.getOrNull() ?: ""
}