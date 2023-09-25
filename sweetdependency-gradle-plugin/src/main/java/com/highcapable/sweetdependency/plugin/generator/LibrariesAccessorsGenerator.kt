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
 * This file is created by fankes on 2023/8/13.
 */
package com.highcapable.sweetdependency.plugin.generator

import com.highcapable.sweetdependency.SweetDependency
import com.highcapable.sweetdependency.document.DependencyDocument
import com.highcapable.sweetdependency.document.factory.splitToDependencyGenerateNames
import com.highcapable.sweetdependency.exception.SweetDependencyUnresolvedException
import com.highcapable.sweetdependency.generated.SweetDependencyProperties
import com.highcapable.sweetdependency.gradle.delegate.entity.ExternalDependencyDelegate
import com.highcapable.sweetdependency.gradle.entity.ExternalDependency
import com.highcapable.sweetdependency.manager.content.Dependencies
import com.highcapable.sweetdependency.plugin.extension.accessors.proxy.IExtensionAccessors
import com.highcapable.sweetdependency.utils.camelcase
import com.highcapable.sweetdependency.utils.capitalize
import com.highcapable.sweetdependency.utils.debug.SError
import com.highcapable.sweetdependency.utils.firstNumberToLetter
import com.highcapable.sweetdependency.utils.toNonJavaName
import com.highcapable.sweetdependency.utils.uncapitalize
import com.highcapable.sweetdependency.utils.uppercamelcase
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import java.text.SimpleDateFormat
import java.util.*
import javax.lang.model.element.Modifier

/**
 * 库依赖可访问 [Class] 生成实现类
 */
internal class LibrariesAccessorsGenerator {

    private companion object {

        /** 生成的 [Class] 所在包名 */
        private const val ACCESSORS_PACKAGE_NAME = "${SweetDependencyProperties.PROJECT_GROUP_NAME}.plugin.extension.accessors.generated"

        /** 生成的 [Class] 后缀名 */
        private const val CLASS_SUFFIX_NAME = "Accessors"

        /** 生成的首位 [Class] 名称 */
        private const val TOP_CLASS_NAME = "Libraries$CLASS_SUFFIX_NAME"

        /** 标识首位生成的 [Class] TAG */
        private const val TOP_SUCCESSIVE_NAME = "_top_successive_name"
    }

    /** 生成的依赖库 [Class] 构建器数组 */
    private val classSpecs = mutableMapOf<String, TypeSpec.Builder>()

    /** 生成的依赖库构造方法构建器数组 */
    private val constructorSpecs = mutableMapOf<String, MethodSpec.Builder>()

    /** 生成的依赖库预添加的构造方法 (父方法) 数组 */
    private val preAddConstructorSpecSupers = mutableListOf<Pair<String, ExternalDependency>>()

    /** 生成的依赖库预添加的构造方法名称数组 */
    private val preAddConstructorSpecNames = mutableListOf<Pair<String, String>>()

    /** 生成的依赖库 [Class] 扩展类名数组 */
    private val memoryExtensionClasses = mutableListOf<Pair<String, String>>()

    /** 生成的依赖库连续名称记录数组 */
    private val grandSuccessiveNames = mutableListOf<String>()

    /** 生成的依赖库连续名称重复次数数组 */
    private val grandSuccessiveDuplicateIndexs = mutableMapOf<String, Int>()

    /** 生成的依赖库不重复 TAG 数组 */
    private val usedSuccessiveTags = mutableSetOf<String>()

    /** 当前库依赖的命名空间 */
    private var librariesNamespace = ""

    /**
     * 不重复调用
     * @param tags 当前 TAG 数组
     * @param block 执行的方法块
     */
    private inline fun noRepeated(vararg tags: String, block: () -> Unit) {
        val allTag = tags.joinToString("-")
        if (usedSuccessiveTags.contains(allTag).not()) block()
        usedSuccessiveTags.add(allTag)
    }

