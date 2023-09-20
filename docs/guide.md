# Sweet Dependency Documentation

Before you start using it, it is recommended that you read this document carefully so that you can better understand how it works and its functions.

You can find the demo in samples in the root directory of the project, and refer to this document for better use.

## Working Principle

`SweetDependency` acts on itself through the pre-repositories declared in the configuration file and applies to Gradle's default project repositories.

It only informs Gradle of the name and version of the dependencies that need to be deployed, and does not participate in the final deployment of
dependencies.

After the above work is done, Gradle will use the custom repositories and dependencies set by `SweetDependency` for the final deployment.

> Workflow following example

```
--- Sweet Dependency ---
⬇️ Read configuration file
⬇️ Set repositories to Gradle current project
⬇️ Autowire currently declared dependencies via repositories
--- Gradle ---
⬇️ Get project repositories
⬇️ Get the dependencies to be deployed
✅ Search all dependencies through the repositories and deploy
```

## Prerequisites

Note that `SweetDependency` supports at least Gradle `7.x.x` and is managed using the new `pluginManagement` and `dependencyResolutionManagement`.

If your project is still managed using the `buildscript` method, please migrate to the new method, otherwise errors will occur.

## Quick Start

First, open `settings.gradle` or `settings.gradle.kts` of your root project.

Remove the entire `dependencyResolutionManagement` method (if any).

Then add the following code in `settings.gradle` or `settings.gradle.kts` of your root project.

If `pluginManagement` already exists, there is no need to add it repeatedly.

You need to add the required repository `mavenCentral` in `pluginManagement.repositories` for Gradle to be able to find the `SweetDependency` plugin.

At the same time you need to keep other repositories exist so that Gradle can complete the initialization of its own plugins.

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

