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
 * This file is created by fankes on 2023/5/16.
 */
@file:Suppress("unused")

package com.highcapable.sweetdependency.plugin

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.gradle.delegate.GradleDelegate
import com.highcapable.sweetdependency.utils.debug.SError
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware

/**
 * [SweetDependency] 插件定义类
 */
class SweetDependencyPlugin<T : ExtensionAware> internal constructor() : Plugin<T> {

    override fun apply(target: T) = when (target) {
        is Settings -> GradleDelegate.create<SweetDependencyExtension>(target)
        else -> SError.make("${SweetDependency.TAG} can only applied in settings.gradle or settings.gradle.kts, but current is $target")
    }
}