    /**
     * 字符串首字母大写并添加 [CLASS_SUFFIX_NAME] 后缀
     * @return [String]
     */
    private fun String.capitalized() = "${capitalize()}$CLASS_SUFFIX_NAME"

    /**
     * 字符串首字母小写并添加 [CLASS_SUFFIX_NAME] 后缀
     * @return [String]
     */
    private fun String.uncapitalized() = "${uncapitalize()}$CLASS_SUFFIX_NAME"

    /**
     * 字符串类名转换为 [ClassName]
     * @param packageName 包名 - 默认空
     * @return [ClassName]
     */
    private fun String.asClassType(packageName: String = "") = ClassName.get(packageName, this)

    /**
     * 通过 [TypeSpec] 创建 [JavaFile]
     * @return [JavaFile]
     */
    private fun TypeSpec.createJavaFile(packageName: String) = JavaFile.builder(packageName, this).build()

    /**
     * 创建扩展类完整名称 (含包名)
     * @param name 子类名 - 默认空
     * @return [String]
     */
    private fun createAccessorsName(name: String = "") =
        "$ACCESSORS_PACKAGE_NAME.$TOP_CLASS_NAME${if (name.isNotBlank()) "\$${name.capitalized()}" else ""}"

    /**
     * 创建通用构建器描述类
     * @param name 名称
     * @param accessors 接续对象 - 没有默认值
     * @param isInner 是否为内部类 - 默认是
     * @return [TypeSpec.Builder]
     */
    private fun createClassSpec(name: String, accessors: Any = Any(), isInner: Boolean = true) =
        TypeSpec.classBuilder(if (isInner) name.capitalized() else name).apply {
            if (isInner) {
                val actual = when (accessors) {
                    is ExternalDependency -> "\"$accessors\" library"
                    else -> "\"$accessors\" accessors"
                }; addJavadoc("The $actual")
                addSuperinterface(IExtensionAccessors::class.java)
                addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            } else {
                addJavadoc(
                    """
                      This class is generated by ${SweetDependency.TAG} at ${SimpleDateFormat.getDateTimeInstance().format(Date())}
                      <br/>
                      The content here is automatically generated according to the dependencies of your projects
                      <br/>
                      You can visit <a href="${SweetDependency.PROJECT_URL}">here</a> for more help
                    """.trimIndent()
                )
                addModifiers(Modifier.PUBLIC)
            }
        }

