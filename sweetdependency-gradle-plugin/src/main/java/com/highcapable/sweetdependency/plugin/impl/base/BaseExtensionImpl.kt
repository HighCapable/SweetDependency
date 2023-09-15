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
 * This file is created by fankes on 2023/5/27.
 */
package com.highcapable.sweetdependency.plugin.impl.base

import com.highcapable.sweetdependency.gradle.delegate.ProjectTransaction
import com.highcapable.sweetdependency.plugin.config.proxy.ISweetDependencyConfigs
import org.gradle.api.initialization.Settings

/**
 * 扩展父类实现类
 */
internal abstract class BaseExtensionImpl internal constructor() {

    /**
     * 当初始化时回调
     * @param settings 当前设置
     * @param configs 当前配置
     */
    internal abstract fun onInitialization(settings: Settings, configs: ISweetDependencyConfigs)

    /**
     * 当开始事务时回调
     * @param transaction 当前实例
     */
    internal abstract fun onTransaction(transaction: ProjectTransaction)
}