import xyz.ksharma.sumi.gradle.AndroidVersion

plugins {
    alias(libs.plugins.sumi.kotlin.multiplatform)
    alias(libs.plugins.sumi.compose.multiplatform)
    alias(libs.plugins.sumi.android.kmp.library)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "xyz.ksharma.sumi.share"
        compileSdk = AndroidVersion.COMPILE_SDK
        minSdk = AndroidVersion.MIN_SDK
    }

    sourceSets {
        androidMain.dependencies {
            api(libs.di.koinAndroid)
        }
        commonMain.dependencies {
            implementation(libs.compose.ui)
            implementation(libs.compose.runtime)
            api(libs.di.koinComposeViewmodel)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
