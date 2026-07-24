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
        // CI sets `-PversionCode="$VERSION_CODE"` so signed-bundle releases get a
        // monotonic value from the GitHub repo variable ANDROID_VERSION_CODE.
        // Local builds fall back to 10 — high enough that an accidental local
        // upload to Play Console is rejected for collision rather than landing.
        versionCode = findProperty("versionCode")?.toString()?.toInt() ?: 107
        versionName = "1.5.0"
    }

    signingConfigs {
        create("release") {
            // build-android.yml CI workflow base64-decodes ANDROID_KEYSTORE_FILE
            // into rootProject/keystore.jks and exports the three secrets below
            // as env vars before invoking Gradle. Local release builds need the
            // same keystore + env vars to sign — see CI/CD setup notes.
            storeFile = rootProject.file("keystore.jks")
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("ANDROID_KEY_ALIAS")
            keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
        }
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
            signingConfig = signingConfigs.getByName("release")
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
