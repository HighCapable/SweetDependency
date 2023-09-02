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
 * This file is Created by fankes on 2023/6/3.
 */
@file:Suppress("unused")

package org.gradle.kotlin.dsl

import com.highcapable.sweetdependency.gradle.factory.configure
import com.highcapable.sweetdependency.gradle.factory.get
import com.highcapable.sweetdependency.plugin.extension.dsl.configure.SweetDependencyConfigureExtension
import org.gradle.api.Action
import org.gradle.api.initialization.Settings

/**
 * WORKAROUND: for some reason a type-safe accessor is not generated for the extension,
 * even though it is present in the extension container where the plugin is applied.
 * This seems to work fine, and the extension methods are only available when the plugin
 * is actually applied.
 *
 * See related link [here](https://stackoverflow.com/questions/72627792/gradle-settings-plugin-extension)
 */

/**
 * Retrieves the [SweetDependencyConfigureExtension] extension.
 * @return [SweetDependencyConfigureExtension]
 */
val Settings.sweetDependency get() = get<SweetDependencyConfigureExtension>(SweetDependencyConfigureExtension.NAME)

/**
 * Configures the [SweetDependencyConfigureExtension] extension.
 * @param configure
 */
fun Settings.sweetDependency(configure: Action<SweetDependencyConfigureExtension>) = configure(SweetDependencyConfigureExtension.NAME, configure)