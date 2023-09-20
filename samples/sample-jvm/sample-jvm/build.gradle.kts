plugins {
    autowire(libs.plugins.org.jetbrains.kotlin.jvm)
    application
}

group = "com.highcapable.sweetdependency.demo_jvm"
version = "1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.highcapable.sweetdependency.demo_jvm.MainKt")
}

dependencies {
    testImplementation(org.jetbrains.kotlin.kotlin.test)
}