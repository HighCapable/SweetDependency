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
 * This file is created by fankes on 2023/5/16.
 */
package com.highcapable.sweetdependency.manager.const

/**
 * 附加常用第三方存储库
 */
internal object AdditionalRepositories {

    /** 中央存储库 (分流) */
    const val MAVEN_CENTRAL_BRANCH = "https://repo1.maven.org/maven2"

    /** JitPack */
    const val JITPACK = "https://www.jitpack.io"

    /** OSS Release */
    const val SONATYPE_OSS_RELEASES = "https://s01.oss.sonatype.org/content/repositories/releases"

    /** OSS Snapshots */
    const val SONATYPE_OSS_SNAPSHOTS = "https://s01.oss.sonatype.org/content/repositories/snapshots"

    /** 阿里云 Google 存储库镜像 */
    const val ALIYUN_GOOGLE_MIRROR = "https://maven.aliyun.com/repository/google"

    /** 阿里云中央存储库镜像 */
    const val ALIYUN_MAVEN_CENTRAL_MIRROR = "https://maven.aliyun.com/repository/central"

    /** 阿里云公共存储库镜像 */
    const val ALIYUN_MAVEN_PUBLIC_MIRROR = "https://maven.aliyun.com/repository/public"

    /** 阿里云 JCenter 镜像 */
    const val ALIYUN_JCENTER_MIRROR = "https://maven.aliyun.com/nexus/content/repositories/jcenter"

    /**
     * 存储库简洁名称定义类
     */
    internal object Name {

        /** 中央存储库 (分流) */
        const val MAVEN_CENTRAL_BRANCH = "maven-central-branch"

        /** JitPack */
        const val JITPACK = "jit-pack"

        /** OSS Release */
        const val SONATYPE_OSS_RELEASES = "sonatype-oss-releases"

        /** OSS Snapshots */
        const val SONATYPE_OSS_SNAPSHOTS = "sonatype-oss-snapshots"

        /** 阿里云 Google 存储库镜像 */
        const val ALIYUN_GOOGLE_MIRROR = "aliyun-google-mirror"

        /** 阿里云中央存储库镜像 */
        const val ALIYUN_MAVEN_CENTRAL_MIRROR = "aliyun-maven-central-mirror"

        /** 阿里云公共存储库镜像 */
        const val ALIYUN_MAVEN_PUBLIC_MIRROR = "aliyun-maven-public-mirror"

        /** 阿里云 JCenter 镜像 */
        const val ALIYUN_JCENTER_MIRROR = "aliyun-jcenter-mirror"
    }
}