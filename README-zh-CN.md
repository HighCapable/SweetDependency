# Sweet Dependency

[![GitHub license](https://img.shields.io/github/license/HighCapable/SweetDependency?color=blue)](https://github.com/HighCapable/SweetDependency/blob/master/LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/HighCapable/SweetDependency?display_name=release&logo=github&color=green)](https://github.com/HighCapable/SweetDependency/releases)
[![Telegram](https://img.shields.io/badge/discussion-Telegram-blue.svg?logo=telegram)](https://t.me/HighCapable_Dev)

<img src="https://github.com/HighCapable/SweetDependency/blob/master/img-src/icon.png?raw=true" width = "100" height = "100" alt="LOGO"/>

一个轻松自动装配和管理依赖的 Gradle 插件。

[English](https://github.com/HighCapable/SweetDependency/blob/master/README.md) | 简体中文

## 这是什么

这是一个用来管理 Gradle 依赖的 Gradle 插件，所有依赖集中管理并自动更新，解决了每个子项目都需要手动更新到相同版本依赖的问题。

虽然 Gradle 后期推出了 Version Catalogs 来管理依赖，但是它的方式依然不够自由和人性化，且存在限制。

不同于传统的依赖管理方式，`SweetDependency` 采用 YAML 进行动态配置，可读性相对较高，配置过程简单且易用。

## 兼容性

不仅仅是 Android 项目，任何使用 Gradle 作为构建工具的项目都可以使用。

目前暂时只有 Gradle 插件，IDEA 的相关插件还在计划开发中，预计将会支持配置文件的语法检查和将配置文件显示在 Android 项目的 Gradle 文件列表中。

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

- [点击这里](https://github.com/HighCapable/SweetDependency/blob/master/docs/guide-zh-CN.md) 查看使用文档

## 更新日志

- [点击这里](https://github.com/HighCapable/SweetDependency/blob/master/docs/changelog-zh-CN.md) 查看历史更新日志

## 项目推广

如果你正在寻找一个可以自动生成属性键值的 Gradle 插件，你可以了解一下 [SweetProperty](https://github.com/HighCapable/SweetProperty) 项目。

本项目同样使用了 **SweetProperty**。

## 捐赠支持

工作不易，无意外情况此项目将继续维护下去，提供更多可能，欢迎打赏。

<img src="https://github.com/fankes/fankes/blob/main/img-src/payment_code.jpg?raw=true" width = "500" alt="Payment Code"/>

## Star History

![Star History Chart](https://api.star-history.com/svg?repos=HighCapable/SweetDependency&type=Date)

## 许可证

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

版权所有 © 2019-2023 HighCapable