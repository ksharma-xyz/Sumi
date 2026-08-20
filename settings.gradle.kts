rootProject.name = "Sumi"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("gradle/build-logic")

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

// Darpan is a git submodule until it is published to Central. As a composite build, Gradle
// substitutes the xyz.ksharma:darpan-* coordinates in the catalog for these projects
// automatically, so CI needs no published artifact and no mavenLocal. After the Central publish
// this include goes away and the same catalog entries resolve normally.
includeBuild("darpan")

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":composeApp")
include(":game")
include(":share")
