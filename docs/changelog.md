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