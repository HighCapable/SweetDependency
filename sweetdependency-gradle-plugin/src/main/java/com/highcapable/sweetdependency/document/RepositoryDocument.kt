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
 * This file is created by fankes on 2023/5/17.
 */
package com.highcapable.sweetdependency.document

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.manager.const.InternalRepositories
import com.highcapable.sweetdependency.manager.content.Repositories
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.parseUnixFileSeparator
import com.highcapable.sweetdependency.utils.toSpaceList
import com.highcapable.sweetdependency.utils.yaml.proxy.IYamlDocument
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.artifacts.repositories.RepositoryContentDescriptor

/**
 * 存储库配置项文档实体
 * @param isEnable 是否启用
 * @param scope 作用域
 * @param content 内容过滤器
 * @param credentials 身份验证配置项文档实体
 * @param url URL 地址
 * @param path 文件路径
 */
@Serializable
internal data class RepositoryDocument(
    @SerialName("enable")
    internal var isEnable: Boolean = true,
    @SerialName("scope")
    internal var scope: RepositoryScope = RepositoryScope.ALL,
    @SerialName("content")
    internal var content: ContentDocument = ContentDocument(),
    @SerialName("credentials")
    internal var credentials: CredentialsDocument = CredentialsDocument(),
    @SerialName("url")
    internal var url: String = "",
    @SerialName("path")
    internal var path: String = "",
) : IYamlDocument {

    /**
     * 身份验证配置项文档实体
     *
     * 这些内容来自 [PasswordCredentials]
     * @param username 用户名
     * @param password 密码
     */
    @Serializable
    internal data class CredentialsDocument(
        @SerialName("username")
        internal var username: String = "",
        @SerialName("password")
        internal var password: String = ""
    ) : IYamlDocument

    /**
     * 内容配置文档实体
     *
     * 这些内容来自 [RepositoryContentDescriptor]
     * @param exclude 排除配置文档实体
     * @param include 包含配置文档实体
     */
    @Serializable
    internal data class ContentDocument(
        @SerialName("exclude")
        internal var exclude: FilterDocument = FilterDocument(),
        @SerialName("include")
        internal var include: FilterDocument = FilterDocument()
    ) : IYamlDocument {

        /**
         * 内容过滤器配置文档实体
         *
         * 这些内容来自 [RepositoryContentDescriptor]
         * @param group 过滤器条件内容
         * @param groupAndSubgroups 过滤器条件内容
         * @param groupByRegex 过滤器条件内容
         * @param module 过滤器条件内容
         * @param moduleByRegex 过滤器条件内容
         * @param version 过滤器条件内容
         * @param versionByRegex 过滤器条件内容
         */
        @Serializable
        internal data class FilterDocument(
            @SerialName("group")
            internal var group: String = "",
            @SerialName("group-and-subgroups")
            internal var groupAndSubgroups: String = "",
            @SerialName("group-by-regex")
            internal var groupByRegex: String = "",
            @SerialName("module")
            internal var module: String = "",
            @SerialName("module-by-regex")
            internal var moduleByRegex: String = "",
            @SerialName("version")
            internal var version: String = "",
            @SerialName("version-by-regex")
            internal var versionByRegex: String = ""
        ) : IYamlDocument {

            /**
             * 获取过滤器条件内容
             * @return [List]<[String]>
             */
            internal fun group() = group.toSpaceList()

            /**
             * 获取过滤器条件内容
             * @return [List]<[String]>
             */
            internal fun groupAndSubgroups() = groupAndSubgroups.toSpaceList()

            /**
             * 获取过滤器条件内容
             * @return [List]<[String]>
             */
            internal fun groupByRegex() = groupByRegex.toSpaceList()

            /**
             * 获取过滤器条件内容
             * @return [List]<[String]>
             */
            internal fun module() = module.toSpaceList()

            /**
             * 获取过滤器条件内容
             * @return [List]<[String]>
             */
            internal fun moduleByRegex() = moduleByRegex.toSpaceList()

            /**
             * 获取过滤器条件内容
             * @return [List]<[String]>
             */
            internal fun version() = version.toSpaceList()

            /**
             * 获取过滤器条件内容
             * @return [List]<[String]>
             */
            internal fun versionByRegex() = versionByRegex.toSpaceList()

            /**
             * 当前规则是否为空
             * @return [Boolean]
             */
            internal fun isEmpty() =
                group.isBlank() && groupAndSubgroups.isBlank() && groupByRegex.isBlank() &&
                    module.isBlank() && moduleByRegex.isBlank() &&
                    version.isBlank() && versionByRegex.isBlank()
        }

        /**
         * 当前规则是否为空
         * @return [Boolean]
         */
        internal fun isEmpty() = exclude.isEmpty() && include.isEmpty()
    }

    /** 节点名称 */
    @Transient
    internal var nodeName = ""

    /** 节点类型 */
    @Transient
    internal var nodeType = RepositoryType.UNSPECIFIED

    /**
     * 存储库作用域定义类
     */
    internal enum class RepositoryScope {
        /** 作用于所有类型依赖 */
        ALL,

        /** 作用于插件依赖 */
        PLUGINS,

        /** 作用于库依赖 */
        LIBRARIES
    }

    /**
     * 存储库已知类型定义类
     */
    internal enum class RepositoryType {
        /** 未指定 */
        UNSPECIFIED,

        /** Google Maven */
        GOOGLE,

        /** 中央存储库 */
        MAVEN_CENTRAL,

        /** 本地存储库 */
        MAVEN_LOCAL,

        /** 自定义存储库 */
        MAVEN,

        /** Gradle Plugin 存储库 */
        GRADLE_PLUGIN_PORTAL
    }

    /**
     * 获取是否包含在作用域内
     * @param isPlugins 当前类型是否为插件依赖
     * @return [Boolean]
     */
    internal fun isIncludeScope(isPlugins: Boolean) =
        if (isPlugins) scope == RepositoryScope.ALL || scope == RepositoryScope.PLUGINS
        else scope == RepositoryScope.ALL || scope == RepositoryScope.LIBRARIES

    /**
     * 创建当前实体
     * @param name 键值名称
     * @return [RepositoryDocument]
     */
    internal fun build(name: String) = apply {
        when (name) {
            InternalRepositories.Name.GOOGLE -> {
                url = url.ifBlank { InternalRepositories.GOOGLE }
                nodeType = RepositoryType.GOOGLE
            }
            InternalRepositories.Name.MAVEN_CENTRAL -> {
                url = url.ifBlank { InternalRepositories.MAVEN_CENTRAL }
                nodeType = RepositoryType.MAVEN_CENTRAL
            }
            InternalRepositories.Name.GRADLE_PLUGIN_PORTAL -> {
                url = url.ifBlank { InternalRepositories.GRADLE_PLUGIN_PORTAL }
                nodeType = RepositoryType.GRADLE_PLUGIN_PORTAL
            }
            InternalRepositories.Name.MAVEN_LOCAL -> {
                path = path.ifBlank { Repositories.defaultMavenLocalPath }
                nodeType = RepositoryType.MAVEN_LOCAL
            }
            InternalRepositories.Name.MAVEN -> SError.make("Use \"maven\" as a repository name is an error, please choose another name")
            InternalRepositories.Name.IVY -> SError.make("Ivy is not support on ${SweetDependency.TAG} ${SweetDependency.VERSION}")
            else -> {
                url = url.ifBlank {
                    Repositories.findAdditional(name).ifBlank {
                        SError.make("Could not found internal or additional repository URL by repository name \"$name\", you must specify a URL")
                    }
                }; nodeType = RepositoryType.MAVEN
            }
        }; nodeName = name
        if (url.isNotBlank() && path.isNotBlank()) SError.make("There can only be one \"url\" and \"path\" parameter of \"$name\"")
        if (path.isNotBlank() && (path.startsWith("https://") || path.startsWith("http://"))) SError.make("Invalid repository path: $path")
        if (url.isNotBlank() && !url.startsWith("https://") && !url.startsWith("http://")) SError.make("Invalid repository URL: $url")
        if (path.isNotBlank()) path = path.parseUnixFileSeparator()
    }
}