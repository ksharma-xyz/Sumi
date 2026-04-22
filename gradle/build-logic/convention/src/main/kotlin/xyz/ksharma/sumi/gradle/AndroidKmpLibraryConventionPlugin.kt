package xyz.ksharma.sumi.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for KMP Android libraries (AGP 9+).
 * Uses `com.android.kotlin.multiplatform.library` — NOT `com.android.library`.
 *
 * Module usage:
 * ```
 * plugins { alias(libs.plugins.sumi.android.kmp.library) }
 * kotlin {
 *     androidLibrary {
 *         namespace = "xyz.ksharma.sumi.module"
 *         compileSdk = AndroidVersion.COMPILE_SDK
 *         minSdk = AndroidVersion.MIN_SDK
 *     }
 * }
 * ```
 */
class AndroidKmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.kotlin.multiplatform.library")
            apply("io.gitlab.arturbosch.detekt")
        }
        configureDetekt()
    }
}
