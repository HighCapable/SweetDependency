/*
 * SweetDependency - An easy autowire and manage dependencies Gradle plugin.
 * Copyright (C) 2019-2024 HighCapable
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
 * This file is created by fankes on 2023/8/6.
 */
@file:Suppress("unused")

package com.highcapable.sweetdependency.utils.code.factory

import com.highcapable.sweetdependency.plugin.SweetDependencyExtension
import com.highcapable.sweetdependency.utils.code.CodeCompiler
import com.highcapable.sweetdependency.utils.code.entity.MavenPomData
import com.squareup.javapoet.JavaFile
import javax.tools.JavaFileObject

/**
 * 编译 [JavaFile] 为 Maven 依赖
 * @param pomData Maven POM 实体
 * @param outputDirPath 编译输出目录路径
 * @param compileOnlyFiles [JavaFile] 仅编译数组 - 默认空
 * @throws SweetDependencyExtension 如果编译失败
 */
@JvmName("compileWithJavaFile")
internal fun JavaFile.compile(pomData: MavenPomData, outputDirPath: String, compileOnlyFiles: List<JavaFile> = mutableListOf()) =
    CodeCompiler.compile(
        pomData = pomData,
        outputDirPath = outputDirPath,
        files = listOf(toJavaFileObject()),
        compileOnlyFiles = mutableListOf<JavaFileObject>().also { compileOnlyFiles.forEach { e -> it.add(e.toJavaFileObject()) } }
    )

/**
 * 编译 [JavaFile] 为 Maven 依赖
 * @param pomData Maven POM 实体
 * @param outputDirPath 编译输出目录路径
 * @param compileOnlyFiles [JavaFile] 仅编译数组 - 默认空
 * @throws SweetDependencyExtension 如果编译失败
 */
@JvmName("compileWithJavaFile")
internal fun List<JavaFile>.compile(pomData: MavenPomData, outputDirPath: String, compileOnlyFiles: List<JavaFile> = mutableListOf()) =
    CodeCompiler.compile(
        pomData = pomData,
        outputDirPath = outputDirPath,
        files = mutableListOf<JavaFileObject>().also { forEach { e -> it.add(e.toJavaFileObject()) } },
        compileOnlyFiles = mutableListOf<JavaFileObject>().also { compileOnlyFiles.forEach { e -> it.add(e.toJavaFileObject()) } }
    )

/**
 * 编译 [JavaFileObject] 为 Maven 依赖
 * @param pomData Maven POM 实体
 * @param outputDirPath 编译输出目录路径
 * @param compileOnlyFiles [JavaFileObject] 仅编译数组 - 默认空
 * @throws SweetDependencyExtension 如果编译失败
 */
@JvmName("compileWithJavaFileObject")
internal fun JavaFileObject.compile(pomData: MavenPomData, outputDirPath: String, compileOnlyFiles: List<JavaFileObject> = mutableListOf()) =
    CodeCompiler.compile(pomData, outputDirPath, listOf(this), compileOnlyFiles)

/**
 * 编译 [JavaFileObject] 为 Maven 依赖
 * @param pomData Maven POM 实体
 * @param outputDirPath 编译输出目录路径
 * @param compileOnlyFiles [JavaFileObject] 仅编译数组 - 默认空
 * @throws SweetDependencyExtension 如果编译失败
 */
@JvmName("compileWithJavaFileObject")
internal fun List<JavaFileObject>.compile(pomData: MavenPomData, outputDirPath: String, compileOnlyFiles: List<JavaFileObject> = mutableListOf()) =
    CodeCompiler.compile(pomData, outputDirPath, files = this, compileOnlyFiles)