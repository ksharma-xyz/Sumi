package xyz.ksharma.sumi.ads

import app.lexilabs.basic.ads.AdUnitId
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import xyz.ksharma.sumi.BuildKonfig

/**
 * Cross-platform ad unit IDs.
 *
 * Hardcoded here (rather than read through BuildKonfig STRING fields) because
 * `buildConfigField(STRING, "...", "\"value\"")` generates `val X: String = "\"value\""`,
 * which keeps the surrounding `"` characters as literal chars at runtime — Google's
 * AdMob SDK then rejects the ID with "Cannot determine request type".
 *
 * Test IDs: https://developers.google.com/admob/android/test-ads
 * Switch to release IDs by toggling [BuildKonfig.IS_DEBUG] (Boolean fields are unaffected
 * by the escape bug).
 */
@OptIn(DependsOnGoogleMobileAds::class)
object AdUnits {

    val Banner: String = AdUnitId.autoSelect(
        androidAdUnitId = if (BuildKonfig.IS_DEBUG) {
            "ca-app-pub-3940256099942544/6300978111"
        } else {
            "ca-app-pub-1771675816656791/4801131656"
        },
        iosAdUnitId = if (BuildKonfig.IS_DEBUG) {
            "ca-app-pub-3940256099942544/2934735716"
        } else {
            "ca-app-pub-1771675816656791/8716325930"
        },
    )

    val Interstitial: String = AdUnitId.autoSelect(
        androidAdUnitId = if (BuildKonfig.IS_DEBUG) {
            "ca-app-pub-3940256099942544/1033173712"
        } else {
            "ca-app-pub-1771675816656791/6038280998"
        },
        iosAdUnitId = if (BuildKonfig.IS_DEBUG) {
            "ca-app-pub-3940256099942544/4411468910"
        } else {
            "ca-app-pub-1771675816656791/9083766250"
        },
    )

    val Rewarded: String = AdUnitId.autoSelect(
        androidAdUnitId = if (BuildKonfig.IS_DEBUG) {
            "ca-app-pub-3940256099942544/5224354917"
        } else {
            "ca-app-pub-1771675816656791/9638525361"
        },
        iosAdUnitId = if (BuildKonfig.IS_DEBUG) {
            "ca-app-pub-3940256099942544/1712485313"
        } else {
            "ca-app-pub-1771675816656791/9877973172"
        },
    )
}
