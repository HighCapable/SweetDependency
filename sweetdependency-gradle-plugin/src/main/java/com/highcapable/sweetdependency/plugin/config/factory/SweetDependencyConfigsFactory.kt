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
 * This file is created by fankes on 2023/5/21.
 */
package com.highcapable.sweetdependency.plugin.config.factory

import com.highcapable.sweetdependency.document.RootConfigDocument
import com.highcapable.sweetdependency.document.mapping.RootConfigDocumentMapping
import com.highcapable.sweetdependency.plugin.config.proxy.ISweetDependencyConfigs
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.debug.SLog
import com.highcapable.sweetdependency.utils.toFile
import com.highcapable.sweetdependency.utils.yaml.Yaml
import java.io.File

/**
 * 获取并解析配置文件 [RootConfigDocument] 实体和 [RootConfigDocumentMapping]
 * @return [Pair]<[RootConfigDocument], [RootConfigDocumentMapping]>
 */
internal fun ISweetDependencyConfigs.build() = configFilePath.loadOrCreateEmpty() to RootConfigDocumentMapping(this)

/**
 * 通过字符串路径获取或创建配置文件 [RootConfigDocument] 实体
 * @return [RootConfigDocument]
 */
private fun String.loadOrCreateEmpty(): RootConfigDocument {
    toFile().apply {
        if (!name.endsWith(".yaml") && !name.endsWith(".yml"))
            SError.make("Config file name must be end with \".yaml\" or \".yml\"")
    }.createTemplateFileOrNot()
    return Yaml.loadFromFile<RootConfigDocument>(path = this)
}

/** 自动创建模版配置文件 */
private fun File.createTemplateFileOrNot() {
    fun createTemplateFile() {
        writeText(RootConfigDocument.defaultContent)
        SLog.info("Automatically created config file: $absolutePath")
    }
    runCatching {
        when {
            !exists() && !parentFile.exists() -> {
                parentFile.mkdirs()
                createTemplateFile()
            }
            !exists() -> createTemplateFile()
            exists() && isDirectory -> SError.make("Tries to create file path is a directory")
        }
    }.onFailure { SError.make("Could not automatically created config file: $absolutePath", it) }
}