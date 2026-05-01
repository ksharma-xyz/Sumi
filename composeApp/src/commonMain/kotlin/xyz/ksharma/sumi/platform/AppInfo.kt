package xyz.ksharma.sumi.platform

/**
 * Per-platform info Sumi needs to read at runtime — currently just the
 * debug-vs-release flag. Each platform provides its own implementation
 * via Koin, using the OS-native debug signal so we never need to forward
 * Gradle properties or branch on Xcode CONFIGURATION manually:
 *
 * - Android: `applicationInfo.flags and FLAG_DEBUGGABLE != 0`. AGP sets
 *   FLAG_DEBUGGABLE per `buildTypes { debug / release }` automatically.
 * - iOS: `kotlin.native.Platform.isDebugBinary`. Kotlin/Native flips this
 *   based on Xcode's CONFIGURATION (Debug vs Release / Archive).
 */
interface AppInfo {
    val isDebugBuild: Boolean
}
