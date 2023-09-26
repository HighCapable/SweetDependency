pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
}
plugins {
    id("com.highcapable.sweetdependency") version "1.0.1"
    id("com.highcapable.sweetproperty") version "1.0.3"
}
sweetDependency {
    isEnableVerboseMode = false
}
sweetProperty {
    global {
        sourcesCode {
            className = rootProject.name
            isEnableRestrictedAccess = true
        }
    }
    rootProject { all { isEnable = false } }
}
rootProject.name = "SweetDependency"
include(":sweetdependency-gradle-plugin")