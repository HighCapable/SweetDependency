# Changelog

## 1.0.0 | 2023.09.03

- The first version is submitted to Maven

## 1.0.1 | 2023.09.07

- Use `net.lingala.zip4j` to replace JDK's default function of creating compressed files and fix the problem that the JAR created by Gradle 8.0.2+
  version on Windows platform is broken and the generated classes cannot be found
- Refactor the loading function of the automatically generated code part, and add an error message that classes may not be found
- Added an exception will be thrown when running the autowire related Gradle task directly
  under the condition of setting an undefined version of plugins
- Fix possible old version of Gradle throwing exception when using `content` function of `repositories`

## 1.0.2 | 2023.09.26

- The automatic code generation function will always output source code files to facilitate debugging when the generation fails
- Fix dependencies with some consecutive names may cause generation failure
- Fix plugin own update function
- Fix Gradle lifecycle problem that may cause the project scope of the `autowire` method to be incorrect
- Improve and adopt Gradle project naming convention
- Added dependencies namespace to enable or disable the generation function, after the update, you need to refer to the documentation to migrate the
  node names of some configuration files yourself, otherwise errors will occur
- Added the function of using `<plugins>::` or `<libraries>::` to access the dependencies name and alias of other nodes and set them to `version-ref`

## 1.0.3 | 2023.11.04

- Fix a `Class` conflict between this plugin and the `Kotlin` plugin starting from `1.0.0` version
- Change the configuration dependency `autowire(...)` in Kotlin Multiplatform to `sweet.autowire(...)`
- Generated code is marked with `@Nonnull` to make it recognized as a non-null return type in Kotlin DSL scripts
- Some other functional improvements