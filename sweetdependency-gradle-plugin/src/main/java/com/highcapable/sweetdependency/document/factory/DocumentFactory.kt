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
 * This file is created by fankes on 2023/5/31.
 */
package com.highcapable.sweetdependency.document.factory

import com.highcapable.sweetdependency.document.DependencyDocument
import com.highcapable.sweetdependency.document.PreferencesDocument
import com.highcapable.sweetdependency.document.RepositoryDocument
import com.highcapable.sweetdependency.gradle.entity.DependencyName
import com.highcapable.sweetdependency.gradle.entity.DependencyUpdateMode
import com.highcapable.sweetdependency.gradle.entity.DependencyVersion
import com.highcapable.sweetdependency.gradle.factory.isUnSafeExtName
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.firstNumberToLetter

/** 存储库文档实体类型定义 */
internal typealias RepositoryList = MutableList<RepositoryDocument>

/** 依赖文档实体类型定义 */
internal typealias DependencyMap = MutableMap<DependencyName, DependencyDocument>

/** 依赖文档更新实体类型定义 */
internal typealias DependencyUpdateMap = MutableMap<String, Pair<DependencyName, DependencyVersion>>

/** 依赖文档查找条件类型定义 */
internal typealias DependenciesCondition = (dependencyName: DependencyName, artifact: DependencyDocument) -> Boolean

/**
 * 转换 [PreferencesDocument.AutowireOnSyncMode] 到 [DependencyUpdateMode]
 *
 * 如果为 [PreferencesDocument.AutowireOnSyncMode.OFF] 则会返回 null
 * @return [DependencyUpdateMode] or null
 */
internal fun PreferencesDocument.AutowireOnSyncMode.toUpdateMode() = when (this) {
    PreferencesDocument.AutowireOnSyncMode.UPDATE_OPTIONAL_DEPENDENCIES ->
        DependencyUpdateMode(DependencyUpdateMode.DependencyType.ALL, DependencyUpdateMode.UpdateType.UPDATE_OPTIONAL)
    PreferencesDocument.AutowireOnSyncMode.UPDATE_ALL_DEPENDENCIES ->
        DependencyUpdateMode(DependencyUpdateMode.DependencyType.ALL, DependencyUpdateMode.UpdateType.UPDATE_ALL)
    PreferencesDocument.AutowireOnSyncMode.ONLY_AUTOWIRE_DEPENDENCIES ->
        DependencyUpdateMode(DependencyUpdateMode.DependencyType.ALL, DependencyUpdateMode.UpdateType.ONLY_AUTOWIRE)
    PreferencesDocument.AutowireOnSyncMode.UPDATE_OPTIONAL_PLUGINS ->
        DependencyUpdateMode(DependencyUpdateMode.DependencyType.PLUGINS, DependencyUpdateMode.UpdateType.UPDATE_OPTIONAL)
    PreferencesDocument.AutowireOnSyncMode.UPDATE_ALL_PLUGINS ->
        DependencyUpdateMode(DependencyUpdateMode.DependencyType.PLUGINS, DependencyUpdateMode.UpdateType.UPDATE_ALL)
    PreferencesDocument.AutowireOnSyncMode.ONLY_AUTOWIRE_PLUGINS ->
        DependencyUpdateMode(DependencyUpdateMode.DependencyType.PLUGINS, DependencyUpdateMode.UpdateType.ONLY_AUTOWIRE)
    PreferencesDocument.AutowireOnSyncMode.UPDATE_OPTIONAL_LIBRARIES ->
        DependencyUpdateMode(DependencyUpdateMode.DependencyType.LIBRARIES, DependencyUpdateMode.UpdateType.UPDATE_OPTIONAL)
    PreferencesDocument.AutowireOnSyncMode.UPDATE_ALL_LIBRARIES ->
        DependencyUpdateMode(DependencyUpdateMode.DependencyType.LIBRARIES, DependencyUpdateMode.UpdateType.UPDATE_ALL)
    PreferencesDocument.AutowireOnSyncMode.ONLY_AUTOWIRE_LIBRARIES ->
        DependencyUpdateMode(DependencyUpdateMode.DependencyType.LIBRARIES, DependencyUpdateMode.UpdateType.ONLY_AUTOWIRE)
    PreferencesDocument.AutowireOnSyncMode.OFF -> null
}

/**
 * 合并到依赖名称
 * @param groupId Group ID
 * @param artifactId Artifact ID
 * @return [String]
 */
