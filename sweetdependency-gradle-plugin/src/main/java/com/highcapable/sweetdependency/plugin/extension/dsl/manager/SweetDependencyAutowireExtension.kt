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
 * This file is created by fankes on 2023/6/26.
 */
package com.highcapable.sweetdependency.plugin.extension.dsl.manager

import com.highcapable.sweetdependency.manager.helper.DependencyDeployHelper
import org.gradle.api.Project

/**
 * 依赖扩展功能配置方法体实现类
 * @param project 当前项目
 */
open class SweetDependencyAutowireExtension internal constructor(private val project: Project) {

    internal companion object {

        /** [SweetDependencyAutowireExtension] 扩展名称 */
        internal const val NAME = "sweet"
    }

    /**
     * 自动装配依赖
     * @param params 参数数组
     * @return [Any]
     */
    fun autowire(vararg params: String) = DependencyDeployHelper.resolveAutowire(project, params)
}