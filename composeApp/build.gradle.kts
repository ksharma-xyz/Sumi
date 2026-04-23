import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import xyz.ksharma.sumi.gradle.AndroidVersion

plugins {
    alias(libs.plugins.sumi.kotlin.multiplatform)
    alias(libs.plugins.sumi.compose.multiplatform)
    alias(libs.plugins.sumi.android.kmp.library)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidLibrary {
        namespace = "xyz.ksharma.sumi.composeapp"
        compileSdk = AndroidVersion.COMPILE_SDK
        minSdk = AndroidVersion.MIN_SDK

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

        // MANDATORY for AGP 9 to include assets (compose multiplatform resources)
        androidResources {
            enable = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.tooling)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android.driver)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.di.koinCore)
            implementation(libs.di.koinCompose)
            implementation(libs.di.koinComposeViewmodel)
            implementation(libs.navigation3.ui)
            implementation(libs.navigation3.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.json)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.datastore.preferences)
            implementation(projects.game)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }

        commonTest.dependencies {
            implementation(libs.test.kotlin)
        }
    }
}

sqldelight {
    databases {
        create("SumiDatabase") {
            packageName.set("xyz.ksharma.sumi.database")
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "xyz.ksharma.sumi.resources"
    generateResClass = auto
}
