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
 * This file is created by fankes on 2023/6/25.
 */
@file:Suppress("USELESS_ELVIS", "KotlinRedundantDiagnosticSuppress")

package com.highcapable.sweetdependency.gradle.delegate.entity

import com.highcapable.sweetdependency.document.factory.spliceToDependencyNotation
import com.highcapable.sweetdependency.gradle.entity.DependencyVersion
import com.highcapable.sweetdependency.manager.GradleTaskManager
import com.highcapable.sweetdependency.utils.debug.SError
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency

/**
 * 外部存储库依赖实体代理类
 *
 * 代理 [DefaultExternalModuleDependency]
 * @param groupId Group ID
 * @param artifactId Artifact ID
 * @param version 版本
 */
internal open class ExternalDependencyDelegate internal constructor(
    @get:JvmName("getDelegateGroupId")
    @set:JvmName("setDelegateGroupId")
    var groupId: String,
    @get:JvmName("getDelegateArtifactId")
    @set:JvmName("setDelegateArtifactId")
    var artifactId: String,
    @get:JvmName("getDelegateVersion")
    @set:JvmName("setDelegateVersion")
    var version: String
) : DefaultExternalModuleDependency(groupId, artifactId, version) {

    override fun getVersion(): String {
        val notation = spliceToDependencyNotation(groupId, artifactId)
        if (version == DependencyVersion.AUTOWIRE_VERSION_NAME && !GradleTaskManager.isInternalRunningTask) SError.make(
            """
              This library "$notation" is not autowired and cannot be deployed
              You can try the following solutions to resolve this problem:
              1. Manually re-run Gradle Sync (make sure "autowire-on-sync-mode" not be "OFF")
              2. Manually run "${GradleTaskManager.AUTOWIRE_LIBRARIES_TASK_NAME}" task and re-run Gradle Sync
              3. Fill an existing version for dependency "$notation" and re-run Gradle Sync
              If you get this error again after doing the above, maybe the currently set repositories cannot find this library
            """.trimIndent()
        ); return super.getVersion() ?: version
    }

    override fun toString() = "ExternalDependencyDelegate(groupId = $groupId, artifactId = $artifactId, version = $version)"
}