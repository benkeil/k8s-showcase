plugins {
    kotlin("jvm") version "2.0.0" apply false
}

group = "de.benkeil"
version = "0.0.1-SNAPSHOT"

allprojects {
    buildscript {
        repositories {
            gradlePluginPortal()
            maven("https://packages.confluent.io/maven/")
            maven("https://plugins.gradle.org/m2/")
            maven("https://jitpack.io")
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/benkeil/*")
            credentials {
                username = "not_required"
                password = System.getenv("GITHUB_PACKAGES_READ_TOKEN")
            }
            content {
                includeGroup("de.benkeil")
            }
        }
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://packages.confluent.io/maven/")
    }
}
