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
 * This file is created by fankes on 2023/8/6.
 */
package com.highcapable.sweetdependency.utils.code

import com.highcapable.sweetdependency.exception.SweetDependencyUnresolvedException
import com.highcapable.sweetdependency.utils.code.entity.MavenPomData
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.deleteEmptyRecursively
import com.highcapable.sweetdependency.utils.toFile
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import java.io.File
import javax.tools.DiagnosticCollector
import javax.tools.JavaFileObject
import javax.tools.StandardLocation
import javax.tools.ToolProvider

/**
 * 代码编译处理类
 */
internal object CodeCompiler {

    /** Maven 模型版本 */
    private const val MAVEN_MODEL_VERSION = "4.0.0"

    /**
     * 编译 [JavaFileObject] 为 Maven 依赖
     * @param pomData Maven POM 实体
     * @param outputDirPath 编译输出目录路径
     * @param files [JavaFileObject] 数组
     * @param compileOnlyFiles [JavaFileObject] 仅编译数组 - 默认空
     * @throws IllegalStateException 如果编译失败
     */
    internal fun compile(
        pomData: MavenPomData,
        outputDirPath: String,
        files: List<JavaFileObject>,
        compileOnlyFiles: List<JavaFileObject> = mutableListOf()
    ) {
        val outputDir = outputDirPath.toFile()
        if (files.isEmpty()) {
            if (outputDir.exists()) outputDir.deleteRecursively()
            return
        } else outputDir.also { if (it.exists().not()) it.mkdirs() }
        val outputBuildDir = "$outputDirPath/build".toFile().also { if (it.exists()) it.deleteRecursively(); it.mkdirs() }
        val outputClassesDir = "${outputBuildDir.absolutePath}/classes".toFile().apply { mkdirs() }
        val outputSourcesDir = "${outputBuildDir.absolutePath}/sources".toFile().apply { mkdirs() }
        val compiler = ToolProvider.getSystemJavaCompiler()
        val diagnostics = DiagnosticCollector<JavaFileObject>()
        val fileManager = compiler.getStandardFileManager(diagnostics, null, null)
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, listOf(outputClassesDir))
        val task = compiler.getTask(null, fileManager, diagnostics, null, null, compileOnlyFiles + files)
        val result = task.call()
        var diagnosticsMessage = ""
        diagnostics.diagnostics?.forEach { diagnostic ->
            diagnosticsMessage += "  > Error on line ${diagnostic.lineNumber} in ${diagnostic.source?.toUri()}\n"
            diagnosticsMessage += "    ${diagnostic.getMessage(null)}\n"
        }
        runCatching { fileManager.close() }
        if (result) {
            compileOnlyFiles.forEach { "${outputClassesDir.absolutePath}/${it.name}".replace(".java", ".class").toFile().delete() }
            files.forEach {
                it.toFiles(outputSourcesDir).also { (sourceDir, sourceFile) ->
                    sourceDir.mkdirs()
                    sourceFile.writeText(it.getCharContent(true).toString())
                }
            }; outputClassesDir.deleteEmptyRecursively()
            writeMetaInf(outputClassesDir)
            writeMetaInf(outputSourcesDir)
            createJarAndPom(pomData, outputDir, outputBuildDir, outputClassesDir, outputSourcesDir)
        } else SError.make("Failed to compile java files into path: $outputDirPath\n$diagnosticsMessage")
    }

    /**
     * 打包 JAR 并写入 POM
     * @param pomData Maven POM 实体
     * @param outputDir 编译输出目录
     * @param buildDir 编译目录
     * @param classesDir 编译二进制目录
     * @param sourcesDir 编译源码目录
     */
    private fun createJarAndPom(pomData: MavenPomData, outputDir: File, buildDir: File, classesDir: File, sourcesDir: File) {
        val pomDir = outputDir.resolve(pomData.relativePomPath).also { if (it.exists().not()) it.mkdirs() }
        packageToJar(classesDir, pomDir, pomData, isSourcesJar = false)
        packageToJar(sourcesDir, pomDir, pomData, isSourcesJar = true)
        writePom(pomDir, pomData)
        buildDir.deleteRecursively()
    }

    /**
     * 写入 META-INF/MANIFEST.MF
     * @param dir 当前目录
     */
    private fun writeMetaInf(dir: File) {
        val metaInfDir = dir.resolve("META-INF").apply { mkdirs() }
        metaInfDir.resolve("MANIFEST.MF").writeText("Manifest-Version: 1.0")
    }

    /**
     * 写入 POM
     * @param dir 当前目录
     * @param pomData Maven POM 实体
     */
    private fun writePom(dir: File, pomData: MavenPomData) =
        dir.resolve("${pomData.artifactId}-${pomData.version}.pom").writeText(
            """
              <?xml version="1.0" encoding="UTF-8"?>
              <project
                xmlns="http://maven.apache.org/POM/$MAVEN_MODEL_VERSION"
                xsi:schemaLocation="http://maven.apache.org/POM/$MAVEN_MODEL_VERSION https://maven.apache.org/xsd/maven-$MAVEN_MODEL_VERSION.xsd"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <modelVersion>$MAVEN_MODEL_VERSION</modelVersion>
                <groupId>${pomData.groupId}</groupId>
                <artifactId>${pomData.artifactId}</artifactId>
                <version>${pomData.version}</version>
              </project>
            """.trimIndent()
        )

    /**
     * 转换到文件
     * @param outputDir 输出目录
     * @return [Pair]<[File], [File]>
     */
    private fun JavaFileObject.toFiles(outputDir: File): Pair<File, File> {
        val outputDirPath = outputDir.absolutePath
        val separator = if (name.contains("/")) "/" else "\\"
        val names = name.split(separator)
        val fileName = names[names.lastIndex]
        val folderName = name.replace(fileName, "")
        return "$outputDirPath/$folderName".toFile() to "$outputDirPath/$name".toFile()
    }

    /**
     * 打包编译输出目录到 JAR
     * @param buildDir 编译目录
     * @param outputDir 输出目录
     * @param pomData Maven POM 实体
     * @param isSourcesJar 是否为源码 JAR
     * @throws SweetDependencyUnresolvedException 如果编译输出目录不存在
     */
    private fun packageToJar(buildDir: File, outputDir: File, pomData: MavenPomData, isSourcesJar: Boolean) {
        if (buildDir.exists().not()) SError.make("Jar file output path not found: ${buildDir.absolutePath}")
        val jarFile = outputDir.resolve("${pomData.artifactId}-${pomData.version}${if (isSourcesJar) "-sources" else ""}.jar")
        if (jarFile.exists()) jarFile.delete()
        ZipFile(jarFile).addFolder(buildDir, ZipParameters().apply { isIncludeRootFolder = false })
    }
}