    /**
     * 创建通用构造方法构建器描述类
     * @return [MethodSpec.Builder]
     */
    private fun createConstructorSpec() = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)

    /**
     * 向通用构建器描述类添加变量
     * @param accessors 接续对象
     * @param className 类名
     * @return [TypeSpec.Builder]
     */
    private fun TypeSpec.Builder.addSuccessiveField(accessors: Any, className: String) = addField(
        FieldSpec.builder(className.capitalized().asClassType(), className.uncapitalized(), Modifier.PRIVATE, Modifier.FINAL)
            .apply {
                val actual = when (accessors) {
                    is ExternalDependency -> "\"$accessors\" library"
                    else -> "\"$accessors\" accessors"
                }; addJavadoc("Create the $actual")
            }.build()
    )

    /**
     * 向通用构建器描述类添加方法
     * @param accessors 接续对象
     * @param methodName 方法名
     * @param className 类名
     * @return [TypeSpec.Builder]
     */
    private fun TypeSpec.Builder.addSuccessiveMethod(accessors: Any, methodName: String, className: String) =
        addMethod(
            MethodSpec.methodBuilder("get${methodName.capitalize()}")
                .apply {
                    val actual = when (accessors) {
                        is ExternalDependency -> "\"$accessors\" library"
                        else -> "\"$accessors\" accessors"
                    }; addJavadoc("Resolve the $actual")
                }.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(className.capitalized().asClassType())
                .addStatement("return ${className.uncapitalized()}")
                .build()
        )

    /**
     * 向通用构建器描述类添加依赖描述器
     * @param dependency 外部存储库依赖实体
     * @param artifact 依赖文档实体
     * @return [TypeSpec.Builder]
     */
    private fun TypeSpec.Builder.addLibraryClass(dependency: ExternalDependency, artifact: DependencyDocument) = apply {
        superclass(ExternalDependencyDelegate::class.java)
        artifact.versions().forEach { (alias, _) ->
            addField(
                FieldSpec.builder(ExternalDependencyDelegate::class.java, alias.camelcase(), Modifier.PRIVATE, Modifier.FINAL)
                    .addJavadoc("Create the \"$dependency\" version alias \"$alias\"")
                    .build()
            )
            addMethod(
                MethodSpec.methodBuilder("get${alias.uppercamelcase()}")
                    .addJavadoc("Resolve the \"$dependency\" version alias \"$alias\"")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .returns(ExternalDependencyDelegate::class.java)
                    .addStatement("return ${alias.camelcase()}")
                    .build()
            )
        }
    }

    /**
     * 向通用构造方法构建器描述类添加变量实例化语句
     * @param className 类名
     * @return [MethodSpec.Builder]
     */
    private fun MethodSpec.Builder.addSuccessiveStatement(className: String) =
        addStatement("${className.uncapitalized()} = new ${className.capitalized()}()")

    /**
     * 向通用构造方法构建器描述类添加依赖描述器 (父方法)
     * @param dependency 外部存储库依赖实体
     * @return [MethodSpec.Builder]
     */
    private fun MethodSpec.Builder.addLibrarySuper(dependency: ExternalDependency) =
        addStatement("super(\"${dependency.groupId}\", \"${dependency.artifactId}\", \"${dependency.version.deployed}\")")

    /**
     * 向通用构造方法构建器描述类添加依赖描述器
     * @param dependency 外部存储库依赖实体
     * @param artifact 依赖文档实体
     * @return [MethodSpec.Builder]
     */
    private fun MethodSpec.Builder.addLibraryStatement(dependency: ExternalDependency, artifact: DependencyDocument) = apply {
        artifact.versions().forEach { (alias, version) ->
            addStatement(
                "${alias.camelcase()} = new ${ExternalDependencyDelegate::class.java.simpleName}" +
                    "(\"${dependency.groupId}\", \"${dependency.artifactId}\", \"$version\")"
            )
        }
    }

    /**
     * 获取、创建通用构建器描述类
     * @param name 名称
     * @param accessors 接续对象
     * @return [TypeSpec.Builder]
     */
    private fun getOrCreateClassSpec(name: String, accessors: Any) =
        classSpecs[name] ?: createClassSpec(name, accessors).also { classSpecs[name] = it }

    /**
     * 获取、创建通用构造方法构建器描述类
     * @param name 名称
     * @return [MethodSpec.Builder]
     */
    private fun getOrCreateConstructorSpec(name: String) = constructorSpecs[name] ?: createConstructorSpec().also { constructorSpecs[name] = it }

    /**
     * 追加到通用构造方法构建器描述类
     * @param name 名称
     * @param statement 回调 [MethodSpec.Builder]
     */
    private inline fun appendToConstructorSpec(name: String, statement: MethodSpec.Builder.() -> Unit) {
        val codeBlock = getOrCreateConstructorSpec(name)?.build()?.code
        if (constructorSpecs.contains(name)) constructorSpecs.remove(name)
        getOrCreateConstructorSpec(name)?.apply(statement)?.addCode(codeBlock)
    }

    /**
     * 解析并生成所有类的构建器 (核心方法)
     *
     * 解析开始前需要确保已调用 [createTopClassSpec] 并调用一次 [clearGeneratedData] 防止数据混淆
     *
     * 解析完成后需要调用 [releaseParseTypeSpecNames] 完成解析
     * @param successiveName 连续的名称
     * @param dependency 外部存储库依赖实体
     * @param artifact 依赖文档实体
     */
    private fun parseTypeSpec(successiveName: String, dependency: ExternalDependency, artifact: DependencyDocument) {
        /**
         * 获取生成的依赖库连续名称重复次数
         * @return [Int]
         */
        fun String.duplicateGrandSuccessiveIndex() = lowercase().let { name ->
            if (grandSuccessiveDuplicateIndexs.contains(name)) {
                grandSuccessiveDuplicateIndexs[name] = (grandSuccessiveDuplicateIndexs[name] ?: 1) + 1
                grandSuccessiveDuplicateIndexs[name] ?: 2
            } else 2.also { grandSuccessiveDuplicateIndexs[name] = it }
        }

        /**
         * 解析 (拆分) 名称到数组
         *
         * 形如 "com.mytest" → "ComMytest" → "mytest"
         * @return [List]<[Triple]<[String], [String], [String]>>
         */
        fun String.parseSuccessiveNames(): List<Triple<String, String, String>> {
            var grandAcccessorsName = ""
            var grandSuccessiveName = ""
            val successiveNames = mutableListOf<Triple<String, String, String>>()
            val splitNames = splitToDependencyGenerateNames()
            splitNames.forEach { eachName ->
                val name = eachName.capitalize().toNonJavaName().firstNumberToLetter()
                grandAcccessorsName += if (grandAcccessorsName.isNotBlank()) ".$eachName" else eachName
                grandSuccessiveName += name
                if (grandSuccessiveNames.any { it != grandSuccessiveName && it.lowercase() == grandSuccessiveName.lowercase() })
                    grandSuccessiveName += duplicateGrandSuccessiveIndex().toString()
                grandSuccessiveNames.add(grandSuccessiveName)
                successiveNames.add(Triple(grandAcccessorsName, grandSuccessiveName, name))
            }; return successiveNames
        }
        val successiveNames = successiveName.parseSuccessiveNames()
        successiveNames.forEachIndexed { index, (accessorsName, className, methodName) ->
            val nextItem = successiveNames.getOrNull(index + 1)
            val nextAccessorsName = nextItem?.first ?: ""
            val nextClassName = nextItem?.second ?: ""
            val nextMethodName = nextItem?.third ?: ""
            val isPreLastIndex = index == successiveNames.lastIndex - 1
            val nextAccessors: Any = if (isPreLastIndex) dependency else nextAccessorsName
            if (index == successiveNames.lastIndex) {
                getOrCreateClassSpec(className, dependency)?.addLibraryClass(dependency, artifact)
                getOrCreateConstructorSpec(className)?.addLibraryStatement(dependency, artifact)
                preAddConstructorSpecSupers.add(className to dependency)
            } else {
                if (index == 0) noRepeated(TOP_SUCCESSIVE_NAME, methodName, className) {
                    getOrCreateClassSpec(TOP_SUCCESSIVE_NAME, accessorsName)
                        .addSuccessiveField(accessorsName, className)
                        .addSuccessiveMethod(accessorsName, methodName, className)
                    getOrCreateConstructorSpec(TOP_SUCCESSIVE_NAME)
                        .addSuccessiveStatement(className)
                    memoryExtensionClasses.add(methodName.uncapitalize() to createAccessorsName(className))
                }
                noRepeated(className, nextMethodName, nextClassName) {
                    getOrCreateClassSpec(className, accessorsName)
                        .addSuccessiveField(nextAccessors, nextClassName)
                        .addSuccessiveMethod(nextAccessors, nextMethodName, nextClassName)
                    preAddConstructorSpecNames.add(className to nextClassName)
                }
            }
        }
    }

    /** 完成生成所有类的构建器 (构造方法名称) (释放) */
    private fun releaseParseTypeSpecNames() =
        preAddConstructorSpecNames.onEach { (topClassName, innerClassName) ->
            getOrCreateConstructorSpec(topClassName)?.addSuccessiveStatement(innerClassName)
        }.clear()

    /** 完成生成所有类的构建器 (构造方法的父方法) (释放) */
    private fun releaseParseTypeSpecSupers() =
        preAddConstructorSpecSupers.onEach { (className, dependency) ->
            appendToConstructorSpec(className) { addLibrarySuper(dependency) }
        }.clear()

    /**
     * 解析并生成所有类的构建器
     * @return [TypeSpec]
     */
    private fun buildTypeSpec(): TypeSpec {
        classSpecs.forEach { (name, typeSpec) ->
            constructorSpecs[name]?.build()?.let { typeSpec.addMethod(it) }
            if (name != TOP_SUCCESSIVE_NAME) classSpecs[TOP_SUCCESSIVE_NAME]?.addType(typeSpec.build())
        }; return classSpecs[TOP_SUCCESSIVE_NAME]?.build() ?: SError.make("Merge accessors classes failed")
    }

    /** 创建首位构建器 */
    private fun createTopClassSpec() {
        classSpecs[TOP_SUCCESSIVE_NAME] = createClassSpec(TOP_CLASS_NAME, isInner = false)
        constructorSpecs[TOP_SUCCESSIVE_NAME] = createConstructorSpec()
    }

    /** 清空所有已生成的数据 */
    internal fun clearGeneratedData() {
        classSpecs.clear()
        constructorSpecs.clear()
        preAddConstructorSpecNames.clear()
        memoryExtensionClasses.clear()
        grandSuccessiveNames.clear()
        grandSuccessiveDuplicateIndexs.clear()
        usedSuccessiveTags.clear()
        librariesNamespace = ""
    }

    /**
     * 生成 [JavaFile]
     * @param namespace 依赖命名空间
     * @return [JavaFile]
     * @throws SweetDependencyUnresolvedException 如果生成失败
     */
    internal fun build(namespace: String) = runCatching {
        librariesNamespace = namespace
        clearGeneratedData()
        createTopClassSpec()
        Dependencies.libraries().forEach { (dependencyName, artifact) ->
            val dependency = ExternalDependency(dependencyName, artifact.version())
            parseTypeSpec(dependencyName.current, dependency, artifact)
            if (artifact.alias.isNotBlank()) parseTypeSpec(artifact.alias, dependency, artifact)
            releaseParseTypeSpecNames()
        }; releaseParseTypeSpecSupers(); buildTypeSpec().createJavaFile(ACCESSORS_PACKAGE_NAME)
    }.getOrElse { SError.make("Failed to generated accessors classes, please checking your config file", it) }

    /**
     * 获取参与编译的 Stub [JavaFile] 数组
     * @return [List]<[JavaFile]>
     */
    internal val compileStubFiles get(): List<JavaFile> {
        val stubFiles = mutableListOf<JavaFile>()
        val iExtensionAccessorsFile =
            TypeSpec.interfaceBuilder(IExtensionAccessors::class.java.simpleName)
                .addModifiers(Modifier.PUBLIC)
                .build().createJavaFile(IExtensionAccessors::class.java.packageName)
        val externalDependencyDelegateFile =
            TypeSpec.classBuilder(ExternalDependencyDelegate::class.java.simpleName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(
                    MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String::class.java, "groupId")
                        .addParameter(String::class.java, "artifactId")
                        .addParameter(String::class.java, "version")
                        .build()
                ).build().createJavaFile(ExternalDependencyDelegate::class.java.packageName)
        stubFiles.add(iExtensionAccessorsFile)
        stubFiles.add(externalDependencyDelegateFile)
        return stubFiles
    }

    /**
     * 获取扩展功能预置 [Class] 数组 (依赖)
     *
     * 需要调用 [build] 生成后才可以使用 - 否则可能会返回空数组
     * @return [List]<[Pair]<[String], [String]>>
     */
    internal val librariesClasses get() =
        if (librariesNamespace.isNotBlank())
            listOf(librariesNamespace to createAccessorsName())
        else memoryExtensionClasses
}