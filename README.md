# Sweet Dependency

[![GitHub license](https://img.shields.io/github/license/HighCapable/SweetDependency?color=blue)](https://github.com/HighCapable/SweetDependency/blob/master/LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/HighCapable/SweetDependency?display_name=release&logo=github&color=green)](https://github.com/HighCapable/SweetDependency/releases)
[![Telegram](https://img.shields.io/badge/discussion-Telegram-blue.svg?logo=telegram)](https://t.me/HighCapable_Dev)
[![QQ](https://img.shields.io/badge/discussion-QQ-blue.svg?logo=tencent-qq&logoColor=red)](https://qm.qq.com/cgi-bin/qm/qr?k=Pnsc5RY6N2mBKFjOLPiYldbAbprAU3V7&jump_from=webapi&authKey=X5EsOVzLXt1dRunge8ryTxDRrh9/IiW1Pua75eDLh9RE3KXE+bwXIYF5cWri/9lf)

<img src="img-src/icon.png" width = "100" height = "100" alt="LOGO"/>

An easy autowire and manage dependencies Gradle plugin.

English | [简体中文](README-zh-CN.md)

| <img src="https://github.com/HighCapable/.github/blob/main/img-src/logo.jpg?raw=true" width = "30" height = "30" alt="LOGO"/> | [HighCapable](https://github.com/HighCapable) |
|-------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------|

This project belongs to the above-mentioned organization, **click the link above to follow this organization** and discover more good projects.

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

- [Click here](docs/guide.md) to view the documentation

## Changelog

- [Click here](docs/changelog.md) to view the historical changelog

## Promotion

If you are looking for a Gradle plugin that can automatically generate properties key-values,
you can check out the [SweetProperty](https://github.com/HighCapable/SweetProperty) project.

This project also uses **SweetProperty**.

<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
     <h2>Hey, please stay! 👋</h2>
     <h3>Here are related projects such as Android development tools, UI design, Gradle plugins, Xposed Modules and practical software. </h3>
     <h3>If the project below can help you, please give me a star! </h3>
     <h3>All projects are free, open source, and follow the corresponding open source license agreement. </h3>
     <h1><a href="https://github.com/fankes/fankes/blob/main/project-promote/README.md">→ To see more about my projects, please click here ←</a></h1>
</div>

## Star History

![Star History Chart](https://api.star-history.com/svg?repos=HighCapable/SweetDependency&type=Date)

## License

- [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0)

```
Apache License Version 2.0

Copyright (C) 2019 HighCapable

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

Copyright © 2019 HighCapable