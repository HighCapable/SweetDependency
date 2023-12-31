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
 * This file is created by fankes on 2023/6/26.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package com.highcapable.sweetdependency.plugin.extension.dsl.configure

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.environment.Environment
import com.highcapable.sweetdependency.plugin.config.proxy.ISweetDependencyConfigs
import org.gradle.api.initialization.Settings

/**
 * [SweetDependency] 配置方法体实现类
 */
open class SweetDependencyConfigureExtension internal constructor() {

    internal companion object {

        /** [SweetDependencyConfigureExtension] 扩展名称 */
        internal const val NAME = "sweetDependency"
    }

    /**
     * 是否启用插件
     *
     * 默认启用 - 如果你想关闭插件 - 在这里设置就可以了
     */
    var isEnable = true
        @JvmName("enable") set

    /**
     * 是否启用调试模式
     *
     * 默认不启用 - 启用后将在自动装配时输出详细的依赖搜索信息
     */
    var isDebug = false
        @JvmName("debug") set

    /**
     * [SweetDependency] 配置文件名称
     *
     * 默认为 [ISweetDependencyConfigs.DEFAULT_CONFIG_FILE_NAME]
     */
    var configFileName = ISweetDependencyConfigs.DEFAULT_CONFIG_FILE_NAME
        @JvmName("configFileName") set

    /**
     * 是否使用 [Settings.dependencyResolutionManagement] 管理库依赖
     *
     * 此功能默认启用 - 如果你的项目必须存在自定义的 "repositories" 方法块 - 请关闭此功能
     *
     * - 注意：关闭后配置文件中的 "repositories-mode" 选项将不再有效
     */
    var isUseDependencyResolutionManagement = true
        @JvmName("useDependencyResolutionManagement") set

    /**
     * 是否启用依赖自动装配日志
     *
     * 此功能默认启用 - 会在当前根项目 (Root Project) 的 build 目录下创建日志文件
     */
    var isEnableDependenciesAutowireLog = true
        @JvmName("enableDependenciesAutowireLog") set

    /**
     * 是否启用详细模式
     *
     * 此功能默认启用 - 关闭后 [SweetDependency] 将会在非必要情况下保持安静 (省略非必要日志)
     */
    var isEnableVerboseMode = true
        @JvmName("enableVerboseMode") set

    /**
     * 构造 [ISweetDependencyConfigs]
     * @return [ISweetDependencyConfigs]
     */
    internal fun build(): ISweetDependencyConfigs {
        val currentEnable = isEnable
        val currentDebug = isDebug
        val currentConfigFilePath = Environment.resourcesDir(configFileName).absolutePath
        val currentUseDependencyResolutionManagement = isUseDependencyResolutionManagement
        val currentEnableDependenciesAutowireLog = isEnableDependenciesAutowireLog
        val currentEnableVerboseMode = isEnableVerboseMode
        return object : ISweetDependencyConfigs {
            override val isEnable get() = currentEnable
            override val isDebug get() = currentDebug
            override val configFilePath get() = currentConfigFilePath
            override val isUseDependencyResolutionManagement get() = currentUseDependencyResolutionManagement
            override val isEnableDependenciesAutowireLog get() = currentEnableDependenciesAutowireLog
            override val isEnableVerboseMode get() = currentEnableVerboseMode
        }
    }
}