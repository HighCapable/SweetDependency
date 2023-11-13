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
 * This file is created by fankes on 2023/5/18.
 */
package com.highcapable.sweetdependency.plugin.config.proxy

import com.highcapable.sweetdependency.SweetDependency
import org.gradle.api.initialization.Settings

/**
 * [SweetDependency] 配置类接口类
 */
internal interface ISweetDependencyConfigs {

    companion object {

        /**
         * 默认的配置文件名称
         *
         * "sweet-dependency-config.yaml"
         */
        internal const val DEFAULT_CONFIG_FILE_NAME = "sweet-dependency-config.yaml"
    }

    /** 是否启用插件 */
    val isEnable: Boolean

    /** [SweetDependency] 的配置文件路径 */
    val configFilePath: String

    /** 是否使用 [Settings.dependencyResolutionManagement] 管理库依赖 */
    val isUseDependencyResolutionManagement: Boolean

    /** 是否启用依赖自动装配日志 */
    val isEnableDependenciesAutowireLog: Boolean

    /** 是否启用详细模式 */
    val isEnableVerboseMode: Boolean
}