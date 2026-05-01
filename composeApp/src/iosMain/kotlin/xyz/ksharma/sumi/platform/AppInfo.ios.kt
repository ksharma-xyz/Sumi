@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class)

package xyz.ksharma.sumi.platform

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/**
 * iOS [AppInfo] backed by `kotlin.native.Platform.isDebugBinary`. The
 * Kotlin/Native runtime flips this flag based on the framework's build
 * configuration as set by Xcode — Debug for Cmd+R, Release for Archive
 * / TestFlight / App Store. No Gradle property forwarding needed.
 */
class IosAppInfo : AppInfo {
    override val isDebugBuild: Boolean
        get() = Platform.isDebugBinary
}
