package xyz.ksharma.sumi

/**
 * Single source of truth for outbound URLs the app references.
 *
 * All values are placeholders right now — replace with the real listing URLs
 * once the app is published. The store URLs are used by the Home "Pass Sumi
 * along" share text; the legal URLs by the Paywall footer; the landing URL by
 * marketing copy that needs a generic destination.
 */
object AppLinks {
    const val APP_STORE_URL = "https://example.org/sumi-ios"
    const val PLAY_STORE_URL = "https://example.org/sumi-android"
    const val LANDING_URL = "https://example.org/sumi"
    const val PRIVACY_URL = "https://example.org/sumi/privacy"
    const val TERMS_URL = "https://example.org/sumi/terms"
}
