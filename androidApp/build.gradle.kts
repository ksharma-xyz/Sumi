plugins {
    alias(libs.plugins.sumi.android.application)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlyticsPlugin)
}

android {
    namespace = "xyz.ksharma.sumi"

    defaultConfig {
        applicationId = "xyz.ksharma.sumi"
        minSdk = 28
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        // Real Sumi AdMob Android App ID. Same value for both build types so the SDK
        // initializes correctly in development too — the unit IDs themselves diverge
        // (debug uses Google's test units to comply with AdMob's "always test with
        // test ads" policy; release uses real Sumi units).
        val admobAndroidAppId = "ca-app-pub-1771675816656791~9270948994"
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            manifestPlaceholders["admobAppId"] = admobAndroidAppId
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            manifestPlaceholders["admobAppId"] = admobAndroidAppId
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(projects.share)
    implementation(libs.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.di.koinAndroid)
    implementation(libs.datastore.preferences)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    debugImplementation(libs.compose.ui.tooling)
}
