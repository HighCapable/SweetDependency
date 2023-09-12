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
 * This file is created by fankes on 2023/5/18.
 */
package com.highcapable.sweetdependency.manager.const

/**
 * 内置存储库
 */
internal object InternalRepositories {

    /** 本地 Maven 存储库相对路径 */
    const val MAVEN_LOCAL_RELATIVE_PATH = ".m2/repository"

    /** Google Maven */
    const val GOOGLE = "https://dl.google.com/dl/android/maven2"

    /** 中央存储库 */
    const val MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2"

    /** Gradle Plugin 存储库 */
    const val GRADLE_PLUGIN_PORTAL = "https://plugins.gradle.org/m2"

    /**
     * 存储库简洁名称定义类
     */
    internal object Name {

        /** Google Maven */
        const val GOOGLE = "google"

        /** 中央存储库 */
        const val MAVEN_CENTRAL = "maven-central"

        /** 本地 Maven 存储库 */
        const val MAVEN_LOCAL = "maven-local"

        /** Maven 存储库 */
        const val MAVEN = "maven"

        /** Ivy 存储库 */
        const val IVY = "ivy"

        /** Gradle Plugin 存储库 */
        const val GRADLE_PLUGIN_PORTAL = "gradle-plugin-portal"
    }
}