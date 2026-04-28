import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import xyz.ksharma.sumi.gradle.AndroidVersion

import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.sumi.kotlin.multiplatform)
    alias(libs.plugins.sumi.compose.multiplatform)
    alias(libs.plugins.sumi.android.kmp.library)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.buildkonfig)
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
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.admob)
            implementation(libs.android.ump)
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
            implementation(libs.kotlinx.datetime)
            implementation(libs.basic.ads)
            implementation(libs.firebase.gitLiveAnalytics)
            implementation(libs.firebase.gitLiveCrashlytics)
            implementation(projects.game)
            implementation(projects.share)
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

buildkonfig {
    packageName = "xyz.ksharma.sumi"
    objectName = "BuildKonfig"
    exposeObjectWithName = "BuildKonfig"

    // Defaults = debug. AdMob test unit IDs are safe to commit and won't generate revenue.
    // Reference: https://developers.google.com/admob/android/test-ads
    defaultConfigs {
        buildConfigField(BOOLEAN, "IS_DEBUG", "true")
        buildConfigField(STRING, "BUILD_TYPE", "\"debug\"")

        buildConfigField(STRING, "AD_BANNER_ANDROID",       "\"ca-app-pub-3940256099942544/6300978111\"")
        buildConfigField(STRING, "AD_BANNER_IOS",           "\"ca-app-pub-3940256099942544/2934735716\"")
        buildConfigField(STRING, "AD_INTERSTITIAL_ANDROID", "\"ca-app-pub-3940256099942544/1033173712\"")
        buildConfigField(STRING, "AD_INTERSTITIAL_IOS",     "\"ca-app-pub-3940256099942544/4411468910\"")
        buildConfigField(STRING, "AD_REWARDED_ANDROID",     "\"ca-app-pub-3940256099942544/5224354917\"")
        buildConfigField(STRING, "AD_REWARDED_IOS",         "\"ca-app-pub-3940256099942544/1712485313\"")
    }

    // Activate with: ./gradlew ... -Pbuildkonfig.flavor=release
    // TODO(launch): replace AD_* with real Sumi unit IDs from the AdMob console before shipping.
    defaultConfigs("release") {
        buildConfigField(BOOLEAN, "IS_DEBUG", "false")
        buildConfigField(STRING, "BUILD_TYPE", "\"release\"")

        buildConfigField(STRING, "AD_BANNER_ANDROID",       "\"ca-app-pub-3940256099942544/6300978111\"")
        buildConfigField(STRING, "AD_BANNER_IOS",           "\"ca-app-pub-3940256099942544/2934735716\"")
        buildConfigField(STRING, "AD_INTERSTITIAL_ANDROID", "\"ca-app-pub-3940256099942544/1033173712\"")
        buildConfigField(STRING, "AD_INTERSTITIAL_IOS",     "\"ca-app-pub-3940256099942544/4411468910\"")
        buildConfigField(STRING, "AD_REWARDED_ANDROID",     "\"ca-app-pub-3940256099942544/5224354917\"")
        buildConfigField(STRING, "AD_REWARDED_IOS",         "\"ca-app-pub-3940256099942544/1712485313\"")
    }
}
