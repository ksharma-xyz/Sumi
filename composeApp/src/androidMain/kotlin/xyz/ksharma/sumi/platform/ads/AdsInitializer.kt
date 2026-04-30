package xyz.ksharma.sumi.platform.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

/**
 * Initialise the Google Mobile Ads SDK on Android. Must run once from
 * [android.app.Application.onCreate], BEFORE any AdView / BannerAd composes.
 * Without this call, AdView.loadAd() silently fails to fetch — that was why
 * Android banners were invisible while iOS was working (iOS init is handled
 * elsewhere by the basic-ads library).
 *
 * Mirrors the Huezoo `initMobileAds` pattern.
 */
fun initMobileAds(context: Context) {
    MobileAds.setRequestConfiguration(
        RequestConfiguration.Builder()
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build(),
    )
    MobileAds.initialize(context)
}
