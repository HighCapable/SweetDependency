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
 * This file is created by fankes on 2023/5/16.
 */
package com.highcapable.sweetdependency.manager.content

import com.highcapable.sweetdependency.document.RepositoryDocument
import com.highcapable.sweetdependency.document.factory.RepositoryList
import com.highcapable.sweetdependency.manager.const.AdditionalRepositories
import com.highcapable.sweetdependency.manager.const.InternalRepositories
import com.highcapable.sweetdependency.utils.parseFileSeparator

/**
 * 已添加的存储库管理类
 */
internal object Repositories {

    /** 默认本地 Maven 存储库路径 */
    internal val defaultMavenLocalPath by lazy {
        "${System.getProperty("user.home")}/${InternalRepositories.MAVEN_LOCAL_RELATIVE_PATH}".parseFileSeparator()
    }

    /** 默认版本过滤器排除列表数组 */
    internal val defaultVersionFilterExclusionList = arrayOf("-beta", "-alpha", "-dev", "-canary", "-pre", "-rc", "-ga", "-snapshot")

    /** 当前已添加的全部存储库数组 */
    private val entries = mutableListOf<RepositoryDocument>()

    /**
     * 获取当前存储库数组
     * @return [MutableList]<[RepositoryDocument]>
     */
    internal fun all() = entries

    /**
     * 当前是否存在存储库
     * @return [Boolean]
     */
    internal fun isEmpty() = all().isEmpty()

    /**
     * 当前是否不存在存储库
     * @return [Boolean]
     */
    internal fun isNotEmpty() = !isEmpty()

    /**
     * 生成存储库数组
     * @param repositories 存储库数组
     */
    internal fun generate(repositories: RepositoryList) {
        if (repositories == all()) return
        resetData()
        all().addAll(repositories)
    }

    /**
     * 查找可用的存储库名 URL 地址
     * @param name 存储库名
     * @return [String]
     */
    internal fun findAdditional(name: String) = when (name) {
        AdditionalRepositories.Name.MAVEN_CENTRAL_BRANCH -> AdditionalRepositories.MAVEN_CENTRAL_BRANCH
        AdditionalRepositories.Name.JITPACK -> AdditionalRepositories.JITPACK
        AdditionalRepositories.Name.SONATYPE_OSS_RELEASES -> AdditionalRepositories.SONATYPE_OSS_RELEASES
        AdditionalRepositories.Name.SONATYPE_OSS_SNAPSHOTS -> AdditionalRepositories.SONATYPE_OSS_SNAPSHOTS
        AdditionalRepositories.Name.ALIYUN_GOOGLE_MIRROR -> AdditionalRepositories.ALIYUN_GOOGLE_MIRROR
        AdditionalRepositories.Name.ALIYUN_MAVEN_CENTRAL_MIRROR -> AdditionalRepositories.ALIYUN_MAVEN_CENTRAL_MIRROR
        AdditionalRepositories.Name.ALIYUN_MAVEN_PUBLIC_MIRROR -> AdditionalRepositories.ALIYUN_MAVEN_PUBLIC_MIRROR
        AdditionalRepositories.Name.ALIYUN_JCENTER_MIRROR -> AdditionalRepositories.ALIYUN_JCENTER_MIRROR
        else -> ""
    }

    /** 重置 (清空) 当前存储库数组 */
    private fun resetData() = entries.clear()
}