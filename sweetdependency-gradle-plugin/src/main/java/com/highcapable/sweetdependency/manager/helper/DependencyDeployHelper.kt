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
 * This file is created by fankes on 2023/6/13.
 */
package com.highcapable.sweetdependency.manager.helper

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.environment.Environment
import com.highcapable.sweetdependency.generated.SweetDependencyProperties
import com.highcapable.sweetdependency.gradle.delegate.ProjectTransaction
import com.highcapable.sweetdependency.gradle.delegate.entity.ExternalDependencyDelegate
import com.highcapable.sweetdependency.gradle.entity.DependencyVersion
import com.highcapable.sweetdependency.gradle.factory.addDependencyToBuildScript
import com.highcapable.sweetdependency.gradle.factory.applyPlugin
import com.highcapable.sweetdependency.gradle.factory.getOrCreate
import com.highcapable.sweetdependency.gradle.factory.loadBuildScriptClass
import com.highcapable.sweetdependency.manager.GradleTaskManager
import com.highcapable.sweetdependency.manager.content.Dependencies
import com.highcapable.sweetdependency.plugin.config.content.SweetDependencyConfigs
import com.highcapable.sweetdependency.plugin.generator.LibrariesAccessorsGenerator
import com.highcapable.sweetdependency.utils.camelcase
import com.highcapable.sweetdependency.utils.code.entity.MavenPomData
import com.highcapable.sweetdependency.utils.code.factory.compile
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.debug.SLog
import com.highcapable.sweetdependency.utils.isEmpty
import com.highcapable.sweetdependency.utils.isValidZip
import com.highcapable.sweetdependency.utils.single
import com.highcapable.sweetdependency.utils.toAbsoluteFilePaths
import com.highcapable.sweetdependency.utils.toFile
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

/**
 * 依赖部署工具类
 */
internal object DependencyDeployHelper {

    /** 库依赖可访问 [Class] 标识名称 */
    private const val ACCESSORS_NAME = "dependencies-accessors"

    /** 库依赖可访问 [Class] 生成目录 */
    private val accessorsDir = Environment.memoryDir(ACCESSORS_NAME)

    /** 库依赖可访问 [Class] 虚拟依赖数据 */
    private val accessorsPomData = MavenPomData(SweetDependencyProperties.PROJECT_GROUP_NAME, ACCESSORS_NAME, SweetDependency.VERSION)

    /** 库依赖可访问 [Class] 生成实例 */
    private val accessorsGenerator = LibrariesAccessorsGenerator()

    /**
     * 生成 Version Catalogs
     *
     * 由于 Gradle API 限制 - 无法针对插件依赖进行自定义 - 所以间接使用 Version Catalogs 生成
     * @param settings 当前设置
     */
    internal fun generateVersionCatalogs(settings: Settings) {
        val pluginsNamespace = SweetDependencyConfigs.document.preferences().dependenciesNamespace.plugins()
        runCatching {
            settings.dependencyResolutionManagement.versionCatalogs.create(pluginsNamespace) {
                Dependencies.plugins().forEach { (dependencyName, artifact) ->
                    if (GradleTaskManager.isInternalRunningTask && artifact.version().isAutowire) SError.make(
                        """
                          It looks like now you directly execute ${SweetDependency.TAG}'s autowiring related Gradle task
                          Since this plugin "$dependencyName" that set "${DependencyVersion.AUTOWIRE_VERSION_NAME}" as the version,
                          you now need to ensure that the version exists during the initialization phase
                          The version catalogs rules require that a plugin version must be declared
                          You can try the following solutions to resolve this problem:
                          1. Manually re-run Gradle Sync
                             Make sure "autowire-on-sync-mode" is one of:
                             - UPDATE_OPTIONAL_DEPENDENCIES
                             - UPDATE_OPTIONAL_PLUGINS
                             - UPDATE_ALL_DEPENDENCIES
                             - UPDATE_ALL_PLUGINS
                             - ONLY_AUTOWIRE_DEPENDENCIES
                             - ONLY_AUTOWIRE_PLUGINS
                          2. Fill an existing version for plugin "$dependencyName" and re-run Gradle Sync
                          If you get this error again after doing the above, maybe the currently set repositories cannot find this plugin
                        """.trimIndent()
                    )
                    if (artifact.version().isNoSpecific) return@forEach SLog.warn(
                        """
                          You must specific a version for plugin "$dependencyName" or use Gradle's internal plugin function instead it
                          This problem came from the version catalogs rules, so will not generate "$dependencyName"
                          You can also use "autowire("$dependencyName")" to solve this problem
                          You will see this warning every time, because we don't recommend declaring plugins without version
                        """.trimIndent()
                    )
                    if (artifact.version().isAutowire) SError.make(
                        """
                          This plugin "$dependencyName" is not autowired and cannot be generate
                          You can try the following solutions to resolve this problem:
                          1. Manually re-run Gradle Sync (make sure "autowire-on-sync-mode" not be "OFF")
                          2. Manually run "${GradleTaskManager.AUTOWIRE_PLUGINS_TASK_NAME}" task and re-run Gradle Sync
                          3. Fill an existing version for plugin "$dependencyName" and re-run Gradle Sync
                          If you get this error again after doing the above, maybe the currently set repositories cannot find this plugin
                        """.trimIndent()
                    )
                    val deployedName = dependencyName.ambiguousName(symbol = "-", isReplaceFirstChar = true, isLowerCase = false)
                    plugin(deployedName, dependencyName.current).version(artifact.version().fixed)
                    artifact.versions().forEach { (name, version) ->
                        plugin("$deployedName-${name.camelcase()}", dependencyName.current).version(version.fixed)
                        if (artifact.alias.isNotBlank())
                            plugin("${artifact.alias}-${name.camelcase()}", dependencyName.current).version(version.fixed)
                    }
                    if (artifact.alias.isNotBlank()) plugin(artifact.alias, dependencyName.current).version(artifact.version().fixed)
                }
            }
        }.onFailure {
            when (it) {
                is InvalidUserDataException -> SError.make("Illegal name called in Gradle version catalogs", it)
                else -> throw it
            }
        }
    }

