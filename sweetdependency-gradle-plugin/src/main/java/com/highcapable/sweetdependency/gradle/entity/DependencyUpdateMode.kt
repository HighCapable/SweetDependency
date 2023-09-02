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
 * This file is Created by fankes on 2023/8/18.
 */
package com.highcapable.sweetdependency.gradle.entity

/**
 * 依赖更新模式实体类
 * @param dependencyType 依赖类型
 * @param updateType 依赖更新模式
 */
internal data class DependencyUpdateMode(internal var dependencyType: DependencyType, internal var updateType: UpdateType) {

    /**
     * 依赖类型定义类
     */
    internal enum class DependencyType {
        /** 全部类型 */
        ALL,

        /** 插件依赖 */
        PLUGINS,

        /** 库依赖 */
        LIBRARIES,
    }

    /**
     * 依赖更新模式类型定义类
     */
    internal enum class UpdateType {
        /** 可选更新 */
        UPDATE_OPTIONAL,

        /** 全部更新 */
        UPDATE_ALL,

        /** 仅自动装配 */
        ONLY_AUTOWIRE,
    }
}