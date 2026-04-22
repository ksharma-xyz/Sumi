import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "xyz.ksharma.sumi.gradle"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.composeCompiler.gradlePlugin)
    compileOnly(libs.detekt.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("composeMultiplatform") {
            id = "sumi.compose.multiplatform"
            implementationClass = "xyz.ksharma.sumi.gradle.ComposeMultiplatformConventionPlugin"
        }
        register("androidApplication") {
            id = "sumi.android.application"
            implementationClass = "xyz.ksharma.sumi.gradle.AndroidApplicationConventionPlugin"
        }
        register("androidKmpLibrary") {
            id = "sumi.android.kmp.library"
            implementationClass = "xyz.ksharma.sumi.gradle.AndroidKmpLibraryConventionPlugin"
        }
        register("kotlinMultiplatform") {
            id = "sumi.kotlin.multiplatform"
            implementationClass = "xyz.ksharma.sumi.gradle.KotlinMultiplatformConventionPlugin"
        }
    }
}
