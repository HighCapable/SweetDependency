plugins {
    `kotlin-dsl`
    autowire(libs.plugins.kotlin.jvm)
    autowire(libs.plugins.kotlin.serialization)
    autowire(libs.plugins.maven.publish)
}

group = property.project.groupName
version = property.project.version

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

kotlin {
    jvmToolchain(21)
    sourceSets.all { languageSettings { languageVersion = "2.0" } }
    compilerOptions {
        freeCompilerArgs = listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
    }
}

dependencies {
    implementation(org.snakeyaml.snakeyaml.engine)
    implementation(com.charleskorn.kaml.kaml)
    implementation(com.squareup.okhttp3.okhttp)
    implementation(com.squareup.javapoet)
    implementation(net.lingala.zip4j.zip4j)
}

gradlePlugin {
    plugins {
        create(property.project.moduleName) {
            id = property.project.groupName
            implementationClass = property.gradle.plugin.implementationClass
        }
    }
}