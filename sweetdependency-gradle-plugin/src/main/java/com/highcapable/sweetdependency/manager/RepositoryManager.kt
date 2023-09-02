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
 * This file is Created by fankes on 2023/6/6.
 */
package com.highcapable.sweetdependency.manager

import com.highcapable.sweetdependency.document.PreferencesDocument
import com.highcapable.sweetdependency.document.RepositoryDocument
import com.highcapable.sweetdependency.exception.SweetDependencyUnresolvedException
import com.highcapable.sweetdependency.manager.const.AdditionalRepositories
import com.highcapable.sweetdependency.manager.content.Repositories
import com.highcapable.sweetdependency.plugin.config.content.SweetDependencyConfigs
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.debug.SLog
import com.highcapable.sweetdependency.utils.noBlank
import com.highcapable.sweetdependency.utils.noEmpty
import com.highcapable.sweetdependency.utils.toFile
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.AuthenticationSupported
import org.gradle.api.artifacts.repositories.UrlArtifactRepository
import org.gradle.api.initialization.Settings
import java.net.URI
import org.gradle.api.initialization.resolve.RepositoriesMode as GradleRepositoriesMode

/**
 * 存储库装配管理类
 */
internal object RepositoryManager {

    /**
     * 生成并应用存储库数组
     * @param settings 当前设置
     */
    internal fun generateAndApply(settings: Settings) {
        val repositories = SweetDependencyConfigs.document.repositories()
        Repositories.generate(repositories)
        /**
         * 应用存储库数组到 Gradle
         * @param isPlugins 当前应用类型是否为插件依赖
         */
        fun RepositoryHandler.apply(isPlugins: Boolean) = repositories.forEach {
            if (it.isIncludeScope(isPlugins)) when (it.nodeType) {
                RepositoryDocument.RepositoryType.GOOGLE -> google { applyToArtifact(it) }
                RepositoryDocument.RepositoryType.MAVEN_CENTRAL -> mavenCentral { applyToArtifact(it) }
                RepositoryDocument.RepositoryType.MAVEN_LOCAL -> mavenLocal { applyToArtifact(it) }
                RepositoryDocument.RepositoryType.MAVEN -> maven { applyToArtifact(it) }
                RepositoryDocument.RepositoryType.GRADLE_PLUGIN_PORTAL -> gradlePluginPortal { applyToArtifact(it) }
                else -> {}
            }
        }
        settings.pluginManagement {
            this.repositories.clear()
            this.repositories.apply(isPlugins = true)
        }
        settings.dependencyResolutionManagement {
            this.repositoriesMode.set(when (SweetDependencyConfigs.document.preferences().repositoriesMode) {
                PreferencesDocument.RepositoriesMode.PREFER_PROJECT -> GradleRepositoriesMode.PREFER_PROJECT
                PreferencesDocument.RepositoriesMode.PREFER_SETTINGS -> GradleRepositoriesMode.PREFER_SETTINGS
                PreferencesDocument.RepositoriesMode.FAIL_ON_PROJECT_REPOS -> GradleRepositoriesMode.FAIL_ON_PROJECT_REPOS
            })
            this.repositories.clear()
            this.repositories.apply(isPlugins = false)
        }
    }

    /**
     * 应用存储库到 [ArtifactRepository]
     * @param document 存储库配置项文档实体
     */
    private fun ArtifactRepository.applyToArtifact(document: RepositoryDocument) {
        document.nodeName.noBlank()?.also { docName -> this.name = docName }
        if (this is AuthenticationSupported && document.credentials.let { it.username.isNotBlank() || it.password.isNotBlank() })
            credentials { this.username = document.credentials.username; this.password = document.credentials.password }
        if (document.content.isEmpty().not()) content {
            /**
             * 使用 ":" 分割字符串
             * @param size 期望的个数
             * @param result 回调每项
             * @return [List]<[String]>
             * @throws SweetDependencyUnresolvedException 如果 [size] 不是期望的个数
             */
            fun List<String>.forEachParams(size: Int, result: (List<String>) -> Unit) = forEach {
                result(it.split(":").also { e -> if (e.size != size) SError.make("Missing argument in content configuration") })
            }
            document.content.exclude.also {
                it.group().noEmpty()?.forEach { e -> excludeGroup(e) }
                it.groupAndSubgroups().noEmpty()?.forEach { e -> excludeGroupAndSubgroups(e) }
                it.groupByRegex().noEmpty()?.forEach { e -> excludeGroupByRegex(e) }
                it.module().noEmpty()?.forEachParams(size = 2) { e -> excludeModule(e[0], e[1]) }
                it.moduleByRegex().noEmpty()?.forEachParams(size = 2) { e -> excludeModuleByRegex(e[0], e[1]) }
                it.version().noEmpty()?.forEachParams(size = 3) { e -> excludeVersion(e[0], e[1], e[2]) }
                it.versionByRegex().noEmpty()?.forEachParams(size = 3) { e -> excludeVersionByRegex(e[0], e[1], e[2]) }
            }
            document.content.include.also {
                it.group().noEmpty()?.forEach { e -> includeGroup(e) }
                it.groupAndSubgroups().noEmpty()?.forEach { e -> includeGroupAndSubgroups(e) }
                it.groupByRegex().noEmpty()?.forEach { e -> includeGroupByRegex(e) }
                it.module().noEmpty()?.forEachParams(size = 2) { e -> includeModule(e[0], e[1]) }
                it.moduleByRegex().noEmpty()?.forEachParams(size = 2) { e -> includeModuleByRegex(e[0], e[1]) }
                it.version().noEmpty()?.forEachParams(size = 3) { e -> includeVersion(e[0], e[1], e[2]) }
                it.versionByRegex().noEmpty()?.forEachParams(size = 3) { e -> includeVersionByRegex(e[0], e[1], e[2]) }
            }
        }
        if (document.nodeType != RepositoryDocument.RepositoryType.MAVEN_LOCAL)
            (document.url.noBlank()?.let {
                /** JCenter 已经终止服务 - 不确定其镜像源是否也会同时关闭 */
                if (it == AdditionalRepositories.ALIYUN_JCENTER_MIRROR)
                    SLog.warn("JCenter has shut down, and its mirror server may stop soon, please transfer to other repositories")
                URI.create(it)
            } ?: document.path.noBlank()?.toFile()?.toURI())
                ?.also { uri -> if (this is UrlArtifactRepository) this.url = uri }
    }
}