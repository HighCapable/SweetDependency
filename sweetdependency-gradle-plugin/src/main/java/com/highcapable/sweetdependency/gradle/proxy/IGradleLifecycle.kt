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
 * This file is created by fankes on 2023/5/26.
 */
package com.highcapable.sweetdependency.gradle.proxy

import org.gradle.api.Project
import org.gradle.api.initialization.Settings

/**
 * Gradle 生命周期接口
 */
internal interface IGradleLifecycle {

    /**
     * 当 Gradle 开始装载时回调
     * @param settings 当前设置
     */
    fun onSettingsLoaded(settings: Settings)

    /**
     * 当 Gradle 装载完成时回调
     * @param settings 当前设置
     */
    fun onSettingsEvaluate(settings: Settings)

    /**
     * 当 Gradle 开始装载项目时回调
     * @param project 当前项目
     * @param isRoot 是否为根项目
     */
    fun onProjectLoaded(project: Project, isRoot: Boolean)

    /**
     * 当 Gradle 项目装载完成时回调
     * @param project 当前项目
     * @param isRoot 是否为根项目
     */
    fun onProjectEvaluate(project: Project, isRoot: Boolean)
}