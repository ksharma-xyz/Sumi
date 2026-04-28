package xyz.ksharma.sumi.ads

import app.lexilabs.basic.ads.AdUnitId
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import xyz.ksharma.sumi.BuildKonfig

/**
 * Cross-platform ad unit IDs sourced from BuildKonfig.
 *
 * Defined once here so every screen that shows an ad uses the same constant — and so
 * swapping test IDs for real ones is a one-line change in `composeApp/build.gradle.kts`,
 * not a hunt across the codebase.
 */
@OptIn(DependsOnGoogleMobileAds::class)
object AdUnits {
    // basic-ads' AdUnitId.autoSelect returns a platform-resolved String — not an AdUnitId
    // type. Composables (BannerAd / InterstitialAd / RewardedAd) accept the String directly.
    val Banner: String = AdUnitId.autoSelect(
        androidAdUnitId = BuildKonfig.AD_BANNER_ANDROID,
        iosAdUnitId = BuildKonfig.AD_BANNER_IOS,
    )
    val Interstitial: String = AdUnitId.autoSelect(
        androidAdUnitId = BuildKonfig.AD_INTERSTITIAL_ANDROID,
        iosAdUnitId = BuildKonfig.AD_INTERSTITIAL_IOS,
    )
    val Rewarded: String = AdUnitId.autoSelect(
        androidAdUnitId = BuildKonfig.AD_REWARDED_ANDROID,
        iosAdUnitId = BuildKonfig.AD_REWARDED_IOS,
    )
}
