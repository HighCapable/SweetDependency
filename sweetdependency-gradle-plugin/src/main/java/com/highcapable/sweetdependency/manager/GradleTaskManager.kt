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
 * This file is created by fankes on 2023/6/29.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package com.highcapable.sweetdependency.manager

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.generated.SweetDependencyProperties
import com.highcapable.sweetdependency.gradle.factory.createTask
import com.highcapable.sweetdependency.gradle.helper.GradleHelper
import com.highcapable.sweetdependency.plugin.task.AutowireDependenciesTask
import com.highcapable.sweetdependency.plugin.task.AutowireLibrariesTask
import com.highcapable.sweetdependency.plugin.task.AutowirePluginsTask
import com.highcapable.sweetdependency.plugin.task.CreateDependenciesMigrationTemplateTask
import com.highcapable.sweetdependency.plugin.task.SweetDependencyDebugTask
import com.highcapable.sweetdependency.plugin.task.UpdateAllDependenciesTask
import com.highcapable.sweetdependency.plugin.task.UpdateAllLibrariesTask
import com.highcapable.sweetdependency.plugin.task.UpdateAllPluginsTask
import com.highcapable.sweetdependency.plugin.task.UpdateOptionalDependenciesTask
import com.highcapable.sweetdependency.plugin.task.UpdateOptionalLibrariesTask
import com.highcapable.sweetdependency.plugin.task.UpdateOptionalPluginsTask
import org.gradle.api.Project

/**
 * Gradle Task 管理类
 */
internal object GradleTaskManager {

    /** Gradle Task 分组名称 */
    internal const val TASK_GROUP_NAME = SweetDependencyProperties.PROJECT_MODULE_NAME

    /** 创建依赖迁移模板 Gradle Task 名称 */
    internal const val CREATE_DEPENDENCIES_MIGRATION_TEMPLATE_TASK_NAME = "createDependenciesMigrationTemplate"

    /** 依赖自动装配、更新 (可选) (插件依赖 + 库依赖) Gradle Task 名称 */
    internal const val UPDATE_OPTIONAL_DEPENDENCIES_TASK_NAME = "updateOptionalDependencies"

    /** 依赖自动装配、更新 (全部) (插件依赖 + 库依赖) Gradle Task 名称 */
    internal const val UPDATE_ALL_DEPENDENCIES_TASK_NAME = "updateAllDependencies"

    /** 依赖自动装配 (插件依赖 + 库依赖) Gradle Task 名称 */
    internal const val AUTOWIRE_DEPENDENCIES_TASK_NAME = "autowireDependencies"

    /** 依赖自动装配、更新 (可选) (插件依赖) Gradle Task 名称 */
    internal const val UPDATE_OPTIONAL_PLUGINS_TASK_NAME = "updateOptionalPlugins"

    /** 依赖自动装配、更新 (全部) (插件依赖) Gradle Task 名称 */
    internal const val UPDATE_ALL_PLUGINS_TASK_NAME = "updateAllPlugins"

    /** 插件依赖自动装配 (插件依赖) Gradle Task 名称 */
    internal const val AUTOWIRE_PLUGINS_TASK_NAME = "autowirePlugins"

    /** 依赖自动装配、更新 (可选) (库依赖) Gradle Task 名称 */
    internal const val UPDATE_OPTIONAL_LIBRARIES_TASK_NAME = "updateOptionalLibraries"

    /** 依赖自动装配、更新 (全部) (库依赖) Gradle Task 名称 */
    internal const val UPDATE_ALL_LIBRARIES_TASK_NAME = "updateAllLibraries"

    /** 依赖自动装配 (库依赖) Gradle Task 名称 */
    internal const val AUTOWIRE_LIBRARIES_TASK_NAME = "autowireLibraries"

    /** 调试 Gradle Task 名称 */
    internal const val SWEET_DEPENDENCY_DEBUG_TASK_NAME = "sweetDependencyDebug"

    /**
     * 当前正在运行的是否为 [SweetDependency] 内部 Gradle Task
     * @return [Boolean]
     */
    internal val isInternalRunningTask
        get() = GradleHelper.runningTaskNames.orEmpty().any {
            it == CREATE_DEPENDENCIES_MIGRATION_TEMPLATE_TASK_NAME ||
                it == UPDATE_OPTIONAL_DEPENDENCIES_TASK_NAME ||
                it == UPDATE_ALL_DEPENDENCIES_TASK_NAME ||
                it == AUTOWIRE_DEPENDENCIES_TASK_NAME ||
                it == UPDATE_OPTIONAL_PLUGINS_TASK_NAME ||
                it == UPDATE_ALL_PLUGINS_TASK_NAME ||
                it == AUTOWIRE_PLUGINS_TASK_NAME ||
                it == UPDATE_OPTIONAL_LIBRARIES_TASK_NAME ||
                it == UPDATE_ALL_LIBRARIES_TASK_NAME ||
                it == AUTOWIRE_LIBRARIES_TASK_NAME ||
                it == SWEET_DEPENDENCY_DEBUG_TASK_NAME
        }

    /**
     * 注册 [SweetDependency] 全部 Gradle Task
     * @param rootProject 根项目
     */
    internal fun register(rootProject: Project) {
        rootProject.createTask<CreateDependenciesMigrationTemplateTask>(TASK_GROUP_NAME, CREATE_DEPENDENCIES_MIGRATION_TEMPLATE_TASK_NAME)
        rootProject.createTask<UpdateOptionalDependenciesTask>(TASK_GROUP_NAME, UPDATE_OPTIONAL_DEPENDENCIES_TASK_NAME)
        rootProject.createTask<UpdateAllDependenciesTask>(TASK_GROUP_NAME, UPDATE_ALL_DEPENDENCIES_TASK_NAME)
        rootProject.createTask<AutowireDependenciesTask>(TASK_GROUP_NAME, AUTOWIRE_DEPENDENCIES_TASK_NAME)
        rootProject.createTask<UpdateOptionalPluginsTask>(TASK_GROUP_NAME, UPDATE_OPTIONAL_PLUGINS_TASK_NAME)
        rootProject.createTask<UpdateAllPluginsTask>(TASK_GROUP_NAME, UPDATE_ALL_PLUGINS_TASK_NAME)
        rootProject.createTask<AutowirePluginsTask>(TASK_GROUP_NAME, AUTOWIRE_PLUGINS_TASK_NAME)
        rootProject.createTask<UpdateOptionalLibrariesTask>(TASK_GROUP_NAME, UPDATE_OPTIONAL_LIBRARIES_TASK_NAME)
        rootProject.createTask<UpdateAllLibrariesTask>(TASK_GROUP_NAME, UPDATE_ALL_LIBRARIES_TASK_NAME)
        rootProject.createTask<AutowireLibrariesTask>(TASK_GROUP_NAME, AUTOWIRE_LIBRARIES_TASK_NAME)
        rootProject.createTask<SweetDependencyDebugTask>(TASK_GROUP_NAME, SWEET_DEPENDENCY_DEBUG_TASK_NAME)
    }
}