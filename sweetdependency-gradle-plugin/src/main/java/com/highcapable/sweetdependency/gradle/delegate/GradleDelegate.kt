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
 * This file is created by fankes on 2023/5/26.
 */
package com.highcapable.sweetdependency.gradle.delegate

import com.highcapable.sweetdependency.gradle.helper.GradleHelper
import com.highcapable.sweetdependency.gradle.proxy.IGradleLifecycle
import com.highcapable.sweetdependency.utils.debug.SError
import org.gradle.api.Project
import org.gradle.api.initialization.Settings

/**
 * Gradle 代理工具类
 */
internal object GradleDelegate {

    /** 当前 Gradle 生命周期接口实例 */
    private var lifecycle: IGradleLifecycle? = null

    /**
     * 创建 Gradle 生命周期 (插件) [T]
     * @param settings 当前设置
     */
    internal inline fun <reified T : IGradleLifecycle> create(settings: Settings) {
        runCatching {
            lifecycle = T::class.java.getConstructor().newInstance()
        }.onFailure { SError.make("Failed to create Gradle lifecycle of \"${T::class.java}\"") }
        GradleHelper.attach(settings)
        callOnSettingsLoaded(settings)
        settings.gradle.settingsEvaluated { callOnSettingsEvaluate(settings = this) }
        settings.gradle.projectsLoaded {
            rootProject.beforeEvaluate { callOnProjectLoaded(project = this, isRoot = true) }
            rootProject.afterEvaluate { callOnProjectEvaluate(project = this, isRoot = true) }
            rootProject.subprojects.forEach {
                it.beforeEvaluate { callOnProjectLoaded(project = this, isRoot = false) }
                it.afterEvaluate { callOnProjectEvaluate(project = this, isRoot = false) }
            }
        }
    }

    /**
     * 调用 Gradle 开始装载事件
     * @param settings 当前实例
     */
    private fun callOnSettingsLoaded(settings: Settings) {
        lifecycle?.onSettingsLoaded(settings)
    }

    /**
     * 调用 Gradle 装载完成事件
     * @param settings 当前实例
     */
    private fun callOnSettingsEvaluate(settings: Settings) {
        lifecycle?.onSettingsEvaluate(settings)
    }

    /**
     * 调用 Gradle 开始装载项目事件
     * @param project 当前项目
     * @param isRoot 是否为根项目
     */
    private fun callOnProjectLoaded(project: Project, isRoot: Boolean) {
        if (isRoot) GradleHelper.cachingProjectList(project)
        lifecycle?.onProjectLoaded(project, isRoot)
    }

    /**
     * 调用 Gradle 项目装载完成事件
     * @param project 当前项目
     * @param isRoot 是否为根项目
     */
    private fun callOnProjectEvaluate(project: Project, isRoot: Boolean) {
        GradleHelper.cachingDependencyList(project, isRoot)
        lifecycle?.onProjectEvaluate(project, isRoot)
    }
}