Please replace `<version>` in the above code with the latest version in
[Release](https://github.com/fankes/SweetDependency/releases), please note that <u>**DO NOT**</u> add `apply false` after it.

After the above configuration is complete, run Gradle Sync once.

**Possible Problems**

If running Gradle Sync fails with the error `Cannot have abstract method KotlinTarget. withSourcesJar()`,
this may be a problem with the Kotlin plugin version of your current project.

This problem is an error caused by upgrading the Kotlin plugin from `1.8.0+` → `1.9.0+`.

The solution is to modify the version of the Kotlin plugin to `1.9.0+`.

**Pay Attention**

`SweetDependency` will replace the repositories set in `pluginManagement` and `dependencyResolutionManagement`,
if you manually configure repositories in these method blocks, they will have no effect.

If you want to continue to configure other content in `dependencyResolutionManagement`, such as `versionCatalogs`, it can only appear under `plugins`.

We do not recommend continuing to configure `versionCatalogs`, because `SweetDependency` used some functions from it,
which may cause conflicts.

Without exception, `SweetDependency` will automatically create a YAML configuration file for you, and you will get the following project structure
(take Android project as an example).

```
MyApplication
 ├─ gradle
 │  └─ sweet-dependency
 │     └─ sweet-dependency-config.yaml <-- SweetDependency config file
 ├─ build.gradle / build.gradle.kts
 ├─ settings.gradle / settings.gradle.kts
 ├─ app
 │  └─ build.gradle / build.gradle.kts
 └─ mylibrary
    └─ build.gradle / build.gradle.kts
```

Then, please open the `sweet-dependency-config.yaml` configuration file to configure the basic configuration of `SweetDependency`.

The default configuration has been automatically generated for you, and you can configure it according to your needs.

If you think manual configuration is cumbersome, no problem, you can jump directly
to [Migrating Dependencies to Sweet Dependency](#migrating-dependencies-to-sweet-dependency) for reading.

> The following example

```yaml
# Configure preferences
preferences:
  # Autowire and update dependency mode when Gradle Sync
  # This option determines the behavior of Gradle Sync
  # - UPDATE_OPTIONAL_DEPENDENCIES
  # ↑ Default mode, autowire and update optional dependencies
  # - UPDATE_ALL_DEPENDENCIES
  # ↑ Autowire and update all dependencies
  # - ONLY_AUTOWIRE_DEPENDENCIES
  # ↑ Autowire only dependencies fill in "+" versions
  # - UPDATE_OPTIONAL_PLUGINS
  # ↑ Autowire and update optional dependencies (plugin dependencies)
  # - UPDATE_ALL_PLUGINS
  # ↑ Autowire and update all dependencies (plugin dependencies)
  # - ONLY_AUTOWIRE_PLUGINS
  # ↑ Autowire only dependencies fill in "+" versions (plugin dependencies)
  # - UPDATE_OPTIONAL_LIBRARIES
  # ↑ Autowire and update optional dependencies (library dependencies)
  # - UPDATE_ALL_LIBRARIES
  # ↑ Autowire and update all dependencies (library dependencies)
  # - ONLY_AUTOWIRE_LIBRARIES
  # ↑ Autowire only dependencies fill in "+" versions (library dependencies)
  # - OFF
  # ↑ Do nothing, turn off all functions
  # Note: It is not recommended to turn off this function completely
  # If there are dependencies that are not autowired, you will not be able to continue to deploy dependencies
  autowire-on-sync-mode: UPDATE_OPTIONAL_DEPENDENCIES
  # Repositories setup mode
  # At present, Gradle provides the following 3 modes, the specific mode can refer to the official document
  # - PREFER_PROJECT
  # - PREFER_SETTINGS
  # - FAIL_ON_PROJECT_REPOS
  repositories-mode: FAIL_ON_PROJECT_REPOS
  # Dependencies namespace
  # After setting, you need to add the namespace as a prefix when deploying dependencies
  # Only allow 26 letters (upper and lower case) and '.', '_', '-' and must start with a letter
  # For example, we have a library dependency "com.mydemo.test:test" and use the "implementation" deployment method as an example
  # No namespace: implementation(com.mydemo.test.test)
  # Has namespace: implementation(libs.com.mydemo.test.test)
  dependencies-namespace:
    # Plugin dependencies namespace must exist, if not set, the default is "libs"
    plugins: libs
    # Library dependencies namespace is optional
    # If you don't need the namespace of library dependencies, delete this node
    libraries: libs
  # Dependencies version filter
  # If you need to exclude some dependency versions that you don't want to be updated to (such as test versions),
  # you can configure them manually
  # By default, the filter has automatically excluded the test version for you, no need to configure this
  version-filter:
    # Use built-in filters
    # The default is enabled, and the internal filter contains keywords that may appear in all test versions
    # Contains: "-beta", "-alpha", "-dev", "-canary", "-pre", "-rc", "-ga", "-snapshot"
    # Versions that can be matched eg: "1.2.0-alpha01" or "1.1.2-beta01"
    # If this option is disabled, only the keywords declared in "exclusion-list" will be used,
    # if "exclusion-list" is empty, the filter will be disabled
    use-internal: true
    # Exclusion list
    # You can fill in the custom keywords that need to be excluded in the exclusion list (case-insensitive)
    # It is recommended to add "-" at the beginning to prevent misjudgment,
    # for example: "bar" matches "1.0.0-bar01" and also matches "1.0.0-foobar01"
    exclusion-list:
      -foo
      -bar

# Configure repositories used by dependencies
repositories:
  # The following content is just for example, you only need to add the repositories used
  # Generally, you only need to add google and maven-central,
  # and the default configuration file will automatically add them for you
  # Each repository can configure url and path, depending on whether the repository supports this configuration method
  # Currently SweetDependency is not compatible with custom repositories other than Maven
  # The following node names are built-in repositories, you cannot use these names as custom repositories
  google: # Google repository
  maven-central: # Maven Central repository
  maven-local: # Maven Local repository
    # Generally, there is no need to configure the path of the local repository
    # By default, it will be automatically obtained according to the default path of the following operating systems
    # Windows: C:\Users\<User_Name>\.m2
    # Linux: /home/<User_Name>/.m2
    # Mac: /Users/<user_name>/.m2
    # For details, please refer to https://www.baeldung.com/maven-local-repository
    # If you modified the path of the repository, please re-specify here
    # If you want to keep the default configuration, please delete this node
    path: /path/to/repository
  gradle-plugin-portal: # Gradle plugin repository
  # The following lists common repository aliases currently built into SweetDependency
  # Maven Central repository (branch)
  maven-central-branch:
  # JitPack
  jit-pack:
  # Alibaba Cloud Google repository mirror
  # For Mainland China
  aliyun-google-mirror:
  # Alibaba Cloud Maven Central repository mirror
  # For Mainland China
  aliyun-maven-central-mirror:
  # Alibaba Cloud public repository mirror
  # For Mainland China
  aliyun-maven-public-mirror:
  # Alibaba Cloud JCenter mirror
  # Note: JCenter has shut down and is no longer recommended
  aliyun-jcenter-mirror:
  # OSS repository
  sonatype-oss-releases:
  # Snapshot repository
  sonatype-oss-snapshots:
  # Custom Maven repository
  # The custom repository node name can be filled in freely except for the built-in repository
  your-custom-repo:
    # All repositories are added and enabled, if you want to disable just add this configuration and set it to false
    enable: true
    # Set scope
    # This option determines what type of dependencies this repository will be used for
    # - ALL
    # ↑ Default mode, which applies to all types of dependencies
    # - PLUGINS
    # ↑ Act on plugins
    # - LIBRARIES
    # ↑ Act on libraries
    scope: ALL
    # Custom content filter
    # This feature can speed up Gradle's search for dependencies
    # This can be used if this repository is known to contain only certain dependencies
    content:
      # Specify what needs to be included
      # You can specify one or a group of content in different forms
      include:
        # This function accepts 1 argument
        group:
          androidx.appcompat
          com.android
        # This function accepts 1 argument
        group-and-subgroups:
          androidx.appcompat
        # This function accepts 1 argument
        group-by-regex:
          androidx.*
          com.android.*
        # This function accepts 2 arguments, separated by ":"
        # Must be 2 parameters, missing arguments will cause an error
        module:
          androidx.core:core
        # This function accepts 2 arguments, separated by ":"
        # Must be 2 parameters, missing arguments will cause an error
        module-by-regex:
          androidx.core:*
        # This function accepts 3 arguments, separated by ":"
        # Must be 3 parameters, missing arguments will cause an error
        version:
          androidx.core:core:1.9.0
        # This function accepts 3 arguments, separated by ":"
        # Must be 3 parameters, missing arguments will cause an error
        version-by-regex:
          androidx.core:*:1.9.0
      # Specify what needs to be excluded
      # You can specify one or a group of content in different forms
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
    # Custom authentication
    # If your repository requires authentication to access, you can add this node
    # You can use ${...} to refer to the key-value content of the current project or system
    # For specific usage, you can continue to read the "Configure String Interpolation" section at the end of the document
    credentials:
      # Your username
      username: ${your-repo.username}
      # Your password
      password: ${your-repo.password}
    # Custom repository URL
    url: https://to.your.custom/repo
    # Custom repository local path
    # Note: There can only be one "url" and "path" parameter
    path: /path/to/repository

# Configure plugins that need to be used
plugins:
  # Note: We only recommend declared some external repositories plugins here, some built-in Gradle plugins should not be declared here
  # Note: Plugins need to declare a version, dependencies that do not declare versions will cause problems and are not recommended,
  # and will not generate autowire code
  # Plugin ID
  com.android.application:
    # Custom dependency alias (optional, will be used when deploying dependencies)
    # Only allow 26 letters (upper and lower case) and '.', '_', '-' and must start with a letter
    # (minimum 3 digits in length)
    # The alias can be split into at least 2 parts by '.', '_', '-', for example "com-mytest"
    alias: android-application
    # Dependency version (if you are not sure about the version, you can fill in "+" and it will be autowiring)
    version: 7.4.1
    # Whether the autowiring, update process will automatically update this dependency (fill in false when version is "+" has no effect)
    auto-update: true
    # Dependencies version filter, follow the global configuration by default
    version-filter:
      use-internal: true
      exclusion-list:
        -foo
        -bar
    # Specify the name of the repositories currently used by dependencies (multiple can be specified at the same time)
    # When this parameter is not set, the current dependency will use all declared repositories to search in order
    # If repositories where this dependency is located is known, it is recommended to set the repositories for it to improve efficiency
    # For example: google, you can directly fill in google
    # For example: maven-central, you can directly fill in maven-central
    # Or the name you specified, such as the jit-pack node above, you need to fill in jit-pack
    # Note: If you set a scope on the target repository and it does not match the current dependency type, it will be automatically excluded
    # Note: If none of the repositories you set are available, the current dependency will be considered as non-existing repositories
    repositories:
      google
      maven-central
  com.android.library:
    # If there are dependencies of the same version,
    # you can use the version reference to avoid filling in the same version repeatedly
    # The version reference content supports matching the full name of the dependency
    # and the alias of the dependency
    # Cannot reference dependencies that already exist version references (recursive call)
    # Note: "version" and "version-ref" nodes can only appear once in a dependency
    # Note: If you declare "version-ref", this dependency will be excluded from autowiring and updating
    # Note: If you declare "version-ref", "auto-update", "repositories", "version-filter" will not work
    version-ref: android-application # Or "com.android.application"
  org.jetbrains.kotlin.android:
    alias: kotlin-android
    version: 1.8.10

# Configure libraries that need to be used
libraries:
  # Group ID
  androidx.core:
    # Artifact ID
    core:
      # Custom dependency alias (optional, will be used when deploying dependencies)
      # Only allow 26 letters (upper and lower case) and '.', '_', '-' and must start with a letter
      # (minimum 3 digits in length)
      # The alias can be split into at least 2 parts by '.', '_', '-', for example "com-mytest"
      alias: androidx-core
      # Dependency version (if you are not sure about the version, you can fill in "+" and it will be autowiring)
      version: 1.9.0
      # Whether the autowiring, update process will automatically update this dependency (fill in false when version is "+" has no effect)
      auto-update: true
      # Dependencies version filter, follow the global configuration by default
      version-filter:
        use-internal: true
        exclusion-list:
          -foo
          -bar
      # Specify the name of the repositories currently used by dependencies (multiple can be specified at the same time)
      # When this parameter is not set, the current dependency will use all declared repositories to search in order
      # If repositories where this dependency is located is known, it is recommended to set the repositories for it to improve efficiency
      # For example: google, you can directly fill in google
      # For example: maven-central, you can directly fill in maven-central
      # Or the name you specified, such as the jit-pack node above, you need to fill in jit-pack
      # Note: If you set a scope on the target repository and it does not match the current dependency type, it will be automatically excluded
      # Note: If none of the repositories you set are available, the current dependency will be considered as non-existing repositories
      repositories:
        google
        maven-central
    core-ktx:
      alias: androidx-core-ktx
      # If there are dependencies of the same version,
      # you can use the version reference to avoid filling in the same version repeatedly
      # The version reference content supports matching the full name of the dependency
      # and the alias of the dependency
      # If the currently referenced version is in the current "Group ID",
      # you can directly fill in the "<this>::Artifact ID"
      # For example, it is currently "androidx.core",
      # and the version that refers to "core" only needs to be filled in as "version-ref: <this>::core"
      # Cannot reference dependencies that already exist version references (recursive call)
      # Note: "version" and "version-ref" nodes can only appear once in a dependency
      # Note: If you declare "version-ref", this dependency will be excluded from autowiring and updating
      # Note: If you declare "version-ref", "auto-update", "repositories", "version-filter" will not work
      version-ref: <this>::core # Or "androidx.core:core" and "androidx-core" (alias)
  com.squareup.okhttp3:
    okhttp:
      # If you declare a version in the version that exists in "version-filter" (internal filter or exclude list)
      # For example version "5.0.0-alpha.7" contains "-alpha"
      # At this point you don't need to configure "version-filter" and set "use-internal: false"
      # When running autowiring and updating dependencies,
      # it will automatically update to the latest version that currently contains "-alpha"
      version: 5.0.0-alpha.7
  com.google.android.material:
    material:
      alias: google-material-android
      version: 1.8.0
  junit:
    junit:
      alias: junit
      version: 4.13.2
  # If you are using a BOM dependency, you can declare it directly like most dependencies
  org.springframework.boot:
    spring-boot-dependencies:
      alias: spring-boot-dependencies
      version: 1.5.8.RELEASE
  dom4j:
    dom4j:
      # You can use "<no-spec>" to declare that this dependency does not need to declare version
      # If it is declared that it does not need to declare a version,
      # it will automatically use the version declared in the BOM
      # Note: If you declare "<no-spec>", this dependency will be excluded from autowiring and updating
      # Note: If you declare "<no-spec>", "versions", "version-ref" will no longer be used
      version: <no-spec>
```

`SweetDependency` takes over Gradle's dependency repositories, and the repositories declared in the config file will be used by both `SweetDependency`
and Gradle.

After the above configuration is complete, run Gradle Sync once.

Then, you can go to your project's `build.gradle` or `build.gradle.kts`, and migrate the dependency deployment method to `SweetDependency`.

`SweetDependency` will automatically split the dependencies namespace, dependencies name, dependencies alias, etc.

Note: If you set the plugins version in the `plugins` method block of `pluginManagement`, please remove it.

> Kotlin DSL

First, deploy plugins in root project, but not applied (consistent with the official Gradle recommendation).

```kotlin
plugins {
    // It is recommended to use the autowire method for deployment
    // (you can also use the official alias method, which behaves the same)
    // Due to the customization limitations of the plugins,
    // the code generation here uses Gradle's own version catalogs
    // Due to version catalogs, plugins must start with "namespace.plugins"
    autowire(libs.plugins.com.android.application) apply false
    autowire(libs.plugins.org.jetbrains.kotlin.android) apply false
    // Use an alias
    autowire(libs.plugins.android.application) apply false
    autowire(libs.plugins.kotlin.android) apply false
}
```

Next, deploy plugins and libraries that need to be used in the sub-projects.

```kotlin
plugins {
    autowire(libs.plugins.com.android.application)
    autowire(libs.plugins.org.jetbrains.kotlin.android)
    // Use an alias
    autowire(libs.plugins.android.application)
    autowire(libs.plugins.kotlin.android)
}

dependencies {
    // Direct deployment
    implementation(androidx.core.core.ktx)
    implementation(com.google.android.material.material)
    // Use an alias
    implementation(androidx.core.ktx)
    implementation(google.material.android)
    // If you set up a dependenies namespace, please deploy the namespace as a prefix
    implementation(libs.androidx.core.core.ktx)
    // In the case of using dependencies namespace, the usage of dependencies aliase is still the same
    implementation(libs.androidx.core.ktx)
}
```

> Groovy DSL

First, deploy plugins in root project, but not applied (consistent with the official Gradle recommendation).

```groovy
plugins {
    // Groovy does not support deployment using the autowire method,
    // you can only use the official alias method
    // Due to the customization limitations of the plugins,
    // the code generation here uses Gradle's own version catalogs
    // Due to version catalogs, plugins must start with "namespace.plugins"
    alias libs.plugins.com.android.application apply false
    alias libs.plugins.org.jetbrains.kotlin.android apply false
    // Use an alias
    alias libs.plugins.android.application apply false
    alias libs.plugins.kotlin.android apply false
}
```

Next, deploy plugins and libraries that need to be used in the sub-projects.

```groovy
plugins {
    alias libs.plugins.com.android.application
    alias libs.plugins.org.jetbrains.kotlin.android
    // Use an alias
    alias libs.plugins.android.application
    alias libs.plugins.kotlin.android
}

dependencies {
    // Direct deployment
    implementation androidx.core.core.ktx
    implementation com.google.android.material.material
    // Use an alias
    implementation androidx.core.ktx
    implementation google.material.android
    // If you set up a dependenies namespace, please deploy the namespace as a prefix
    implementation libs.androidx.core.core.ktx
    // In the case of using dependencies namespace, the usage of dependencies aliase is still the same
    implementation libs.androidx.core.ktx
}
```

**Pay Attention**

Names such as `ext`, `extra`, `extraProperties`, and `extensions` are the default extension methods that come with Gradle when creating extension
methods.

When `SweetDependency` encounters these names when generating the first dependency extension method, it will not be generated normally.

The solution is to add `s` at the end of the name.

If you must use these names as dependencies name or alias, you may consider set a dependencies namespace.

Dependencies starting with these names are not currently collected in the Maven repository.

Certainly, you cannot directly use these built-in default extension method names to set dependencies namespace, dependencies aliase, etc.

**Possible Problems**

If your project only has one root project and does not import any sub-projects,
if extension methods in `dependencies` are not generated properly,
you can solve this problem by migrating your root project to a sub-projects and importing this sub-projects in `settings.gradle`
or `settings.gradle.kts`.

We generally recommend classifying the functions of the project, and the root project is only used to manage plugins and some configurations.

**Limitations Note**

`SweetDependency` cannot manage the `plugins` method block in `settings.gradle` or `settings.gradle.kts`,
because this belongs to the upstream of `SweetDependency`, please use the usual way to manage this situation.

### Kotlin Multiplatform Support

In Kotlin Multiplatform, it is consistent with the general dependency deployment method.

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

## Migrating Dependencies to Sweet Dependency

If you are starting to use `SweetDependency` for the first time, you can manually run the create dependencies migration template task.

You can find the `createDependenciesMigrationTemplate` task in the task group `sweet-dependency` of the root project, and run it manually.

This operation will automatically analyze all external repository dependencies in the projects and generate the `*.template.yaml` file prefixed with
the configuration file name in the root project's `gradle/sweet-dependency`.

If you haven't changed the name of the configuration file, it will default to `sweet-dependency-config.template.yaml`.

If the generated template file already exists, it will be overwritten automatically.

The template file will provide the dependencies nodes used by the current projects.

Please manually copy all the content of the generated nodes to the configuration file and delete the template file.

> The following example

```yaml
plugins:
  ...
libraries:
  ...
```

Please note that `SweetDependency` will not create a dependency repositories in the template file, please add the dependency repositories manually or
use the automatically generated repositories in the first configuration file.

Then please manually migrate external repository dependencies to `SweetDependency` in the method body of `plugins` and `dependencies` of each
project's `build.gradle` or `build.gradle.kts`.

Below is an example for reference.

> Kotlin DSL

```kotlin
plugins {
    // Original deployment writing method
    id("org.jetbrains.kotlin.android") version "1.8.10"
    // Writing after migration
    autowire(libs.plugins.org.jetbrains.kotlin.android)
}

dependencies {
    // Original deployment writing method
    implementation("androidx.core:core-ktx:1.9.0")
    // Writing after migration
    implementation(androidx.core.core.ktx)
}
```

> Groovy DSL

```groovy
plugins {
    // Original deployment writing method
    id 'org.jetbrains.kotlin.android' version '1.8.10'
    // Writing after migration
    alias libs.plugins.org.jetbrains.kotlin.android
}

dependencies {
    // Original deployment writing method
    implementation 'androidx.core:core-ktx:1.9.0'
    // Writing after migration
    implementation androidx.core.core.ktx
}
```

If you are using `versionCatalogs`, please delete declared them in `settings.gradle` or `settings.gradle.kts` too.

If you declared `versionCatalogs` using TOML, such as a `libs.versions.toml` file, you don't need it now,
and you can delete it after migrating dependencies.

Please note that the template file is only used for migration dependencies, it should not appear in the version control system, it is recommended to
delete it after use.

## Configure Dependencies Autowiring

By default, running Gradle Sync will perform a search and autowire and update dependencies.

Dependencies autowire logs will be written to `.gradle/sweet-dependency/dependencies-autowire.log` of the root project.

You can find the method to configure whether to enable dependency autowiring logging at the bottom of this document.

You can configure the mode of `preferences.autowire-on-sync-mode` in `sweet-dependency-config.yaml`.

You can also manually run the following Gradle tasks when you need, you can find these tasks in the root project's task group `sweet-dependency`.

- updateOptionalDependencies
- updateOptionalPlugins
- updateOptionalLibraries

Autowire and update optional dependencies.

The ones ending with "Plugins" only manage plugins, and the ones ending with "Libraries" only manage libraries.

You can use `^` in the configuration file to identify optional update dependencies.

> The following example

```yaml
plugins:
  # The optional update behavior of plugins is consistent with libraries,
  # please refer to the example of libraries below
  ...

libraries:
  com.google.android.material:
    material:
      # Use "^" as the beginning to identify the current version,
      # it will be replaced with the latest version when there is an update
      # Use "^" as the beginning to identify the current version will remove this symbol
      # after the next successful update (single optional update)
      # If you want to keep this symbol persistent (permanent optional update),
      # please double write it, like "^^"
      version: ^1.8.0
```

- updateAllDependencies
- updateAllPlugins
- updateAllLibraries

Autowire and update all dependencies.

The ones ending with "Plugins" only manage plugins, and the ones ending with "Libraries" only manage libraries.

All dependencies will be checked for updates and updated to the latest version,
which can be very time-consuming when there are too many dependencies.

- autowireDependencies
- autowirePlugins
- autowireLibraries

Only autowire fill in "+" version dependencies.

The ones ending with "Plugins" only manage plugins, and the ones ending with "Libraries" only manage libraries.

**Pay Attention**

After dependencies are autowired or updated, you need to manually run Gradle Sync for the changes to take effect,
if you don't do this, the changes will take effect on the next compile or any Gradle activity.

## Configure Dependencies Extensions

You can autowire arbitrary dependencies using the `autowire(...)` method.

Note: Some features may not work with Groovy DSL, please start using or move to Kotlin DSL if needed.

Below is a simple example.

> Kotlin DSL

```kotlin
plugins {
    // Deployment "org.jetbrains.kotlin.android"
    autowire("org.jetbrains.kotlin.android")
    // Deploy with an alias
    autowire("kotlin-android")
}

dependencies {
    // Deployment "androidx.core:core-ktx"
    implementation(autowire("androidx.core:core-ktx"))
    // Deploy with an alias
    implementation(autowire("androidx-core-ktx"))
}
```

> Groovy DSL

```groovy
plugins {
    // Unfortunately, Gradle does not allow custom plugins method blocks using the usual scheme
    // This is Gradle's restriction on custom plugins, and plugins cannot intervene
    // Therefore, the autowire method will not support Groovy DSL
    // Recommended to start using or switch to Kotlin DSL if needed
}

dependencies {
    // Deployment "androidx.core:core-ktx"
    implementation sweet.autowire('androidx.core:core-ktx')
    // Deploy with an alias
    implementation sweet.autowire('androidx-core-ktx')
}
```

In addition to autowiring dependencies from plugins and external repositories, you can also use it to import file collection dependencies.

> Kotlin DSL

```kotlin
dependencies {
    // Import all jar dependencies in the libs directory of the current project
    implementation(autowire("libs/*.jar"))
    // Import all jar dependencies in the libs directory of the mylibrary project
    implementation(autowire("../mylibrary/libs/*.jar"))
    // Import all jar dependencies in an absolute path directory
    implementation(autowire("/home/test/someDepends/*.jar"))
    // Import all dependencies in the libs directory of the current project, regardless of file extension
    implementation(autowire("libs/*"))
    // You can also import one by one or a group of files
    implementation(
        autowire(
            "libs/*.jar",
            "libs/*.aar",
            "/home/test/someDepends/mylibrary-1.jar",
            "/home/test/someDepends/mylibrary-2.jar"
        )
    )
    // The following is a special case
    // If you directly import a file that has no directory hierarchy and is relative to the current project path,
    // it may not be recognized directly
    // For example, we directly import "mylibrary.jar" under the current project path
    // The following will recognize "mylibrary.jar" as an external repository dependency
    implementation(autowire("mylibrary.jar"))
    // To emphasize that this dependency is a file, use parentheses around the file path
    implementation(autowire("(mylibrary.jar)"))
}
```

> Groovy DSL

```groovy
dependencies {
    // Import all jar dependencies in the libs directory of the current project
    implementation sweet.autowire('libs/*.jar')
    // Import all jar dependencies in the libs directory of the mylibrary project
    implementation sweet.autowire('../mylibrary/libs/*.jar')
    // Import all jar dependencies in an absolute path directory
    implementation sweet.autowire('/home/test/someDepends/*.jar')
    // Import all dependencies in the libs directory of the current project, regardless of file extension
    implementation sweet.autowire('libs/*')
    // You can also import one by one or a group of files
    implementation sweet.autowire(
            'libs/*.jar',
            'libs/*.aar',
            '/home/test/someDepends/mylibrary-1.jar',
            '/home/test/someDepends/mylibrary-2.jar'
    )
    // The following is a special case
    // If you directly import a file that has no directory hierarchy and is relative to the current project path,
    // it may not be recognized directly
    // For example, we directly import "mylibrary.jar" under the current project path
    // The following will recognize "mylibrary.jar" as an external repository dependency
    implementation sweet.autowire('mylibrary.jar')
    // To emphasize that this dependency is a file, use parentheses around the file path
    implementation sweet.autowire('(mylibrary.jar)')
}
```

### Kotlin Multiplatform Support

In Kotlin Multiplatform, it is consistent with the general dependency deployment method.

> Kotlin DSL

```kotlin
sourceSets {
    val androidMain by getting {
        dependencies {
            implementation(autowire("androidx.core:core-ktx"))
            implementation(autowire("libs/*.jar"))
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

## Configure Dependencies Version Declares

There are some dependencies versions that we want to keep fixed in the project and not easily updated or changed.

For this case, you can use version declares to predeclare these versions in the configuration file.

> The following example

```yaml
# Declare some versions to use
versions:
  # Node name only allow 26 letters (upper and lower case) and '.', '_', '-' and must start with a letter
  mydemo-test: 1.0.0

# Reference these versions in plugins declare
plugins:
  # The dependencies version declares of plugins is consistent with libraries,
  # please refer to the example of libraries below
  ...

# Reference these versions in libraries declare
libraries:
  com.mydemo.test:
    test:
      # You can reference the declared version directly at this node
      # The node of the version declared name is higher than that of the dependencies name and alias
      # If the same name exists, the former will be used first
      version-ref: mydemo-test
```

## Configure Dependencies Version Aliases

By default, the version of dependencies declared in the configuration file is fixed, and the version of the deployed dependencies follows the version
in the definition.

If you have such a requirement: the version of the same dependency needs to be different from the version of the dependency in other sub-projects or
the main project.

> The "A" project following example

```kotlin
plugins {
    id("com.mydemo.myplugin") version "1.0.1"
}

dependencies {
    implementation("com.mydemo.test:test:1.0.1")
}
```

> The "B" project following example

```kotlin
plugins {
    id("com.mydemo.myplugin") version "1.0.2"
}

dependencies {
    implementation("com.mydemo.test:test:1.0.2")
}
```

For this situation, you can use version aliases to declare multiple different versions in the configuration file.

> The following example

```yaml
plugins:
  com.mydemo.myplugin:
    alias: demo-myplugin
    # Dependency version (current major version, must exist)
    # You can also use "version-ref"
    version: 1.0.2
    # Custom version alias
    # Only allow 26 letters (upper and lower case) and '.', '_', '-' and must start with a letter
    # (minimum 3 digits in length)
    # Version cannot fill in "+", because the version declared by the version alias will not be autowiring
    versions:
      a-version: 1.0.1
      # If you want to follow the main version, you can fill in "<latest>"
      b-version: <latest>

libraries:
  com.mydemo.test:
    test:
      alias: demo-test
      # Dependency version (current major version, must exist)
      # You can also use "version-ref"
      version: 1.0.2
      # Custom version alias
      # Only allow 26 letters (upper and lower case) and '.', '_', '-' and must start with a letter
      # (minimum 3 digits in length)
      # Version cannot fill in "+", because the version declared by the version alias will not be autowiring
      versions:
        a-version: 1.0.1
        # If you want to follow the main version, you can fill in "<latest>"
        b-version: <latest>
```

Then you can use `.` directly after the current dependency to use its version alias.

The version aliases will be automatically converted to lower camel case.

It is recommended that all aliases be expressed in lowercase letters.

> The "A" project following example

```kotlin
plugins {
    autowire(libs.plugins.com.mydemo.myplugin.aVersion)
    // You can also use dependencies alias directly
    autowire(libs.plugins.demo.myplugin.aVersion)
    // Or use the autowire method to deploy
    // Note that you need to fill in the version alias in the second method parameter of autowire
    autowire("com.mydemo.myplugin", "a-version")
    autowire("demo-myplugin", "a-version")
}

dependencies {
    implementation(com.mydemo.test.test.aVersion)
    // You can also use dependencies alias directly
    implementation(demo.test.aVersion)
    // Or use the autowire method to deploy
    // Note that you need to fill in the version alias in the second method parameter of autowire
    implementation(autowire("com.mydemo.test:test", "a-version"))
    implementation(autowire("demo-test", "a-version"))
}
```

> The "B" project following example

```kotlin
plugins {
    autowire(libs.plugins.com.mydemo.myplugin.bVersion)
    // You can also use dependencies alias directly
    autowire(libs.plugins.demo.myplugin.bVersion)
    // Or use the autowire method to deploy
    // Note that you need to fill in the version alias in the second method parameter of autowire
    autowire("com.mydemo.myplugin", "b-version")
    autowire("demo-myplugin", "b-version")
}

dependencies {
    implementation(com.mydemo.test.test.bVersion)
    // You can also use dependencies alias directly
    implementation(demo.test.bVersion)
    // Or use the autowire method to deploy
    // Note that you need to fill in the version alias in the second method parameter of autowire
    implementation(autowire("com.mydemo.test:test", "b-version"))
    implementation(autowire("demo-test", "b-version"))
}
```

Note: Some features may not be available in `plugins` in the Groovy DSL.

If you don't specify a version alias, the deployed dependency defaults to using the current major version of the dependency (that is the version
declared by "version").

**Pay Attention**

If there is a dependency relationship between project A and project B in the above example,
it will use the newer version of the two first (except plugins).

This is Gradle's dependency inheritance rule and is not controlled by version aliases.

No specific version dependencies cannot use the dependencies version aliases function.

## Configure String Interpolation

You can use ${...} to dynamically insert content into `SweetDependency` config file,
so you can export some sensitive information from your config file.

Where `...` represents the currently used KEY (key value name).

`SweetDependency` will look for content to insert from the following locations in order of priority:

- The current project (Root Project)'s `gradle.properties`
- The current user's `gradle.properties`
- System's `System.getProperties()`
- System's `System.getenv(...)`

> The following example

```yaml
# Configure the repository used by dependencies
repositories:
  your-custom-repo:
    credentials:
      username: ${your-repo.username}
      password: ${your-repo.password}
    url: ${your-repo.url}

# Configure plugins that need to be used
plugins:
  com.android.application:
    version: +

# Configure libraries that need to be used
libraries:
  androidx.core:
    # It can also be set on the node
    ${depends.androidx.core.name}:
      version: +
      versions:
        # Or specific content
        a-version: ${depends.androidx.core.core.a-version}
```

When reading config file, `SweetDependency` will first replace these contents with actual strings before parsing.

If no corresponding content is found for the currently used KEY (key value name), an empty string will be returned.

## Autowire Optimization Suggestions

Now that you understand the basic functionality of `SweetDependency`, here are some optimization suggestions for existing project repositories and
dependencies.

### Repositories Section

According to Gradle's dependencies search rules, the order in which the repositories are added makes sense,
and the search order will be in the order you added them.

You can adjust the order of the repositories appropriately, which will help improve dependencies search efficiency.

> The following example

```yaml
repositories:
  google:
  maven-central:
```

For users in mainland China, you can use the mirror server address preset by `SweetDependency` to speed up the dependencies search.

> The following example

```yaml
repositories:
  aliyun-google-mirror:
  aliyun-maven-central-mirror:
  aliyun-maven-public-mirror:
  aliyun-jcenter-mirror:
```

You can also set the `content` parameter to the `google` repository to improve its efficiency, because it currently only contains the dependencies
starting with the following.

- androidx.*
- com.google.*
- com.android.*

> The following example

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

You can also set the `scope` parameter of the `gradle-plugin-portal` repository to `PLUGINS` to improve search efficiency,
since it will only be applied to plugins.

> The following example

```yaml
repositories:
  gradle-plugin-portal:
    scope: PLUGINS
```

It is recommended to rank `gradle-plugin-portal` first among all repositories, and plugins will be searched using it first.

### Dependencies Section

You can set repositories used by a given dependency to reduce the time spent on autowiring searches.

You can use `version-ref` when appropriate, which can reduce the time-consuming of repeatedly searching for the same version of dependencies.

> The following example

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

## Dump Debug Information

You can manually run the `sweetDependencyDebug` task to dump debug information, which you can find in the root project's task
group `sweet-dependency`.

This operation will dump the data structure of the current `SweetDependency` in memory to the console, you can refer to the configuration file to
check whether the data is correct.

If you think `SweetDependency` is not working as expected, you can also provide us with this data so we can debug and fix it.

## Custom Preferences

You can configure `SweetDependency` using `sweetDependency` lambda method in `settings.gradle` or `settings.gradle.kts` of the root project.

> Kotlin DSL

```kotlin
sweetDependency {

    // Enable SweetDependency, set to false will disable all functions
    isEnable = true

    // SweetDependency configuration file name
    configFileName = "sweet-dependency-config.yaml"

    // Whether to enable dependency autowiring logging
    // This function is enabled by default and will create a log file in the ".gradle/sweet-dependency" directory of the current root project
    isEnableDependenciesAutowireLog = true

    // Whether to enable verbose mode
    // This function is enabled by default, and when disabled,
    // SweetDependency will be silent when not necessary (omit unnecessary logs)
    isEnableVerboseMode = true
}
```

> Groovy DSL

```groovy
sweetDependency {
    enable true
    configFileName 'sweet-dependency-config.yaml'
    enableDependenciesAutowireLog true
    enableVerboseMode true
}
```

## Feedback

If you encounter any problems while using `SweetDependency`, you can always open an `issues` on GitHub to give us feedback.