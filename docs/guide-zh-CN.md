# Sweet Dependency 使用文档

在开始使用之前，建议你仔细阅读此文档，以便你能更好地了解它的作用方式与功能。

你可以在项目的根目录找到 samples 中的 Demo，并参考此文档食用效果更佳。

## 工作原理

`SweetDependency` 通过配置文件定义的前置存储库作用于自身的同时应用到 Gradle 默认的项目存储库中。

它仅会将需要部署的依赖的名称和版本告知 Gradle，并不参与依赖的最终部署工作。

在完成上述工作后，Gradle 将使用 `SweetDependency` 设置的自定义存储库和依赖进行最终的部署。

> 工作流程示例

```
--- Sweet Dependency ---
⬇️ 读取配置文件
⬇️ 将存储库设置到 Gradle 当前项目
⬇️ 通过存储库自动装配当前定义的依赖
--- Gradle ---
⬇️ 得到项目存储库
⬇️ 得到待部署的依赖
✅ 通过存储库搜索所有依赖并部署
```

## 前提条件

请注意 `SweetDependency` 最低支持 Gradle `7.x.x`，并且使用 `pluginManagement` 和 `dependencyResolutionManagement` 新方式进行管理。

如果你的项目依然在使用 `buildscript` 的方式进行管理，请迁移到新方式，否则会发生错误。

