# Sweet Dependency

[![GitHub license](https://img.shields.io/github/license/HighCapable/SweetDependency?color=blue&style=flat-square)](https://github.com/HighCapable/SweetDependency/blob/master/LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/HighCapable/SweetDependency?display_name=release&logo=github&color=green&style=flat-square)](https://github.com/HighCapable/SweetDependency/releases)
[![Telegram](https://img.shields.io/badge/discussion-Telegram-blue.svg?logo=telegram&style=flat-square)](https://t.me/HighCapable_Dev)
[![QQ](https://img.shields.io/badge/discussion-QQ-blue.svg?logo=tencent-qq&logoColor=red&style=flat-square)](https://qm.qq.com/cgi-bin/qm/qr?k=Pnsc5RY6N2mBKFjOLPiYldbAbprAU3V7&jump_from=webapi&authKey=X5EsOVzLXt1dRunge8ryTxDRrh9/IiW1Pua75eDLh9RE3KXE+bwXIYF5cWri/9lf)

<img src="img-src/icon.png" width = "100" height = "100" alt="LOGO"/>

一个轻松自动装配和管理依赖的 Gradle 插件。

[English](README.md) | 简体中文

| <img src="https://github.com/HighCapable/.github/blob/main/img-src/logo.jpg?raw=true" width = "30" height = "30" alt="LOGO"/> | [HighCapable](https://github.com/HighCapable) |
| ----------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------- |

这个项目属于上述组织，**点击上方链接关注这个组织**，发现更多好项目。

## 项目停止维护的说明

由于个人时间和精力有限，且目前 Gradle 官方已有更好的依赖管理方案：Version Catalog，本项目将停止维护。

这个问题我想了很久，因为 Gradle 的版本目录已经非常成熟，且有 IDE 层面的支持和代码跳转支持，不需要再手写 TOML 配置文件，这对于配置文件是不是 YAML 格式已经不重要了，并且版本目录有第三方可以一键更新版本的插件，使用起来应该也非常方便，这样一看下来，`SweetDependency` 的优势就不明显了，我觉得这个项目陪我走过的三年，是很美好的，因为它确实解决了我的项目版本管理的关键问题，也为其他有同样需求的开发者提供了帮助。

这个项目有着很多黑盒问题和早年一些我很不成熟的想法，已被标记为**屎山**或者**代码荒漠**，所有代码仅供学习参考，请勿用于生产环境。

最后说一点遗憾，IDEA 插件由于种种原因没写出来，而且我想了一下写出来我可能也大概率没精力维护，我不想把自己的精力浪费在一个不能给我带来生产价值的版本管理插件上了。

`SweetDependency` 不会再适配 Gradle 9.0.0 及以后的版本，就到这里了，这个仓库不会归档，我想是的，我会一直把它留在这里，如果你希望接手这个项目并继续维护，你可以 fork 本仓库并在 README 中注明此事宜，感谢你的支持。

## 这是什么

这是一个用来管理 Gradle 依赖的 Gradle 插件，所有依赖集中管理并自动更新，解决了每个子项目都需要手动更新到相同版本依赖的问题。

虽然 Gradle 后期推出了 Version Catalogs 来管理依赖，但是它的方式依然不够自由和人性化，且存在限制。

不同于传统的依赖管理方式，`SweetDependency` 采用 YAML 进行动态配置，可读性相对较高，配置过程简单且易用。

## 兼容性

不仅仅是 Android 项目，任何使用 Gradle 作为构建工具的项目都可以使用。

目前暂时只有 Gradle 插件，~~IDEA 的相关插件还在计划开发中~~，预计将会支持配置文件的语法检查和将配置文件显示在 Android 项目的 Gradle 文件列表中。

支持 Gradle `7.x.x` 和 `8.x.x`，其它版本未做测试并不推荐使用。

> 构建脚本语言

- Kotlin DSL

推荐优先使用此语言作为构建脚本语言，这也是目前 Gradle 推荐的语言。

- Groovy DSL

部分功能可能无法兼容，在后期会逐渐放弃支持，且部分功能会无法使用。

> 相关功能列表

部分功能将跟随后期用户需求逐渐完善。

- [x] 支持 Kotlin Multiplatform

- [x] 管理 Gradle 插件依赖

- [x] 管理 Gradle 库依赖

- [x] 自动装配 Maven 依赖 (POM、BOM)

- [ ] 自动装配 Ivy 依赖

## 开始使用

- [点击这里](docs/guide-zh-CN.md) 查看使用文档

## 更新日志

- [点击这里](docs/changelog-zh-CN.md) 查看历史更新日志

## 项目推广

如果你正在寻找一个可以自动生成属性键值的 Gradle 插件，你可以了解一下 [SweetProperty](https://github.com/HighCapable/SweetProperty) 项目。

本项目同样使用了 **SweetProperty**。

<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
    <h2>嘿，还请君留步！👋</h2>
    <h3>这里有 Android 开发工具、UI 设计、Gradle 插件、Xposed 模块和实用软件等相关项目。</h3>
    <h3>如果下方的项目能为你提供帮助，不妨为我点个 star 吧！</h3>
    <h3>所有项目免费、开源，遵循对应开源许可协议。</h3>
    <h1><a href="https://github.com/fankes/fankes/blob/main/project-promote/README-zh-CN.md">→ 查看更多关于我的项目，请点击这里 ←</a></h1>
</div>

## Star History

![Star History Chart](https://api.star-history.com/svg?repos=HighCapable/SweetDependency&type=Date)

## 许可证

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

版权所有 © 2019 HighCapable