package xyz.ksharma.sumi.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Debug stand-in for [ProRepository]. Entitlement is driven by the in-app
 * "Simulate Pro" toggle. Prices are `null` (the UI shows "—" placeholders)
 * because the real Play Billing / StoreKit wiring lands in a follow-up.
 *
 * Restore + purchase no-op successfully — useful for verifying UI flows
 * without a store account in the loop.
 */
class DebugProRepository(private val debug: DebugPreferences) : ProRepository {
    override fun isPro(): Flow<Boolean> = debug.observeSimulatePro()

    override fun observePrice(productId: String): Flow<String?> = flowOf(null)

    override suspend fun restorePurchases(): Result<Unit> = Result.success(Unit)

    override suspend fun purchase(productId: String): Result<Unit> {
        debug.setSimulatePro(true)
        return Result.success(Unit)
    }
}