如果你的项目不能使用 `dependencyResolutionManagement` 进行管理，你可以参考此页面最下方的 [自定义选项](#自定义选项)
通过配置 `isUseDependencyResolutionManagement = false` 来使用传统的库依赖管理方式。

## 快速开始

首先，打开你根项目的 `settings.gradle` 或 `settings.gradle.kts`。

删除整个 `dependencyResolutionManagement` 方法 (如果有)。

然后在你根项目的 `settings.gradle` 或 `settings.gradle.kts` 中加入如下代码。

如果已经存在 `pluginManagement` 则不需要重复添加。

你需要在 `pluginManagement.repositories` 添加所需的存储库 `mavenCentral` 以便使 Gradle 能够找到 `SweetDependency` 插件。

同时你需要保持其它存储库存在以便使 Gradle 能够完成自身插件的初始化。

> Kotlin DSL

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
plugins {
    id("com.highcapable.sweetdependency") version "<version>"
}
```

> Groovy DSL

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
plugins {
    id 'com.highcapable.sweetdependency' version '<version>'
}
```

请将上述代码中的 `<version>` 替换为
[Release](https://github.com/fankes/SweetDependency/releases) 中的最新版本， 请注意<u>**不要**</u>在后方加入 `apply false`。

上述配置完成后，运行一次 Gradle Sync。

**特别注意**

`SweetDependency` 会替换 `pluginManagement` 和 `dependencyResolutionManagement` 中设置的存储库，如果你手动在这些方法块中配置了存储库，它们都将会无效。

如果你想继续配置 `dependencyResolutionManagement` 中的其它内容，例如 `versionCatalogs`，它只能出现在 `plugins` 的下方。

我们不建议继续配置 `versionCatalogs`，因为 `SweetDependency` 借用了来自它的部分功能，可能会引发冲突。

不出意外的情况下，`SweetDependency` 会自动为你创建 YAML 配置文件，你将会得到如下项目结构 (以 Android 项目举例)。

```
MyApplication
 ├─ gradle
 │  └─ sweet-dependency
 │     └─ sweet-dependency-config.yaml <-- SweetDependency 配置文件
 ├─ build.gradle / build.gradle.kts
 ├─ settings.gradle / settings.gradle.kts
 ├─ app
 │  └─ build.gradle / build.gradle.kts
 └─ mylibrary
    └─ build.gradle / build.gradle.kts
```

然后，请打开 `sweet-dependency-config.yaml` 配置文件，对 `SweetDependency` 进行基础配置。

默认配置已经帮你自动生成，你可以按照你的需求进行配置。

如果你觉得手动配置很麻烦，没问题，你可以直接跳转到 [迁移依赖到 Sweet Dependency](#迁移依赖到-sweet-dependency) 进行阅读。

> 示例如下

```yaml
# 配置偏好设置
preferences:
  # Gradle Sync 时自动装配、更新依赖模式
  # 此选项决定了 Gradle Sync 时的行为
  # - UPDATE_OPTIONAL_DEPENDENCIES
  # ↑ 默认模式，自动装配和更新可选依赖
  # - UPDATE_ALL_DEPENDENCIES
  # ↑ 自动装配和更新所有依赖
  # - ONLY_AUTOWIRE_DEPENDENCIES
  # ↑ 仅自动装配使用 "+" 填充版本的依赖
  # - UPDATE_OPTIONAL_PLUGINS
  # ↑ 自动装配和更新可选依赖 (插件依赖)
  # - UPDATE_ALL_PLUGINS
  # ↑ 自动装配和更新所有依赖 (插件依赖)
  # - ONLY_AUTOWIRE_PLUGINS
  # ↑ 仅自动装配使用 "+" 填充版本的依赖 (插件依赖)
  # - UPDATE_OPTIONAL_LIBRARIES
  # ↑ 自动装配和更新可选依赖 (库依赖)
  # - UPDATE_ALL_LIBRARIES
  # ↑ 自动装配和更新所有依赖 (库依赖)
  # - ONLY_AUTOWIRE_LIBRARIES
  # ↑ 仅自动装配使用 "+" 填充版本的依赖 (库依赖)
  # - OFF
  # ↑ 什么也不做，关闭所有功能
  # 注意：不建议完全关闭此功能，如果存在未被自动装配的依赖将无法继续部署依赖
  autowire-on-sync-mode: UPDATE_OPTIONAL_DEPENDENCIES
  # 存储库装载模式
  # 目前 Gradle 提供了如下 3 种模式，具体模式可参考官方文档
  # - PREFER_PROJECT
  # - PREFER_SETTINGS
  # - FAIL_ON_PROJECT_REPOS
  repositories-mode: FAIL_ON_PROJECT_REPOS
  # 依赖命名空间
  # 设置后在部署依赖时需要加入命名空间作为前缀
  # 只允许 26 个英文字母 (大小写) 以及 '.'、'_'、'-' 且必须以字母开头 (长度至少为 3 位)
  # 例如我们有库依赖 "com.mydemo.test:test" 以 "implementation" 部署方式举例
  # 没有命名空间：implementation(com.mydemo.test.test)
  # 存在命名空间：implementation(libs.com.mydemo.test.test)
  dependencies-namespace:
    plugins:
      # 如果你希望关闭自动生成，可以设置为 false
      # 在决定关闭时请确保构建脚本中已不存在自动生成的代码，防止发生错误
      enable: true
      # 插件依赖必须存在命名空间，如果不设置，其默认为 "libs"
      name: libs
    libraries:
      # 如果你希望关闭自动生成，可以设置为 false
      # 在决定关闭时请确保构建脚本中已不存在自动生成的代码，防止发生错误
      enable: true
      # 库依赖的命名空间可选
      # 如果你不需要库依赖的命名空间，请删除此节点
      name: libs
  # 依赖版本过滤器
  # 如果你需要排除一些不希望被更新到的依赖版本 (例如测试版本) 可以手动进行配置
  # 默认情况下过滤器已帮你自动排除测试版本，无需对此项进行配置
  version-filter:
    # 使用内置过滤器
    # 默认为启用状态，内置过滤器包含了所有测试版本中可能出现的关键词
    # 其中包含："-beta"、"-alpha"、"-dev"、"-canary"、"-pre"、"-rc"、"-ga"、"-snapshot"
    # 可被匹配的版本例如："1.2.0-alpha01" 或 "1.1.2-beta01"
    # 如果禁用此选项，将仅使用 "exclusion-list" 中定义的关键词，如果 "exclusion-list" 为空，将禁用过滤器
    use-internal: true
    # 排除列表
    # 你可以在排除列表中填写需要排除的自定义关键词 (不区分大小写)
    # 推荐在开头添加 "-" 防止发生误判，例如："bar" 匹配 "1.0.0-bar01" 也会匹配 "1.0.0-foobar01"
    exclusion-list:
      -foo
      -bar

# 配置依赖使用的存储库
repositories:
  # 以下内容仅供示例，你只需要添加用到的存储库
  # 一般情况下只需要添加 google 和 maven-central，默认配置文件中将会自动帮你添加
  # 每个存储库都可以配置 url 和 path，这取决于存储库是否支持这种配置方式
  # 目前 SweetDependency 无法兼容 Maven 以外的自定义存储库
  # 下面这些节点名称为内置存储库，你不可以用这些名称作为自定义存储库
  google: # Google 存储库
  maven-central: # 中央存储库
  maven-local: # 本地存储库
    # 一般情况下不需要配置本地存储库的路径
    # 默认情况下会自动按照以下操作系统的默认路径获取
    # Windows: C:\Users\<User_Name>\.m2
    # Linux: /home/<User_Name>/.m2
    # Mac: /Users/<user_name>/.m2
    # 详情请参考 https://www.baeldung.com/maven-local-repository
    # 如果你修改了存储库的路径，请在这里重新指定
    # 如果你想保持默认配置，请删除此节点
    path: /path/to/repository
  gradle-plugin-portal: # Gradle 插件存储库
  # 以下列出目前 SweetDependency 内置的常见存储库别名
  # 中央存储库 (分流)
  maven-central-branch:
  # JitPack
  jit-pack:
  # 阿里云 Google 存储库镜像
  aliyun-google-mirror:
  # 阿里云中央存储库镜像
  aliyun-maven-central-mirror:
  # 阿里云公共存储库镜像
  aliyun-maven-public-mirror:
  # 阿里云 JCenter 镜像
  # 注意：JCenter 已经终止服务，不再建议使用
  aliyun-jcenter-mirror:
  # OSS 存储库
  sonatype-oss-releases:
  # 快照存储库
  sonatype-oss-snapshots:
  # 自定义 Maven 存储库
  # 自定义的存储库节点名称除内置存储库外可随意填写
  your-custom-repo:
    # 所有存储库添加即启用，如果你想禁用只需要添加此配置并设置为 false
    enable: true
    # 设置作用域
    # 此选项决定了此存储库将被作用于什么类型的依赖
    # - ALL
    # ↑ 默认模式，作用于所有类型依赖
    # - PLUGINS
    # ↑ 作用于插件依赖
    # - LIBRARIES
    # ↑ 作用于库依赖
    scope: ALL
    # 自定义内容过滤器
    # 此功能可以加快 Gradle 搜索依赖的速度
    # 如果已知此存储库仅包含某些依赖，就可以使用此功能
    content:
      # 指定需要包含的内容
      # 你可以以不同形式指定一个或一组内容
      include:
        # 此功能接受 1 个参数
        group:
          androidx.appcompat
          com.android
        # 此功能接受 1 个参数
        group-and-subgroups:
          androidx.appcompat
        # 此功能接受 1 个参数
        group-by-regex:
          androidx.*
          com.android.*
        # 此功能接受 2 个参数，使用 ":" 进行分割
        # 必须为 2 个参数，缺少参数会发生错误
        module:
          androidx.core:core
        # 此功能接受 2 个参数，使用 ":" 进行分割
        # 必须为 2 个参数，缺少参数会发生错误
        module-by-regex:
          androidx.core:*
        # 此功能接受 3 个参数，使用 ":" 进行分割
        # 必须为 3 个参数，缺少参数会发生错误
        version:
          androidx.core:core:1.9.0
        # 此功能接受 3 个参数，使用 ":" 进行分割
        # 必须为 3 个参数，缺少参数会发生错误
        version-by-regex:
          androidx.core:*:1.9.0
      # 指定需要排除的内容
      # 你可以以不同形式指定一个或一组内容
      exclude:
        group-by-regex:
          androidx.*
          com.android.*
        group-and-subgroups:
          androidx.appcompat
        module:
          androidx.core:core
        module-by-regex:
          androidx.core:*
        version:
          androidx.core:core:1.9.0
        version-by-regex:
          androidx.core:*:1.9.0
    # 自定义身份验证
    # 如果你的存储库需要身份验证才能访问，你可以添加此节点
    # 你可以使用 ${...} 的方式来引用当前项目或系统的键值内容
    # 具体用法你可以继续阅读文档最后的 "配置字符串插值" 部分
    credentials:
      # 你的用户名
      username: ${your-repo.username}
      # 你的密码
      password: ${your-repo.password}
    # 自定义存储库 URL
    url: https://to.your.custom/repo
    # 自定义存储库本地路径
    # 注意："url" 与 "path" 参数只能存在一个
    path: /path/to/repository

# 配置需要使用的插件依赖
plugins:
  # 注意：我们只推荐在这里定义一些外部存储库的插件依赖，一些内置于 Gradle 的插件不应该被定义在这里
  # 注意：插件依赖需要声明一个版本，不声明版本的依赖会发生问题且不被推荐，也不会生成自动装配代码
  # 插件 ID
  com.android.application:
    # 自定义依赖别名 (可选，在部署依赖时会用到)
    # 只允许 26 个英文字母 (大小写) 以及 '.'、'_'、'-' 且必须以字母开头 (长度至少为 3 位)
    # 别名可被 '.'、'_'、'-' 至少分割为 2 份，例如 "com-mytest"
    alias: android-application
    # 依赖版本 (如果你不确定版本，可以填写 "+" ，将会自动装配)
    version: 7.4.1
    # 自动装配、更新过程是否自动更新此依赖 (在版本为 "+" 的情况下设为 false 不会生效)
    auto-update: true
    # 依赖版本过滤器，默认情况下跟随全局配置
    version-filter:
      use-internal: true
      exclusion-list:
        -foo
        -bar
    # 指定当前依赖使用的存储库名称 (可以同时指定多个)
    # 在不设置此参数时，当前依赖将使用全部已定义的存储库按顺序依次进行搜索
    # 如果此依赖所在的存储库是已知的，推荐为其设置存储库以提升效率
    # 例如：google，你可以直接填写 google
    # 例如：maven-central，你可以直接填写 maven-central
    # 或是你指定的名称，例如上方的 jit-pack 节点，你需要填写 jit-pack
    # 注意：如果你对目标存储库设置了作用域且不匹配当前依赖类型，它将自动被排除
    # 注意：如果你设置的存储库中没有一个可以被使用，当前依赖将被认为不存在存储库
    repositories:
      google
      maven-central
  com.android.library:
    # 如果存在相同版本的依赖，可以使用版本引用来避免重复填写相同版本
    # 版本引用内容支持匹配依赖全称和依赖别名
    # 不可引用已经存在版本引用的依赖 (递归调用)
    # 注意："version" 和 "version-ref" 节点在一个依赖中只能出现一次
    # 注意：如果你声明了 "version-ref"，此依赖将在自动装配和更新中被排除
    # 注意：如果你声明了 "version-ref"，"auto-update"、"repositories"、"version-filter" 将无效
    version-ref: android-application # 或 "com.android.application"
  org.jetbrains.kotlin.android:
    alias: kotlin-android
    version: 1.8.10
  com.google.devtools.ksp:
    alias: kotlin-ksp
    version: 1.8.10-1.0.9

# 配置需要使用的库依赖
libraries:
  # Group ID
  androidx.core:
    # Artifact ID
    core:
      # 自定义依赖别名 (可选，在部署依赖时会用到)
      # 只允许 26 个英文字母 (大小写) 以及 '.'、'_'、'-' 且必须以字母开头 (长度至少为 3 位)
      # 别名可被 '.'、'_'、'-' 至少分割为 2 份，例如 "com-mytest"
      alias: androidx-core
      # 依赖版本 (如果你不确定版本，可以填写 "+" ，将会自动装配)
      version: 1.9.0
      # 自动装配、更新过程是否自动更新此依赖 (在版本为 "+" 的情况下设为 false 不会生效)
      auto-update: true
      # 依赖版本过滤器，默认情况下跟随全局配置
      version-filter:
        use-internal: true
        exclusion-list:
          -foo
          -bar
      # 指定当前依赖使用的存储库名称 (可以同时指定多个)
      # 在不设置此参数时，当前依赖将使用全部已定义的存储库按顺序依次进行搜索
      # 如果此依赖所在的存储库是已知的，推荐为其设置存储库以提升效率
      # 例如：google，你可以直接填写 google
      # 例如：maven-central，你可以直接填写 maven-central
      # 或是你指定的名称，例如上方的 jit-pack 节点，你需要填写 jit-pack
      # 注意：如果你对目标存储库设置了作用域且不匹配当前依赖类型，它将自动被排除
      # 注意：如果你设置的存储库中没有一个可以被使用，当前依赖将被认为不存在存储库
      repositories:
        google
        maven-central
    core-ktx:
      alias: androidx-core-ktx
      # 如果存在相同版本的依赖，可以使用版本引用来避免重复填写相同版本
      # 版本引用内容支持匹配依赖全称和依赖别名
      # 如果当前被引用的版本在当前 "Group ID" 中，可以直接填写 "<this>::Artifact ID"
      # 例如当前为 "androidx.core"，引用 "core" 的版本只需要填写为 "version-ref: <this>::core"
      # 不可引用已经存在版本引用的依赖 (递归调用)
      # 注意："version" 和 "version-ref" 节点在一个依赖中只能出现一次
      # 注意：如果你声明了 "version-ref"，此依赖将在自动装配和更新中被排除
      # 注意：如果你声明了 "version-ref"，"auto-update"、"repositories"、"version-filter" 将无效
      version-ref: <this>::core # 或 "androidx.core:core" 以及 "androidx-core" (别名)
  com.google.devtools.ksp:
    symbol-processing-api:
      # 如果你想引用一个不属于当前作用域 ("libraries") 的依赖或别名，你需要声明其所属的作用域
      # 例如引用 "plugins" 作用域中的依赖别名 "kotlin-ksp"，你需要使用 "<plugins>::" 作为开头进行声明
      # 同理，在 "plugins" 作用域中需要使用 "<libraries>::" 作为开头进行声明
      version-ref: <plugins>::kotlin-ksp # 或 "<plugins>::com.google.devtools.ksp"
  com.squareup.okhttp3:
    okhttp:
      # 如果你在版本中声明了一个 "version-filter" 中存在的版本 (内置过滤器或排除列表)
      # 例如版本 "5.0.0-alpha.7" 包含 "-alpha"
      # 此时你不需要配置 "version-filter" 并设置 "use-internal: false"
      # 运行自动装配和更新依赖时，它会自动更新到当前包含 "-alpha" 的最新版本
      version: 5.0.0-alpha.7
  com.google.android.material:
    material:
      alias: google-material-android
      version: 1.8.0
  junit:
    junit:
      alias: junit
      version: 4.13.2
  # 如果你正在使用一个 BOM 依赖，你可以像大多数依赖一样直接定义它
  org.springframework.boot:
    spring-boot-dependencies:
      alias: spring-boot-dependencies
      version: 1.5.8.RELEASE
  dom4j:
    dom4j:
      # 你可以使用 "<no-spec>" 声明此依赖不需要定义版本
      # 如果声明其不需要定义版本，它将自动使用 BOM 中定义的版本
      # 注意：如果你声明了 "<no-spec>"，此依赖将在自动装配和更新中被排除
      # 注意：如果你声明了 "<no-spec>"，"versions"、"version-ref" 将不能再使用
      version: <no-spec>
```

`SweetDependency` 接管了 Gradle 的依赖存储库，在配置文件中定义的存储库会同时被 `SweetDependency` 和 Gradle 使用。

上述配置完成后，运行一次 Gradle Sync。

然后，你可以前往你每个项目的 `build.gradle` 或 `build.gradle.kts`，将依赖的部署方式迁移到 `SweetDependency`。

`SweetDependency` 会将依赖命名空间、依赖名称、依赖别名等自动进行分割。

注意：如果你在 `pluginManagement` 的 `plugins` 方法块中设置了插件的版本，请将其移除。

> Kotlin DSL

首先，在根项目部署插件依赖，但是不应用 (与 Gradle 官方推荐做法一致)。

```kotlin
plugins {
    // 推荐使用 autowire 方法进行部署 (你也可以使用官方提供的 alias 方法，其行为一致)
    // 由于插件部分的自定义性限制，这里的代码生成借助了 Gradle 自身的 version catalogs
    // 由于 version catalogs 的要求，插件必须以 "命名空间.plugins" 开头
    autowire(libs.plugins.com.android.application) apply false
    autowire(libs.plugins.org.jetbrains.kotlin.android) apply false
    // 使用别名
    autowire(libs.plugins.android.application) apply false
    autowire(libs.plugins.kotlin.android) apply false
}
```

接下来，在子项目部署需要使用的插件依赖和库依赖。

```kotlin
plugins {
    autowire(libs.plugins.com.android.application)
    autowire(libs.plugins.org.jetbrains.kotlin.android)
    // 使用别名
    autowire(libs.plugins.android.application)
    autowire(libs.plugins.kotlin.android)
}

dependencies {
    // 直接部署
    implementation(androidx.core.core.ktx)
    implementation(com.google.android.material.material)
    // 使用别名
    implementation(androidx.core.ktx)
    implementation(google.material.android)
    // 如果你设置了依赖命名空间，请将命名空间作为前缀进行部署
    implementation(libs.androidx.core.core.ktx)
    // 使用依赖命名空间的情况下，依赖别名的用法依然相同
    implementation(libs.androidx.core.ktx)
}
```

> Groovy DSL

首先，在根项目部署插件依赖，但是不应用 (与 Gradle 官方推荐做法一致)。

```groovy
plugins {
    // Groovy 不支持使用 autowire 方法进行部署，你只能使用官方提供的 alias 方法
    // 由于插件部分的自定义性限制，这里的代码生成借助了 Gradle 自身的 version catalogs
    // 由于 version catalogs 的要求，插件必须以 "命名空间.plugins" 开头
    alias libs.plugins.com.android.application apply false
    alias libs.plugins.org.jetbrains.kotlin.android apply false
    // 使用别名
    alias libs.plugins.android.application apply false
    alias libs.plugins.kotlin.android apply false
}
```

接下来，在子项目部署需要使用的插件依赖和库依赖。

```groovy
plugins {
    alias libs.plugins.com.android.application
    alias libs.plugins.org.jetbrains.kotlin.android
    // 使用别名
    alias libs.plugins.android.application
    alias libs.plugins.kotlin.android
}

dependencies {
    // 直接部署
    implementation androidx.core.core.ktx
    implementation com.google.android.material.material
    // 使用别名
    implementation androidx.core.ktx
    implementation google.material.android
    // 如果你设置了依赖命名空间，请将命名空间作为前缀进行部署
    implementation libs.androidx.core.core.ktx
    // 使用依赖命名空间的情况下，依赖别名的用法依然相同
    implementation libs.androidx.core.ktx
}
```

**特别注意**

形如 `ext`、`extra`、`extraProperties`、`extensions` 的名称为 Gradle 在创建扩展方法时自带的默认扩展方法。

`SweetDependency` 在生成首位依赖扩展方法时如果遇到这些名称将不能正常生成，解决方案是在名称的结尾添加 `s`。

如果你一定要使用这些名称作为依赖的名称或别名，你可以考虑设置一个依赖命名空间。

目前在 Maven 存储库中尚未收集到以这些名称作为开头的依赖。

当然，你也不可以直接使用这些自带的默认扩展方法名称来设置依赖命名空间、依赖别名等。

**可能遇到的问题**

如果你的项目仅存在一个根项目，且没有导入任何子项目，此时如果 `dependencies`
方法体中的扩展方法不能正常生成，你可以将你的根项目迁移至子项目并在 `settings.gradle` 或 `settings.gradle.kts` 中导入这个子项目，这样即可解决此问题。

我们一般推荐将项目的功能进行分类，根项目仅用来管理插件和一些配置。

**局限性说明**

`SweetDependency` 无法管理 `settings.gradle` 或 `settings.gradle.kts` 中的 `plugins` 方法块，因为这属于 `SweetDependency` 的上游，这种情况请使用通常做法进行管理。

### Kotlin Multiplatform 支持

在 Kotlin Multiplatform 中与一般依赖部署方式一致。

> Kotlin DSL

```kotlin
sourceSets {
    val androidMain by getting {
        dependencies {
            implementation(androidx.core.core.ktx)
            implementation(com.google.android.material.material)
        }
    }
}
```

> Groovy DSL

```groovy
sourceSets {
    androidMain {
        dependencies {
            implementation androidx.core.core.ktx
            implementation com.google.android.material.material
        }
    }
}
```

## 迁移依赖到 Sweet Dependency

如果你是第一次开始使用 `SweetDependency`，你可以手动运行创建依赖迁移模板 Task。

你可以在根项目的 Task 分组 `sweet-dependency` 中找到 `createDependenciesMigrationTemplate` Task，手动运行它。

此操作将会自动分析所有项目中的外部存储库依赖，并在根项目的 `gradle/sweet-dependency` 生成以配置文件名为前缀的 `*.template.yaml` 文件。

如果你未修改过配置文件的名称，它将默认为 `sweet-dependency-config.template.yaml`。

如果生成的模版文件已经存在，则会被自动覆盖。

模板文件中会提供当前项目使用的依赖节点，请手动将生成的节点全部内容复制到配置文件中，并删除模板文件。

> 示例如下

```yaml
plugins:
  ...
libraries:
  ...
```

请注意，`SweetDependency` 不会在模版文件中创建依赖使用的存储库，请手动添加依赖使用的存储库或使用首次配置文件中自动生成的存储库。

然后，请手动将每个项目的 `build.gradle` 或 `build.gradle.kts` 的 `plugins` 和 `dependencies` 方法体中的外部存储库依赖迁移到 `SweetDependency`。

下面是一个示例，可供参考。

> Kotlin DSL

```kotlin
plugins {
    // 原始部署写法
    id("org.jetbrains.kotlin.android") version "1.8.10"
    // 迁移后的写法
    autowire(libs.plugins.org.jetbrains.kotlin.android)
}

dependencies {
    // 原始部署写法
    implementation("androidx.core:core-ktx:1.9.0")
    // 迁移后的写法
    implementation(androidx.core.core.ktx)
}
```

> Groovy DSL

```groovy
plugins {
    // 原始部署写法
    id 'org.jetbrains.kotlin.android' version '1.8.10'
    // 迁移后的写法
    alias libs.plugins.org.jetbrains.kotlin.android
}

dependencies {
    // 原始部署写法
    implementation 'androidx.core:core-ktx:1.9.0'
    // 迁移后的写法
    implementation androidx.core.core.ktx
}
```

如果你使用了 `versionCatalogs`，请将在 `settings.gradle` 或 `settings.gradle.kts` 中的定义也一并删除。

如果你使用 TOML 的方式定义了 `versionCatalogs`，例如 `libs.versions.toml` 文件，现在你可以不需要它了，你可以在迁移依赖后将其删除。

请注意，模版文件仅用于迁移依赖使用，它不应该出现在版本控制系统中，建议是使用后将其删除。

## 配置依赖自动装配

默认情况下，运行 Gradle Sync 即会执行搜索并自动装配、更新依赖。

依赖自动装配日志将会写入根项目的 `.gradle/sweet-dependency/dependencies-autowire.log` 中。

你可以在此文档的最下方找到配置是否启用依赖自动装配日志的方法。

你可以在 `sweet-dependency-config.yaml` 中配置 `preferences.autowire-on-sync-mode` 的模式。

你也可以在你需要的时候手动运行以下 Gradle Task，你可以在根项目的 Task 分组 `sweet-dependency` 中找到这些 Task。

- updateOptionalDependencies
- updateOptionalPlugins
- updateOptionalLibraries

自动装配和更新可选依赖。

其中以 "Plugins" 结尾的代表仅管理插件依赖，以 "Libraries" 结尾的代表仅管理库依赖。

你可以在配置文件中使用 `^` 标识可选更新的依赖。

> 示例如下

```yaml
plugins:
  # 插件依赖的可选更新行为与库依赖保持一致，请参考下方库依赖的示例
  ...

libraries:
  com.google.android.material:
    material:
      # 使用 "^" 作为开头标识当前版本，它将在有更新的时候被替换为最新版本
      # 使用 "^" 作为开头的版本将在下一次成功更新后移除此符号 (单次可选更新)
      # 如果你希望持续保留此符号 (常驻可选更新)，请双写它，形如 "^^"
      version: ^1.8.0
```

- updateAllDependencies
- updateAllPlugins
- updateAllLibraries

自动装配和更新所有依赖。

其中以 "Plugins" 结尾的代表仅管理插件依赖，以 "Libraries" 结尾的代表仅管理库依赖。

将会检查所有依赖的更新并更新到最新版本，依赖过多时可能会非常耗时。

- autowireDependencies
- autowirePlugins
- autowireLibraries

仅自动装配使用 "+" 填充版本的依赖。

其中以 "Plugins" 结尾的代表仅管理插件依赖，以 "Libraries" 结尾的代表仅管理库依赖。

**特别注意**

依赖自动装配或更新后，你需要手动运行一次 Gradle Sync 使更改生效，如果你不进行此操作，则更改将在下一次编译或任何 Gradle 活动时生效。

## 配置依赖扩展功能

你可以使用 `autowire(...)` 方法来自动装配任意依赖。

注意：一些特性可能不会适用于 Groovy DSL，如有需要，请开始使用或转移到 Kotlin DSL。

下面是一个简单的示例。

> Kotlin DSL

```kotlin
plugins {
    // 部署 "org.jetbrains.kotlin.android"
    autowire("org.jetbrains.kotlin.android")
    // 使用别名部署
    autowire("kotlin-android")
}

dependencies {
    // 部署 "androidx.core:core-ktx"
    implementation(autowire("androidx.core:core-ktx"))
    // 使用别名部署
    implementation(autowire("androidx-core-ktx"))
}
```

> Groovy DSL

```groovy
plugins {
    // 很遗憾，Gradle 不允许使用常规方案自定义 plugins 方法块
    // 这是 Gradle 对自定义插件的限制，插件无法干预
    // 所以，autowire 方法将不会支持 Groovy DSL
    // 如有需要，推荐开始使用或转换到 Kotlin DSL
}

dependencies {
    // 部署 "androidx.core:core-ktx"
    implementation sweet.autowire('androidx.core:core-ktx')
    // 使用别名部署
    implementation sweet.autowire('androidx-core-ktx')
}
```

除了自动装配插件依赖和外部存储库的依赖之外，你还可以用它来导入本地文件依赖。

> Kotlin DSL

```kotlin
dependencies {
    // 导入当前项目 libs 目录下的所有 jar 依赖
    implementation(autowire("libs/*.jar"))
    // 导入 mylibrary 项目 libs 目录下的所有 jar 依赖
    implementation(autowire("../mylibrary/libs/*.jar"))
    // 导入一个绝对路径目录下的所有 jar 依赖
    implementation(autowire("/home/test/someDepends/*.jar"))
    // 导入当前项目 libs 目录下的所有依赖，不区分文件扩展名
    implementation(autowire("libs/*"))
    // 你也可以一个一个或一组一组文件地导入
    implementation(
        autowire(
            "libs/*.jar",
            "libs/*.aar",
            "/home/test/someDepends/mylibrary-1.jar",
            "/home/test/someDepends/mylibrary-2.jar"
        )
    )
    // 以下是一个特殊情况
    // 如果你直接导入一个没有目录层次并相对于当前项目路径的文件，可能无法直接识别
    // 例如我们直接导入当前项目路径下的 "mylibrary.jar"
    // 以下情况会识别 "mylibrary.jar" 为一个外部存储库依赖
    implementation(autowire("mylibrary.jar"))
    // 要强调这个依赖是一个文件，请使用小括号将文件路径包起来
    implementation(autowire("(mylibrary.jar)"))
}
```

> Groovy DSL

```groovy
dependencies {
    // 导入当前项目 libs 目录下的所有 jar 依赖
    implementation sweet.autowire('libs/*.jar')
    // 导入 mylibrary 项目 libs 目录下的所有 jar 依赖
    implementation sweet.autowire('../mylibrary/libs/*.jar')
    // 导入一个绝对路径目录下的所有 jar 依赖
    implementation sweet.autowire('/home/test/someDepends/*.jar')
    // 导入当前项目 libs 目录下的所有依赖，不区分文件扩展名
    implementation sweet.autowire('libs/*')
    // 你也可以一个一个或一组一组文件地导入
    implementation sweet.autowire(
            'libs/*.jar',
            'libs/*.aar',
            '/home/test/someDepends/mylibrary-1.jar',
            '/home/test/someDepends/mylibrary-2.jar'
    )
    // 以下是一个特殊情况
    // 如果你直接导入一个没有目录层次并相对于当前项目路径的文件，可能无法直接识别
    // 例如我们直接导入当前项目路径下的 "mylibrary.jar"
    // 以下情况会识别 "mylibrary.jar" 为一个外部存储库依赖
    implementation sweet.autowire('mylibrary.jar')
    // 要强调这个依赖是一个文件，请使用小括号将文件路径包起来
    implementation sweet.autowire('(mylibrary.jar)')
}
```

### Kotlin Multiplatform 支持

在 Kotlin Multiplatform 中与一般依赖部署方式一致。

为了解决可能的插件冲突，无论是 Kotlin 还是 Groovy，都需要使用 `sweet.autowire`。

> Kotlin DSL

```kotlin
sourceSets {
    val androidMain by getting {
        dependencies {
            implementation(sweet.autowire("androidx.core:core-ktx"))
            implementation(sweet.autowire("libs/*.jar"))
        }
    }
}
```

> Groovy DSL

```groovy
sourceSets {
    androidMain {
        dependencies {
            implementation sweet.autowire('androidx.core:core-ktx')
            implementation sweet.autowire('libs/*.jar')
        }
    }
}
```

## 配置依赖版本定义

有一些依赖的版本我们希望在项目中固定它们，并不轻易被更新或改变。

针对这种情况，你可以使用版本定义在配置文件中来预先定义这些版本。

> 示例如下

```yaml
# 定义一些需要使用的版本
versions:
  # 节点名称只允许 26 个英文字母 (大小写) 以及 '.'、'_'、'-' 且必须以字母开头 (长度至少为 3 位)
  mydemo-test: 1.0.0

# 在插件依赖定义中引用这些版本
plugins:
  # 插件依赖的依赖版本定义行为与库依赖保持一致，请参考下方库依赖的示例
  ...

# 在库依赖定义中引用这些版本
libraries:
  com.mydemo.test:
    test:
      # 你可以直接在此节点引用被定义的版本
      # 版本定义名称的优先级高于依赖名称、别名，如果存在相同的名称则会优先使用前者
      version-ref: mydemo-test
```

## 配置依赖版本别名

默认情况下，在配置文件中声明的依赖版本是固定的，部署的依赖版本跟随定义中的版本。

如果你有这样的需求：相同依赖的版本需要和其它子项目或主项目中依赖的版本保持不一样。

> A 项目示例如下

```kotlin
plugins {
    id("com.mydemo.myplugin") version "1.0.1"
}

dependencies {
    implementation("com.mydemo.test:test:1.0.1")
}
```

> B 项目示例如下

```kotlin
plugins {
    id("com.mydemo.myplugin") version "1.0.2"
}

dependencies {
    implementation("com.mydemo.test:test:1.0.2")
}
```

针对这种情况，你可以使用版本别名在配置文件中来声明多个不同的版本。

> 示例如下

```yaml
plugins:
  com.mydemo.myplugin:
    alias: demo-myplugin
    # 依赖版本 (当前主版本，必须存在)
    # 你也可以使用 "version-ref"
    version: 1.0.2
    # 自定义版本别名
    # 只允许 26 个英文字母 (大小写) 以及 '.'、'_'、'-' 且必须以字母开头 (长度至少为 3 位)
    # 版本不可以填写 "+"，因为版本别名定义的版本不会被自动装配
    versions:
      a-version: 1.0.1
      # 如果你想跟随主版本，可以填写 "<latest>"
      b-version: <latest>

libraries:
  com.mydemo.test:
    test:
      alias: demo-test
      # 依赖版本 (当前主版本，必须存在)
      # 你也可以使用 "version-ref"
      version: 1.0.2
      # 自定义版本别名
      # 只允许 26 个英文字母 (大小写) 以及 '.'、'_'、'-' 且必须以字母开头 (长度至少为 3 位)
      # 版本不可以填写 "+"，因为版本别名定义的版本不会被自动装配
      versions:
        a-version: 1.0.1
        # 如果你想跟随主版本，可以填写 "<latest>"
        b-version: <latest>
```

然后你可以直接在当前依赖后使用 `.` 来使用它的版本别名。

版本别名会自动被转换为小驼峰形式，建议别名全部使用小写字母表示。

> A 项目示例如下

```kotlin
plugins {
    autowire(libs.plugins.com.mydemo.myplugin.aVersion)
    // 你也可以直接使用依赖别名
    autowire(libs.plugins.demo.myplugin.aVersion)
    // 或者使用 autowire 方法部署
    // 注意，你需要在 autowire 第二个方法参数中填写版本别名
    autowire("com.mydemo.myplugin", "a-version")
    autowire("demo-myplugin", "a-version")
}

dependencies {
    implementation(com.mydemo.test.test.aVersion)
    // 你也可以直接使用依赖别名
    implementation(demo.test.aVersion)
    // 或者使用 autowire 方法部署
    // 注意，你需要在 autowire 第二个方法参数中填写版本别名
    implementation(autowire("com.mydemo.test:test", "a-version"))
    implementation(autowire("demo-test", "a-version"))
}

```

> B 项目示例如下

```kotlin
plugins {
    autowire(libs.plugins.com.mydemo.myplugin.bVersion)
    // 你也可以直接使用依赖别名
    autowire(libs.plugins.demo.myplugin.bVersion)
    // 或者使用 autowire 方法部署
    // 注意，你需要在 autowire 第二个方法参数中填写版本别名
    autowire("com.mydemo.myplugin", "b-version")
    autowire("demo-myplugin", "b-version")
}

dependencies {
    implementation(com.mydemo.test.test.bVersion)
    // 你也可以直接使用依赖别名
    implementation(demo.test.bVersion)
    // 或者使用 autowire 方法部署
    // 注意，你需要在 autowire 第二个方法参数中填写版本别名
    implementation(autowire("com.mydemo.test:test", "b-version"))
    implementation(autowire("demo-test", "b-version"))
}
```

注意：一些特性在 Groovy DSL 中的 `plugins` 中可能会无法使用。

如果你不指定版本别名，部署的依赖默认会使用当前依赖的主版本 (即 "version" 定义的版本)。

**特别注意**

如果上述示例中 A 项目与 B 项目存在依赖关系，它将优先使用二者中的较新版本 (插件依赖除外)。

这是 Gradle 的依赖继承规则，不受版本别名控制。

不指定版本的依赖不能使用依赖版本别名功能。

## 配置字符串插值

你可以使用 ${...} 来动态向 `SweetDependency` 的配置文件中插入内容，这样你就可以从你的配置文件中导出一些敏感信息。

其中 `...` 代表当前使用的 KEY (键值名称)。

`SweetDependency` 会从以下位置按优先级依次查找需要插入的内容：

- 当前项目 (Root Project) 的 `gradle.properties`
- 当前用户的 `gradle.properties`
- 系统的 `System.getProperties()`
- 系统的 `System.getenv(...)`

> 示例如下

```yaml
# 配置依赖使用的存储库
repositories:
  your-custom-repo:
    credentials:
      username: ${your-repo.username}
      password: ${your-repo.password}
    url: ${your-repo.url}

# 配置需要使用的插件依赖
plugins:
  com.android.application:
    version: +

# 配置需要使用的库依赖
libraries:
  androidx.core:
    # 它还能被设置到节点上
    ${depends.androidx.core.name}:
      version: +
      versions:
        # 或是具体的内容上
        a-version: ${depends.androidx.core.core.a-version}
```

在读取配置文件时 `SweetDependency` 会优先将这些内容替换到实际的字符串再进行解析。

如果找不到当前使用的 KEY (键值名称) 对应的内容，将返回空字符串。

## 自动装配优化建议

现在，你已经了解了有关 `SweetDependency` 的基本功能，下面是针对现有项目存储库和依赖的一些优化建议。

### 存储库部分

根据 Gradle 的依赖搜索规则，存储库添加的顺序是有意义的，搜索顺序将按照你添加的顺序依次进行。

你可以适当地调整存储库的顺序，这将有助于提升依赖搜索效率。

> 示例如下

```yaml
repositories:
  google:
  maven-central:
```

针对中国大陆的用户，你可以采用 `SweetDependency` 为你预置的镜像服务器地址来加快依赖的搜索速度。

> 示例如下

```yaml
repositories:
  aliyun-google-mirror:
  aliyun-maven-central-mirror:
  aliyun-maven-public-mirror:
  aliyun-jcenter-mirror:
```

你还可以对 `google` 存储库设置 `content` 参数来提升其使用效率，因为它目前只包含以下开头的依赖。

- androidx.*
- com.google.*
- com.android.*

> 示例如下

```yaml
repositories:
  google:
    content:
      include:
        group-by-regex:
          androidx.*
          com.google.*
          com.android.*
```

你还可以把 `gradle-plugin-portal` 存储库的 `scope` 参数设置为 `PLUGINS` 来提升搜索效率，因为它只会被作用于插件依赖。

> 示例如下

```yaml
repositories:
  gradle-plugin-portal:
    scope: PLUGINS
```

推荐将 `gradle-plugin-portal` 排在所有存储库的第一位，插件依赖优先使用它进行搜索。

### 依赖部分

你可以给指定的依赖设置其使用的存储库以减少自动装配搜索的耗时。

你可以在适当的时候使用 `version-ref`，这可以减少重复搜索相同版本依赖的耗时。

> 示例如下

```yaml
plugins:
  com.android.application:
    alias: android-application
    version: 7.4.1
    repositories:
      google
  com.android.library:
    version-ref: android-application

libraries:
  androidx.core:
    core-ktx:
      version: 1.9.0
      repositories:
        google
```

## 写出调试信息

你可以手动运行 `sweetDependencyDebug` Task 来写出调试信息，你可以在根项目的 Task 分组 `sweet-dependency` 中找到它。

此操作将写出当前 `SweetDependency` 在内存中的数据结构到控制台，你可以参照配置文件检查此数据是否正确。

如果你认为 `SweetDependency` 不能按预期正常工作，你也可以将此数据提供给我们以供我们调试和修复。

## 自定义选项

你可以在根项目的 `settings.gradle` 或 `settings.gradle.kts` 中使用 `sweetDependency` lambda 方法来配置 `SweetDependency`。

> Kotlin DSL

```kotlin
sweetDependency {

    // 启用 SweetDependency，设置为 false 将禁用所有功能
    isEnable = true

    // SweetDependency 配置文件名称
    configFileName = "sweet-dependency-config.yaml"

    // 是否使用 Settings.dependencyResolutionManagement 管理库依赖
    // 此功能默认启用，如果你的项目必须存在自定义的 "repositories" 方法块，请关闭此功能
    // 注意：关闭后配置文件中的 "repositories-mode" 选项将不再有效
    isUseDependencyResolutionManagement = true

    // 是否启用依赖自动装配日志
    // 此功能默认启用，会在当前根项目 (Root Project) 的 ".gradle/sweet-dependency" 目录下创建日志文件
    isEnableDependenciesAutowireLog = true

    // 是否启用详细模式
    // 此功能默认启用，关闭后 SweetDependency 将会在非必要情况下保持安静 (省略非必要日志)
    isEnableVerboseMode = true
}
```

> Groovy DSL

```groovy
sweetDependency {
    enable true
    configFileName 'sweet-dependency-config.yaml'
    useDependencyResolutionManagement true
    enableDependenciesAutowireLog true
    enableVerboseMode true
}
```

## 问题反馈

如果你在使用 `SweetDependency` 的过程中遇到了任何问题，你都可以随时在 GitHub 开启一个 `issues` 向我们反馈。