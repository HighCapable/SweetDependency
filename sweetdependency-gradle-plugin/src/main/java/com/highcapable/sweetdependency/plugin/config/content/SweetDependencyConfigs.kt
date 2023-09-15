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
 * This file is created by fankes on 2023/6/29.
 */
package com.highcapable.sweetdependency.plugin.config.content

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.document.RootConfigDocument
import com.highcapable.sweetdependency.document.mapping.RootConfigDocumentMapping
import com.highcapable.sweetdependency.exception.SweetDependencyUnresolvedException
import com.highcapable.sweetdependency.plugin.config.factory.build
import com.highcapable.sweetdependency.plugin.config.proxy.ISweetDependencyConfigs
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.debug.SLog
import com.highcapable.sweetdependency.utils.yaml.factory.YamlException
import kotlin.properties.Delegates

/**
 * [SweetDependency] 配置类实现类
 */
internal object SweetDependencyConfigs {

    /** 当前配置 */
    internal var configs by Delegates.notNull<ISweetDependencyConfigs>()

    /** 当前文档实体 */
    internal var document by Delegates.notNull<RootConfigDocument>()

    /** 当前文档测绘实例 */
    internal var documentMapping by Delegates.notNull<RootConfigDocumentMapping>()

    /**
     * 插件启用后执行
     * @param block 方法体
     */
    internal inline fun withPluginEnable(block: () -> Unit) {
        if (configs.isEnable) block() else SLog.warn("${SweetDependency.TAG} is disabled (won't do anything)", noRepeat = true)
    }

    /**
     * 初始化配置 (从文件)
     * @param configs 当前配置
     * @param isThrowOnError 是否在发生错误的时候抛出异常 - 默认是
     * @throws SweetDependencyUnresolvedException 如果设置了 [isThrowOnError] 且发生错误
     */
    internal fun initialize(configs: ISweetDependencyConfigs, isThrowOnError: Boolean = true) {
        this.configs = configs
        runCatching {
            configs.build().also {
                document = it.first
                documentMapping = it.second
            }
        }.onFailure {
            if (isThrowOnError) when (it) {
                is YamlException -> SError.make("Failed to parse config file: ${configs.configFilePath}\nPlease check if there are syntax errors", it)
                is SweetDependencyUnresolvedException -> throw it
                else -> SError.make("Failed to load config file: ${configs.configFilePath}", it)
            }
        }
    }
}