plugins {
    `kotlin-dsl`
    autowire(libs.plugins.kotlin.jvm)
    autowire(libs.plugins.kotlin.serialization)
    autowire(libs.plugins.maven.publish)
}

allprojects {
    group = property.project.groupName
    version = property.project.version
}

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
    implementation(org.jetbrains.kotlin.kotlin.gradle.plugin.api)
    implementation(org.snakeyaml.snakeyaml.engine)
    implementation(com.charleskorn.kaml.kaml)
    implementation(com.squareup.okhttp3.okhttp)
    implementation(com.squareup.javapoet)
}

gradlePlugin {
    plugins {
        create(property.project.moduleName) {
            id = property.project.groupName
            implementationClass = property.gradle.plugin.implementationClass
        }
    }
}

mavenPublishing {
    coordinates(property.project.groupName, property.project.moduleName, property.project.version)
    pom {
        name = property.project.name
        description = property.project.description
        url = property.project.url
        licenses {
            license {
                name = property.project.licence.name
                url = property.project.licence.url
                distribution = property.project.licence.url
            }
        }
        developers {
            developer {
                id = property.project.developer.id
                name = property.project.developer.name
                email = property.project.developer.email
            }
        }
        scm {
            url = property.maven.publish.scm.url
            connection = property.maven.publish.scm.connection
            developerConnection = property.maven.publish.scm.developerConnection
        }
    }
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01)
    signAllPublications()
}