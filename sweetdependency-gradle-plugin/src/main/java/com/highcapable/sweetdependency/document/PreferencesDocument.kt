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

import com.highcapable.sweetdependency.document.factory.checkingName
import com.highcapable.sweetdependency.utils.camelcase
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.yaml.proxy.IYamlDocument
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.gradle.api.initialization.resolve.RepositoriesMode as GradleRepositoriesMode

/**
 * 偏好配置项文档实体
 * @param autowireOnSyncMode Gradle Sync 自动装配、更新依赖模式
 * @param repositoriesMode 存储库装载模式
 * @param dependenciesNamespace 依赖命名空间
 * @param versionFilter 版本过滤器文档实体
 */
@Serializable
internal data class PreferencesDocument(
    @SerialName("autowire-on-sync-mode")
    internal var autowireOnSyncMode: AutowireOnSyncMode = AutowireOnSyncMode.UPDATE_OPTIONAL_DEPENDENCIES,
    @SerialName("repositories-mode")
    internal var repositoriesMode: RepositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS,
    @SerialName("dependencies-namespace")
    internal var dependenciesNamespace: DependenciesNamespaceDocument = DependenciesNamespaceDocument(),
    @SerialName("version-filter")
    internal var versionFilter: VersionFilterDocument = VersionFilterDocument()
) : IYamlDocument {

    /**
     * 依赖命名空间文档实体
     * @param plugins 插件依赖
     * @param libraries 库依赖
     */
    @Serializable
    internal data class DependenciesNamespaceDocument(
        @SerialName("plugins")
        var plugins: NamespaceOptionDocument = NamespaceOptionDocument(name = "libs"),
        @SerialName("libraries")
        var libraries: NamespaceOptionDocument = NamespaceOptionDocument()
    ) : IYamlDocument {

        init {
            if (plugins.name.isNotBlank() && libraries.name.isNotBlank() && plugins.name == libraries.name)
                SError.make("Duplicated dependencies namespace \"$plugins\"")
        }
    }

    /**
     * 命名空间选项文档实体
     * @param isEnable 是否启用
     * @param name 名称
     */
    @Serializable
    internal data class NamespaceOptionDocument(
        @SerialName("enable")
        var isEnable: Boolean = true,
        @SerialName("name")
        var name: String = ""
    ) : IYamlDocument {

        /**
         * 获取名称
         * @return [String]
         */
        internal fun name() = name.apply { checkingName("dependencies namespace", isCheckExtName = true) }.camelcase()
    }

    /**
     * Gradle Sync 自动装配、更新依赖模式定义类
     */
    internal enum class AutowireOnSyncMode {
        /** 自动装配和更新可选依赖 (插件依赖 + 库依赖) */
        UPDATE_OPTIONAL_DEPENDENCIES,

        /** 自动装配和更新所有依赖 (插件依赖 + 库依赖) */
        UPDATE_ALL_DEPENDENCIES,

        /** 仅自动装配使用“+”填充版本的依赖 (插件依赖 + 库依赖) */
        ONLY_AUTOWIRE_DEPENDENCIES,

        /** 自动装配和更新可选依赖 (插件依赖) */
        UPDATE_OPTIONAL_PLUGINS,

        /** 自动装配和更新所有依赖 (插件依赖) */
        UPDATE_ALL_PLUGINS,

        /** 仅自动装配使用“+”填充版本的依赖 (插件依赖) */
        ONLY_AUTOWIRE_PLUGINS,

        /** 自动装配和更新可选依赖 (库依赖) */
        UPDATE_OPTIONAL_LIBRARIES,

        /** 自动装配和更新所有依赖 (库依赖) */
        UPDATE_ALL_LIBRARIES,

        /** 仅自动装配使用“+”填充版本的依赖 (库依赖) */
        ONLY_AUTOWIRE_LIBRARIES,

        /** 什么也不做 - 关闭所有功能 */
        OFF
    }

    /**
     * 存储库装载模式定义类 (跟随 Gradle 进行配置调整)
     */
    internal enum class RepositoriesMode {
        /** 参考 [GradleRepositoriesMode.PREFER_PROJECT] */
        PREFER_PROJECT,

        /** 参考 [GradleRepositoriesMode.PREFER_SETTINGS] */
        PREFER_SETTINGS,

        /** 参考 [GradleRepositoriesMode.FAIL_ON_PROJECT_REPOS] */
        FAIL_ON_PROJECT_REPOS
    }
}