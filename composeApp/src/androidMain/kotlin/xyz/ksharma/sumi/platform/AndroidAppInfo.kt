package xyz.ksharma.sumi.platform

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * Android [AppInfo] backed by `ApplicationInfo.FLAG_DEBUGGABLE`. AGP
 * sets the flag for any build whose `buildType` has `isDebuggable = true`
 * (the default for `debug`, false for `release`), so signed-bundle release
 * builds always report `isDebugBuild = false` without any extra Gradle
 * properties or build-script wiring.
 */
class AndroidAppInfo(private val context: Context) : AppInfo {
    override val isDebugBuild: Boolean
        get() = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}
