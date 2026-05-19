package xyz.ksharma.sumi

/**
 * Single source of truth for outbound URLs the app references.
 *
 * The App Store URL is used by the Home/Stats "Pass Sumi along" share text;
 * the legal URLs by the Paywall footer; the landing URL by marketing copy
 * that needs a generic destination. Sumi is iOS-only — there is no Play
 * Store listing.
 */
object AppLinks {
    const val APP_STORE_URL = "https://apps.apple.com/us/app/sumi-zen-sudoku/id6764288103"
    const val LANDING_URL = "https://ksharma-xyz.github.io/Sumi/"
    const val PRIVACY_URL = "https://ksharma-xyz.github.io/Sumi/privacy-policy/"
    const val TERMS_URL = "https://ksharma-xyz.github.io/Sumi/privacy-policy/"
}
