# Sweet Dependency

[![GitHub license](https://img.shields.io/github/license/HighCapable/SweetDependency?color=blue)](https://github.com/HighCapable/SweetDependency/blob/master/LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/HighCapable/SweetDependency?display_name=release&logo=github&color=green)](https://github.com/HighCapable/SweetDependency/releases)
[![Telegram](https://img.shields.io/badge/discussion-Telegram-blue.svg?logo=telegram)](https://t.me/HighCapable_Dev)
[![QQ](https://img.shields.io/badge/discussion-QQ-blue.svg?logo=tencent-qq&logoColor=red)](https://qm.qq.com/cgi-bin/qm/qr?k=Pnsc5RY6N2mBKFjOLPiYldbAbprAU3V7&jump_from=webapi&authKey=X5EsOVzLXt1dRunge8ryTxDRrh9/IiW1Pua75eDLh9RE3KXE+bwXIYF5cWri/9lf)

<img src="img-src/icon.png" width = "100" height = "100" alt="LOGO"/>

依存関係を簡単に自動配線と管理ができる Gradle プラグインです。

[English](README.md) | [简体中文](README-zh-CN.md) | 日本語

| <img src="https://github.com/HighCapable/.github/blob/main/img-src/logo.jpg?raw=true" width = "30" height = "30" alt="LOGO"/> | [HighCapable](https://github.com/HighCapable) |
|-------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------|

このプロジェクトは上記の組織に属しています。**この組織をフォローするには上記のリンク**をクリックして、その他の優れたプロジェクトをご確認ください。

## これは何でしょうか?

これは、Gradle の依存関係を管理するための Gradle プラグインです。

Gradle は後に依存関係を管理するための｢バージョンカタログ｣を導入しましたが、その方法は無料でないことやユーザーフレンドリーでもなく、制限もあります。

従来の依存関係の管理方法とは異なる、`SweetDependency` は動的な構成に YAML を使用します。
これで比較的に読みやすくなり、構成プロセスもシンプルになります。

## 互換性

Android のプロジェクトだけでなく、ビルドツールとして Gradle を使用するすべてのプロジェクトで動作します。

現在は、Gradle のプラグインのみで IDEA 関連のプラグインは開発中です。構成ファイルの構文のチェックと、Android のプロジェクトの Gradle ファイルリストでの構成ファイルの表示のサポートが期待されています。

Gradle `7.x.x` と `8.x.x` をサポートしていますが、他のバージョンは未テストかつ非推奨です。

> ビルドスクリプト言語

- Kotlin DSL

この言語を最初にビルドスクリプト言語として使用することを推奨します。これは現在の Gradle でも推奨をしている言語です。

- Groovy DSL

一部の機能での互換性がなく、将来的にサポートが段階的に廃止され一部の機能が利用できなくなる可能性があります。

> 関連機能の一覧

一部の機能はユーザーのニーズに応じて段階的に改善されます。

- [x] Kotlin マルチプラットフォームのサポート

- [x] Gradle 依存関係の管理 (プラグイン)

- [x] Gradle 依存関係の管理 (ライブラリ)

- [x] Maven 依存関係の自動配線 (POM、BOM)

- [ ] Ivy 依存関係の自動配線

## 始め方

- ドキュメントを確認するには、[こちら](docs/guide.md)をクリックしてください。

## 更新履歴

- 更新履歴を確認するには、[こちら](docs/changelog.md)をクリックしてください。

## プロモーション

プロパティのキーと値を自動的に生成する Gradle プラグインをお探しの方は、[SweetProperty のプロジェクト](https://github.com/HighCapable/SweetProperty)をご確認ください。

このプロジェクトは **SweetProperty** も使用します。

<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
     <h2>ねぇねぇ、ちょっときいて! 👋</h2>
     <h3>Android の開発ツール、UI デザイン、Gradle プラグイン、Xposed モジュール、実用的なソフトウェアなどの関連プロジェクトを紹介しています。</h3>
     <h3>プロジェクトがあなたの役に立てたのであれば、Star を付けてください!</h3>
     <h3>すべてのプロジェクトは無料でオープンソースであり、対応するオープンソースライセンスのルールに従っています。</h3>
     <h1><a href="https://github.com/fankes/fankes/blob/main/project-promote/README.md">→ 私のプロジェクトの詳細はここをクリックしてください ←</a></h1>
</div>

## Star の推移

![Star History Chart](https://api.star-history.com/svg?repos=HighCapable/SweetDependency&type=Date)

## ライセンス

- [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0)

```
Apache License Version 2.0

Copyright (C) 2019-2024 HighCapable

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

Copyright © 2019-2024 HighCapable