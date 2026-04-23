plugins {
    alias(libs.plugins.sumi.android.application)
    alias(libs.plugins.composeCompiler)
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
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
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
    implementation(project(":composeApp"))
    implementation(libs.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.di.koinAndroid)
    implementation(libs.datastore.preferences)
    debugImplementation(libs.compose.ui.tooling)
}
