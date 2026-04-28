package xyz.ksharma.sumi.preferences

import kotlinx.coroutines.flow.Flow

/**
 * Sumi Pro entitlement + IAP surface. Implementations bridge to the platform
 * billing layer (Play Billing on Android, StoreKit 2 on iOS). Until the real
 * billing layer is wired up, [DebugProRepository] satisfies this contract with
 * a debug-toggle-driven entitlement and `null` prices.
 *
 * Pricing strings (currency symbol, amount, locale formatting) MUST come from
 * the store APIs — never hardcode prices in the UI layer.
 */
interface ProRepository {
    /** Emits the current Pro entitlement state. True once a valid purchase is active. */
    fun isPro(): Flow<Boolean>

    /**
     * Emits the localised price string for [productId] (e.g. "$3.99/month") or
     * `null` if the store hasn't returned a product yet (loading or unavailable).
     * The UI should hide / placeholder the row while null.
     */
    fun observePrice(productId: String): Flow<String?>

    /**
     * Triggers the platform's restore-purchases flow. Returns a [Result] that
     * encapsulates store errors (network, no-purchases-found, cancelled). The
     * entitlement [Flow] from [isPro] will emit the new state on success.
     */
    suspend fun restorePurchases(): Result<Unit>

    /**
     * Launches the platform purchase flow for [productId]. Returns a [Result]
     * that encapsulates the user cancelling, billing errors, etc.
     */
    suspend fun purchase(productId: String): Result<Unit>
}

/** Stable product identifiers used across stores. Real product IDs are configured per-store. */
object ProProducts {
    const val YEARLY = "sumi_pro_yearly"
    const val MONTHLY = "sumi_pro_monthly"
}
