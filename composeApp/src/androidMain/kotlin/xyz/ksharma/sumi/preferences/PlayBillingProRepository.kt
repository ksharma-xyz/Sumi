package xyz.ksharma.sumi.preferences

import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Production Play Billing implementation of [ProRepository].
 *
 * Uses Play Billing Library 7.x (one-time products / INAPP type).
 * [BillingClient.launchBillingFlow] requires an [android.app.Activity], so the
 * call site must keep [ActivityHolder] current — [MainActivity] does this via
 * onResume / onDestroy.
 *
 * Entitlement is persisted in DataStore via [ProPreferences] so it survives
 * process restarts without a Play round-trip.
 */
class PlayBillingProRepository(
    context: Context,
    private val prefs: ProPreferences,
) : ProRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _isPro = MutableStateFlow(false)
    private val _price = MutableStateFlow<String?>(null)
    private var cachedProductDetails: ProductDetails? = null
    private var purchaseContinuation: ((Result<Unit>) -> Unit)? = null

    private val purchasesListener = PurchasesUpdatedListener { result, purchases ->
        handlePurchasesUpdate(result, purchases)
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .build()

    init {
        scope.launch { prefs.observeIsPro().collect { _isPro.value = it } }
        connect()
    }

    // ── ProRepository ─────────────────────────────────────────────────────────

    override fun isPro(): Flow<Boolean> = _isPro.asStateFlow()
    override fun observePrice(productId: String): Flow<String?> = _price.asStateFlow()

    override suspend fun purchase(productId: String): Result<Unit> {
        val activity = ActivityHolder.get()
            ?: return Result.failure(Exception("No active screen — purchase unavailable."))

        val product = cachedProductDetails ?: run {
            fetchProductDetails()
            cachedProductDetails
        } ?: return Result.failure(Exception("Product '$productId' is not available from the Play Store."))

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .build(),
                ),
            )
            .build()

        return suspendCancellableCoroutine { cont ->
            purchaseContinuation = { result -> if (cont.isActive) cont.resume(result) }
            // launchBillingFlow must be called on the main thread.
            scope.launch {
                val result = billingClient.launchBillingFlow(activity, params)
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    val cb = purchaseContinuation.also { purchaseContinuation = null }
                    if (cont.isActive) cb?.invoke(Result.failure(Exception(result.debugMessage)))
                }
            }
            cont.invokeOnCancellation { purchaseContinuation = null }
        }
    }

    override suspend fun restorePurchases(): Result<Unit> {
        queryExistingPurchases()
        return Result.success(Unit)
    }

    // ── Purchase updates ───────────────────────────────────────────────────────

    private fun handlePurchasesUpdate(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        scope.launch {
                            acknowledgePurchase(purchase)
                            prefs.setIsPro(true)
                            _isPro.value = true
                        }
                    }
                }
                val cb = purchaseContinuation.also { purchaseContinuation = null }
                cb?.invoke(Result.success(Unit))
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                val cb = purchaseContinuation.also { purchaseContinuation = null }
                cb?.invoke(Result.failure(Exception("Purchase cancelled.")))
            }
            else -> {
                val cb = purchaseContinuation.also { purchaseContinuation = null }
                cb?.invoke(Result.failure(Exception(result.debugMessage)))
            }
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        queryExistingPurchases()
                        fetchProductDetails()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Reconnect on the next purchase attempt — no aggressive retry loop.
            }
        })
    }

    private suspend fun fetchProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(ProProducts.LIFETIME)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()

        val result = withContext(Dispatchers.IO) { billingClient.queryProductDetails(params) }
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val details = result.productDetailsList?.firstOrNull()
            cachedProductDetails = details
            _price.value = details?.oneTimePurchaseOfferDetails?.formattedPrice
        }
    }

    private suspend fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val result = withContext(Dispatchers.IO) { billingClient.queryPurchasesAsync(params) }
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val hasPro = result.purchasesList.any { purchase ->
                purchase.products.contains(ProProducts.LIFETIME) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            if (hasPro) {
                prefs.setIsPro(true)
                _isPro.value = true
            }
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            withContext(Dispatchers.IO) { billingClient.acknowledgePurchase(params) }
        }
    }
}