    /**
     * 处理库依赖可访问 [Class] 装载
     * @param rootProject 当前根项目
     */
    internal fun resolveAccessors(rootProject: Project) {
        if (Dependencies.isOutdate || accessorsDir.resolve(accessorsPomData.relativePomPath).isEmpty())
            accessorsGenerator.build().compile(accessorsPomData, accessorsDir.absolutePath, accessorsGenerator.compileStubFiles)
        rootProject.addDependencyToBuildScript(accessorsDir.absolutePath, accessorsPomData)
    }

    /**
     * 部署库依赖可访问 [Class]
     * @param project 当前项目
     * @param extension 当前扩展实例
     */
    internal fun deployAccessors(project: Project, extension: ExtensionAware) =
        accessorsGenerator.librariesClasses.forEach { (name, className) ->
            val accessorsClass = project.loadBuildScriptClass(className) ?: SError.make(
                """
                  Generated class "$className" not found, stop loading $project
                  Please check whether the initialization process is interrupted and re-run Gradle Sync
                  If this doesn't work, please manually delete the entire "${accessorsDir.absolutePath}" directory
                """.trimIndent()
            )
            extension.getOrCreate(name, accessorsClass)
        }

    /**
     * 处理自动装配的插件依赖
     * @param spec 当前插件依赖声明对象
     * @param params 当前参数数组
     * @return [PluginDependencySpec]
     */
    internal fun resolveAutowire(spec: PluginDependenciesSpec, params: Array<out Any>): PluginDependencySpec {
        if (params.isEmpty()) SError.make("The autowire function need a param to resolve plugin")
        if (params.size > 2) SError.make("The autowire function currently does not support more than 2 params of plugin")
        return when (params[0]) {
            is String -> {
                val entry = Dependencies.findPlugins { key, value -> params[0] == key.current || params[0] == value.alias }.single()
                    ?: SError.make("Failed to resolve plugin \"${params[0]}\", also tried alias")
                val version = if (params.size == 2)
                    entry.value.versions()[params[1]] ?: SError.make("Failed to resolve plugin \"${params[0]}\" with version alias \"${params[1]}\"")
                else entry.value.version()
                spec.applyPlugin(entry.key.current, version.deployed)
            }
            else -> spec.applyPlugin(params[0])
        }
    }

    /**
     * 处理自动装配的依赖
     * @param project 当前项目 - 默认为 [ProjectTransaction.current]
     * @param params 当前参数数组
     * @return [Any]
     */
    internal fun resolveAutowire(project: Project = ProjectTransaction.current, params: Array<out String>): Any {
        if (params.isEmpty()) SError.make("The autowire function need a param to resolve library")
        return if (params[0].let { it.contains("/").not() && it.contains("\\").not() && it.startsWith("(").not() && it.endsWith(")").not() }) {
            if (params.size > 2) SError.make("The autowire function currently does not support more than 2 params of external dependency")
            val entry = Dependencies.findLibraries { key, value -> params[0] == key.current || params[0] == value.alias }.single()
                ?: SError.make("Failed to resolve library \"${params[0]}\", also tried alias")
            val version = if (params.size == 2)
                entry.value.versions()[params[1]] ?: SError.make("Failed to resolve library \"${params[0]}\" with version alias \"${params[1]}\"")
            else entry.value.version()
            ExternalDependencyDelegate(entry.key.groupId, entry.key.artifactId, version.deployed)
        } else mutableListOf<String>().let {
            params.forEach { param ->
                val relativePath = if (param.startsWith("(") && param.endsWith(")")) param.replace("(", "").replace(")", "") else param
                it.addAll(relativePath.toAbsoluteFilePaths(project.projectDir.absolutePath).onEach { path ->
                    if (path.toFile().isValidZip().not()) SError.make(
                        """
                          Invalid library at file path $path
                          The file collection dependency needs to be a valid zip package
                        """.trimIndent()
                    )
                })
            }; project.files(it.toTypedArray())
        }
    }
}