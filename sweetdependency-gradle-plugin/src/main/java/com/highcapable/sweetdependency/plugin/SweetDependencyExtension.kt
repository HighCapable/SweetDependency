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
 * This file is created by fankes on 2023/5/19.
 */
package com.highcapable.sweetdependency.plugin

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.gradle.delegate.ProjectTransaction
import com.highcapable.sweetdependency.gradle.factory.getOrCreate
import com.highcapable.sweetdependency.gradle.proxy.IGradleLifecycle
import com.highcapable.sweetdependency.manager.GradleTaskManager
import com.highcapable.sweetdependency.plugin.extension.dsl.configure.SweetDependencyConfigureExtension
import com.highcapable.sweetdependency.plugin.impl.SweetDependencyExtensionImpl
import com.highcapable.sweetdependency.utils.debug.SError
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

/**
 * [SweetDependency] 插件扩展类
 */
internal class SweetDependencyExtension internal constructor() : IGradleLifecycle {

    /** 当前配置方法体实例 */
    private var configure: SweetDependencyConfigureExtension? = null

    /** 当前扩展实现实例 */
    private var impl: SweetDependencyExtensionImpl? = null

    /** 当前项目事务实例 */
    private var transaction: ProjectTransaction? = null

    override fun onSettingsLoaded(settings: Settings) {
        configure = settings.getOrCreate<SweetDependencyConfigureExtension>(SweetDependencyConfigureExtension.NAME)
    }

    override fun onSettingsEvaluate(settings: Settings) {
        impl = SweetDependencyExtensionImpl()
        impl?.onInitialization(settings, configure?.build() ?: SError.make("Settings lifecycle is broken"))
    }

    override fun onProjectLoaded(project: Project, isRoot: Boolean) {
        ProjectTransaction.current = project
        ProjectTransaction.isRoot = isRoot
        if (transaction == null) transaction = ProjectTransaction()
        if (isRoot) GradleTaskManager.register(project)
        transaction?.also { impl?.onTransaction(it) }
    }

    override fun onProjectEvaluate(project: Project, isRoot: Boolean) {
        transaction?.evaluateCallbacks?.forEach { it(project, isRoot) }
    }
}