internal fun spliceToDependencyNotation(groupId: String, artifactId: String) = "$groupId:$artifactId"

/**
 * 分离到依赖名称数组
 *
 * "com.mylibrary:library-core" → "com.mylibrary" | "library-core"
 * @return [List]<[String]>
 */
internal fun String.splitToDependencyNames() = trim().split(":").apply { if (size != 2) SError.make("Invalid dependency name \"$this\"") }

/**
 * 分离到依赖生成名称数组
 *
 * "com.mylibrary:library-core" → "com" | "mylibrary" | "library" | "core"
 * @return [List]<[String]>
 */
internal fun String.splitToDependencyGenerateNames() =
    trim().replace("_", "|").replace(".", "|").replace(":", "|").replace("-", "|").split("|").filter { it.isNotBlank() }

/**
 * 转换到依赖 URL 名称
 *
 * "com.mylibrary:library-core" → "com/mylibrary/library-core"
 * @return [String]
 */
internal fun String.convertToDependencyUrlName() = splitToDependencyNames().let { "${it[0].replace(".", "/")}/${it[1]}" }

/**
 * 转换到依赖模糊分离名称 (使用 [symbol] 进行分离)
 *
 * "com.mylibrary:library-core" → "com[symbol]mylibrary[symbol]library[symbol]core"
 * @param symbol 分隔符 - 默认 "."
 * @param isReplaceFirstChar 是否使用 [firstNumberToLetter] 替换每一段第一个字符 - 默认否
 * @param isLowerCase 是否全部转换为小写 - 默认是
 * @return [String]
 */
internal fun String.convertToDependencyAmbiguousName(symbol: String = ".", isReplaceFirstChar: Boolean = false, isLowerCase: Boolean = true) =
    mutableListOf<String>().apply {
        trim().replace(".", "|").replace("_", "|").replace(":", "|").replace("-", "|").split("|").forEach {
            add(if (isReplaceFirstChar) it.firstNumberToLetter() else it)
        }
    }.joinToString(symbol).let { if (isLowerCase) it.lowercase() else it }

/**
 * 检查名称、别名是否合法
 *
 * - 只能包含：'0-9'、'A-Z'、'a-z'、'.'、'_'、'-' 且必须以字母开头 (长度至少为 3 位)
 * - 不能是 [isUnSafeExtName]
 * @param content 内容
 * @param isCheckExtName 是否同时检查是否为 Gradle 使用的关键字名称 - 默认否
 * @param isCheckMultiName 是否同时检查是否可被 [splitToDependencyGenerateNames] 分割为两位及以上名称 - 默认否
 * @throws IllegalArgumentException 如果名称、别名不合法
 */
internal fun String.checkingName(content: String, isCheckExtName: Boolean = false, isCheckMultiName: Boolean = false) {
    if (isBlank()) return
    if (length < 3) SError.make("Illegal $content \"$this\", the length of $content must be >= 3")
    /**
     * 检查是否为 Gradle 使用的关键字名称
     * @param isEnable 默认跟随 [isCheckExtName]
     * @throws IllegalArgumentException 如果名称、别名不合法
     */
    fun String.checkUnSafeExtName(isEnable: Boolean = isCheckExtName) {
        if (isEnable && isUnSafeExtName()) SError.make("This $content \"$this\" of \"${this@checkingName}\" is a Gradle built-in extension")
    }
    checkUnSafeExtName()
    if (isCheckMultiName) splitToDependencyGenerateNames().also { splitedNames ->
        if (splitedNames.isEmpty()) SError.make("This $content \"$this\" cannot be split, please check and try again")
        if (splitedNames.size < 2) SError.make("This $content \"$this\" must be able to be split into at least 2 parts")
        splitedNames[0].checkUnSafeExtName(isEnable = true)
        splitedNames.forEach {
            if (it.first() !in 'A'..'Z' && it.first() !in 'a'..'z')
                SError.make("Illegal $content \"$it\" of \"$this\", it must start with a letter")
        }
    }
    forEachIndexed { index, char ->
        if ((char !in 'A'..'Z' && char !in 'a'..'z' && index == 0) ||
            (char !in 'A'..'Z' && char !in 'a'..'z' &&
                char !in '0'..'9' && char != '_' && char != '-' && char != '.')
        ) SError.make("Illegal $content \"$this\", it only allow 26 letters (upper and lower case) and '.', '_', '-' and must start with a letter")
    }
}