# 更新日志

## 1.0.0 | 2023.09.03

- 首个版本提交至 Maven

## 1.0.1 | 2023.09.07

- 使用 `net.lingala.zip4j` 取代 JDK 默认创建压缩文档功能修复在 Windows 平台中 Gradle 8.0.2+ 版本创建的 JAR 损坏导致找不到生成的 Class 问题
- 重构自动生成代码部分的装载功能，增加可能找不到 Class 的错误提示
- 新增在设置了未定义版本的插件依赖条件下直接运行自动装配相关 Gradle Task 将抛出异常
- 修复可能的旧版本 Gradle 在使用 `repositories` 的 `content` 功能会抛出异常