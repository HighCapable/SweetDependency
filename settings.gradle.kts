enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
}
plugins {
    id("com.highcapable.sweetdependency") version "1.0.0"
    id("com.highcapable.sweetproperty") version "1.0.2"
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
    rootProject { sourcesCode { isEnable = false } }
    project("sweetdependency-gradle-plugin") {
        buildScript { isEnableTypeAutoConversion = false }
    }
}
rootProject.name = "SweetDependency"
include(":sweetdependency-gradle-plugin")