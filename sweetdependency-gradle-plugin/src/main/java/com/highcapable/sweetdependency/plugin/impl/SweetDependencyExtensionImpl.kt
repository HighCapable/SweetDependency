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
 * This file is Created by fankes on 2023/5/27.
 */
package com.highcapable.sweetdependency.plugin.impl

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.document.PreferencesDocument
import com.highcapable.sweetdependency.document.factory.toUpdateMode
import com.highcapable.sweetdependency.environment.Environment
import com.highcapable.sweetdependency.gradle.delegate.ProjectTransaction
import com.highcapable.sweetdependency.gradle.helper.GradleHelper
import com.highcapable.sweetdependency.manager.DependencyManager
import com.highcapable.sweetdependency.manager.RepositoryManager
import com.highcapable.sweetdependency.plugin.config.content.SweetDependencyConfigs
import com.highcapable.sweetdependency.plugin.config.proxy.ISweetDependencyConfigs
import com.highcapable.sweetdependency.plugin.helper.PluginUpdateHelper
import com.highcapable.sweetdependency.plugin.impl.base.BaseExtensionImpl
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.debug.SLog
import org.gradle.api.initialization.Settings

/**
 * [SweetDependency] 扩展实现类
 */
internal class SweetDependencyExtensionImpl : BaseExtensionImpl() {

    private companion object {

        /** 插件是否已经装载 - 在 Gradle 守护程序启动后进程不会被结束 */
        private var isPluginLoaded = false
    }

    /**
     * 检查兼容性
     *
     * 目前仅支持 Gradle 7.x.x and 8.x.x 版本
     */
    private fun checkingCompatibility() {
        Environment.characterEncoding?.also {
            if (it.lowercase() != "utf-8") SLog.warn(
                """
                  !!! WARNING !!!
                  The current character encoding is not UTF-8, it is currently $it
                  This will cause some characters cannot be displayed, please change the console character encoding
                """.trimIndent(), noTag = true
            )
        }
        if (GradleHelper.version.let { it.startsWith("7.") || it.startsWith("8.") }.not()) SError.make(
            "${SweetDependency.TAG} ${SweetDependency.VERSION} " +
                "does not support Gradle ${GradleHelper.version}, please update Gradle or plugin version"
        )
    }

    override fun onInitialization(settings: Settings, configs: ISweetDependencyConfigs) {
        checkingCompatibility()
        configureProject(configs)
        SweetDependencyConfigs.initialize(configs)
        SweetDependencyConfigs.withPluginEnable {
            configureRepositoriesAndDependencies(settings) { autowireOnSyncMode ->
                autowireOnSyncMode.toUpdateMode()?.also { DependencyManager.autowireAndUpdate(it) }
            }; PluginUpdateHelper.checkingForUpdate()
        }
    }

    override fun onTransaction(transaction: ProjectTransaction) {
        if (transaction.isRoot) DependencyManager.resolve(transaction.current)
        transaction.evaluation { project, isRoot -> if (isRoot) DependencyManager.deploy(project) }
    }

    /**
     * 配置项目
     * @param configs 当前配置
     */
    private fun configureProject(configs: ISweetDependencyConfigs) {
        SLog.isVerboseMode = configs.isEnableVerboseMode
        if (isPluginLoaded.not() || GradleHelper.isSyncMode.not()) SLog.verbose(SweetDependency.bannerContent, noTag = true)
        if (isPluginLoaded) return
        isPluginLoaded = true
        SLog.verbose("Welcome to ${SweetDependency.TAG} ${SweetDependency.VERSION}! Using Gradle ${GradleHelper.version}")
    }

    /**
     * 配置存储库和依赖
     * @param settings 当前设置
     * @param autowire 自回调动装配方法体
     */
    private inline fun configureRepositoriesAndDependencies(settings: Settings, autowire: (PreferencesDocument.AutowireOnSyncMode) -> Unit) {
        RepositoryManager.generateAndApply(settings)
        DependencyManager.generateAndApply()
        if (GradleHelper.isSyncMode) autowire(SweetDependencyConfigs.document.preferences().autowireOnSyncMode)
        DependencyManager.generateAndApply(settings)
    }
}