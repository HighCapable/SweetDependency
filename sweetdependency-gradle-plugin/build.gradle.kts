plugins {
    `kotlin-dsl`
    autowire(libs.plugins.kotlin.jvm)
    autowire(libs.plugins.kotlin.serialization)
    autowire(libs.plugins.maven.publish)
}

group = property.project.groupName
version = property.project.version

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

kotlin {
    jvmToolchain(17)
    sourceSets.all { languageSettings { languageVersion = "2.0" } }
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