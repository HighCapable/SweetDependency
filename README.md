# Sweet Dependency

![GitHub license](https://img.shields.io/github/license/HighCapable/SweetDependency?color=blue&cacheSeconds=https%3A%2F%2Fgithub.com%2FHighCapable%2FSweetDependency%2Fblob%2Fmaster%2FLICENSE)
![GitHub release](https://img.shields.io/github/v/release/HighCapable/SweetDependency?display_name=release&logo=github&color=green&link=https%3A%2F%2Fgithub.com%2FHighCapable%2FSweetDependency%2Freleases%2Flatest)
[![Telegram](https://img.shields.io/badge/discussion-Telegram-blue.svg?logo=telegram)](https://t.me/HighCapable_Dev)

<img src="https://github.com/HighCapable/SweetDependency/blob/master/img-src/icon.png?raw=true" width = "100" height = "100" alt="LOGO"/>

An easy autowire and manage dependencies Gradle plugin.

English | [简体中文](https://github.com/HighCapable/SweetDependency/blob/master/README-zh-CN.md)

## What's this

This is a Gradle plugin for managing Gradle dependencies.

Although Gradle later launched the Version Catalogs to manage dependencies, its method is still not free and user-friendly, and has limitations.

Different from traditional dependency management methods, `SweetDependency` uses YAML for dynamic configuration,
which is relatively readable, and the configuration process is simple and easy to use.

## Compatibility

Not just Android projects, any project that uses Gradle as a build tool will work.

Currently there is only Gradle plugin, IDEA related plugins are still under development,
support for syntax checking of configuration file and displaying configuration file in the Android project's Gradle files list is expected.

Gradle `7.x.x` and `8.x.x` are supported, other versions have not been tested and are not recommended.

> Build Script Language

- Kotlin DSL

It is recommended to use this language as the build script language first, which is also the language currently recommended by Gradle.

- Groovy DSL

Some functions may be incompatible, support will be gradually dropped in the future, and some functions may become unavailable.

> Related Feature List

Some functions will be gradually improved following the needs of users.

- [x] Support Kotlin Multiplatform

- [x] Manage Gradle dependencies (plugins)

- [x] Manage Gradle dependencies (libraries)

- [x] Autowire Maven dependencies (POM, BOM)

- [ ] Autowire Ivy dependencies

## Get Started

- [Click here](https://github.com/HighCapable/SweetDependency/blob/master/docs/guide.md) to view the documentation

## Changelog

- [Click here](https://github.com/HighCapable/SweetDependency/blob/master/docs/changelog.md) to view the historical changelog

## Promotion

If you are looking for a Gradle plugin that can automatically generate properties key-values,
you can check out the [SweetProperty](https://github.com/HighCapable/SweetProperty) project.

This project also uses **SweetProperty**.

## Star History

![Star History Chart](https://api.star-history.com/svg?repos=HighCapable/SweetDependency&type=Date)

## License

- [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0)

```
Apache License Version 2.0

Copyright (C) 2019-2023 HighCapable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

Copyright © 2019-2023 HighCapable