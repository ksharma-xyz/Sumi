package xyz.ksharma.sumi.ads

import app.lexilabs.basic.ads.AdUnitId
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import xyz.ksharma.sumi.platform.AppInfo

/**
 * Cross-platform ad unit IDs.
 *
 * Resolved as a Koin singleton with [AppInfo] injected. The debug-vs-release
 * branch is driven by the platform-native debug flag (Android FLAG_DEBUGGABLE,
 * iOS Platform.isDebugBinary), so TestFlight archives and Play Store release
 * bundles ship the production unit IDs automatically — no Gradle property
 * forwarding or Xcode build-phase scripting needed.
 *
 * Test IDs: https://developers.google.com/admob/android/test-ads
 */
@OptIn(DependsOnGoogleMobileAds::class)
class AdUnits(appInfo: AppInfo) {

    private val isDebug = appInfo.isDebugBuild

    val banner: String = AdUnitId.autoSelect(
        androidAdUnitId = if (isDebug) {
            "ca-app-pub-3940256099942544/6300978111"
        } else {
            "ca-app-pub-1771675816656791/4801131656"
        },
        iosAdUnitId = if (isDebug) {
            "ca-app-pub-3940256099942544/2934735716"
        } else {
            "ca-app-pub-1771675816656791/8716325930"
        },
    )

    val interstitial: String = AdUnitId.autoSelect(
        androidAdUnitId = if (isDebug) {
            "ca-app-pub-3940256099942544/1033173712"
        } else {
            "ca-app-pub-1771675816656791/6038280998"
        },
        iosAdUnitId = if (isDebug) {
            "ca-app-pub-3940256099942544/4411468910"
        } else {
            "ca-app-pub-1771675816656791/9083766250"
        },
    )

    val rewarded: String = AdUnitId.autoSelect(
        androidAdUnitId = if (isDebug) {
            "ca-app-pub-3940256099942544/5224354917"
        } else {
            "ca-app-pub-1771675816656791/9638525361"
        },
        iosAdUnitId = if (isDebug) {
            "ca-app-pub-3940256099942544/1712485313"
        } else {
            "ca-app-pub-1771675816656791/9877973172"
        },
    )
}
