pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
plugins {
    // Import the SweetDependency plugin here
    // 在这里引入 SweetDependency 插件
    id("com.highcapable.sweetdependency") version "1.0.1"
}
sweetDependency {
    configFileName = "sweet-dependency-config.yaml"
    isEnableDependenciesAutowireLog = true
    isEnableVerboseMode = true
}
rootProject.name = "SweetDependency-Sample-Android"
include(":demo-app")
include(":demo-library")