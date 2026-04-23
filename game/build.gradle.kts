import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import xyz.ksharma.sumi.gradle.AndroidVersion

plugins {
    alias(libs.plugins.sumi.kotlin.multiplatform)
    alias(libs.plugins.sumi.android.kmp.library)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidLibrary {
        namespace = "xyz.ksharma.sumi.game"
        compileSdk = AndroidVersion.COMPILE_SDK
        minSdk = AndroidVersion.MIN_SDK

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

        withHostTestBuilder {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.di.koinCore)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(libs.test.kotlin)
        }
    }
}
