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
 * This file is created by fankes on 2023/5/19.
 */
package com.highcapable.sweetdependency.utils.yaml.factory

import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar

/** YAML 异常类型定义 */
internal typealias YamlException = YamlException

/** YAML 节点数组类型定义 */
internal typealias YamlMapEntries = Map<YamlScalar, YamlNode>

/**
 * 转换为 YAML 节点数组
 * @return [YamlMapEntries] or null
 */
internal fun YamlNode.asMap() = (this as? YamlMap)?.entries

/**
 * 获取 YAML 节点是否存在
 * @param key 节点名称
 * @return [Boolean]
 */
internal fun YamlMapEntries.isKeyExist(key: String) = keys.singleOrNull { it.content == key } != null