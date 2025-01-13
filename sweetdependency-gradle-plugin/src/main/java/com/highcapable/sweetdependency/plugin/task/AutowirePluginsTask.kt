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
 * This file is created by fankes on 2023/8/16.
 */
package com.highcapable.sweetdependency.plugin.task

import com.highcapable.sweetdependency.gradle.entity.DependencyUpdateMode
import com.highcapable.sweetdependency.manager.DependencyManager
import com.highcapable.sweetdependency.plugin.config.content.SweetDependencyConfigs
import com.highcapable.sweetdependency.plugin.task.base.BaseTask

/**
 * 依赖自动装配 Gradle Task
 */
internal class AutowirePluginsTask : BaseTask() {

    override fun onTransaction() = SweetDependencyConfigs.withPluginEnable {
        DependencyManager.autowireAndUpdate(
            DependencyUpdateMode(
                DependencyUpdateMode.DependencyType.PLUGINS,
                DependencyUpdateMode.UpdateType.ONLY_AUTOWIRE
            ), isRunningOnSync = false
        )
    }
}