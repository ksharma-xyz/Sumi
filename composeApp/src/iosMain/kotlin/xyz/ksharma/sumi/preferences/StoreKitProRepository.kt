package xyz.ksharma.sumi.preferences

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.StoreKit.SKPayment
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState
import platform.StoreKit.SKProduct
import platform.StoreKit.SKProductsRequest
import platform.StoreKit.SKProductsRequestDelegateProtocol
import platform.StoreKit.SKProductsResponse
import platform.StoreKit.SKRequest
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * Production StoreKit 1 implementation of [ProRepository].
 *
 * Kotlin/Native does not allow a class to mix an ObjC supertype (NSObject)
 * with a Kotlin interface, so the SKPaymentTransactionObserver logic lives in
 * [TransactionObserver] — a dedicated NSObject subclass — while this class
 * is a plain Kotlin class that implements [ProRepository] and delegates all
 * store callbacks through it.
 *
 * Lifecycle:
 * - Registers [TransactionObserver] with [SKPaymentQueue] on init so
 *   unfinished transactions from previous sessions are delivered at launch.
 * - Fetches the localised price in the background; [observePrice] emits null
 *   until the App Store responds (UI shows a placeholder in the meantime).
 * - Entitlement is persisted in DataStore via [ProPreferences] so it survives
 *   process restarts without a round-trip to the store.
 */
class StoreKitProRepository(private val prefs: ProPreferences) : ProRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _isPro = MutableStateFlow(false)
    private val _price = MutableStateFlow<String?>(null)
    private var cachedProduct: SKProduct? = null
    private var lastInvalidIds: Set<String> = emptySet()
    // Both fields kept as strong refs: SKProductsRequest.delegate is weak on iOS,
    // and Kotlin/Native's tracing GC collects isolated self-retain cycles.
    private var productFetchDelegate: ProductFetchDelegate? = null
    private var activeProductRequest: SKProductsRequest? = null

    private val observer = TransactionObserver(
        onSuccess = {
            scope.launch {
                prefs.setIsPro(true)
                _isPro.value = true
            }
        },
    )

    init {
        SKPaymentQueue.defaultQueue().addTransactionObserver(observer)
        scope.launch { prefs.observeIsPro().collect { _isPro.value = it } }
        scope.launch { fetchProduct(ProProducts.LIFETIME) }
    }

    // ── ProRepository ────────────────────────────────────────────────────────

    override fun isPro(): Flow<Boolean> = _isPro.asStateFlow()
    override fun observePrice(productId: String): Flow<String?> = _price.asStateFlow()

    override suspend fun purchase(productId: String): Result<Unit> {
        if (!SKPaymentQueue.canMakePayments()) {
            return Result.failure(Exception("Purchases are not allowed on this device."))
        }
        val product = cachedProduct ?: run {
            fetchProduct(productId)
            cachedProduct
        } ?: return Result.failure(
            Exception(
                if (productId in lastInvalidIds) {
                    "Product ID '$productId' was rejected (invalid ID or not linked to this app)"
                } else {
                    "Product '$productId' not returned (check Paid Apps agreement and product status)"
                },
            ),
        )

        return suspendCancellableCoroutine { cont ->
            observer.purchaseContinuation = { result -> if (cont.isActive) cont.resume(result) }
            SKPaymentQueue.defaultQueue().addPayment(SKPayment.paymentWithProduct(product))
            cont.invokeOnCancellation { observer.purchaseContinuation = null }
        }
    }

    override suspend fun restorePurchases(): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            observer.restoreContinuation = { result -> if (cont.isActive) cont.resume(result) }
            SKPaymentQueue.defaultQueue().restoreCompletedTransactions()
            cont.invokeOnCancellation { observer.restoreContinuation = null }
        }

    // ── Price fetching ────────────────────────────────────────────────────────

    private suspend fun fetchProduct(productId: String) {
        suspendCancellableCoroutine { cont ->
            val delegate = ProductFetchDelegate { product, invalidIds ->
                lastInvalidIds = invalidIds
                cachedProduct = product
                _price.value = product?.formattedPrice()
                productFetchDelegate = null
                activeProductRequest = null
                if (cont.isActive) cont.resume(Unit)
            }
            productFetchDelegate = delegate
            val request = SKProductsRequest(productIdentifiers = setOf(productId))
            activeProductRequest = request
            request.delegate = delegate
            request.start()
            cont.invokeOnCancellation {
                request.cancel()
                productFetchDelegate = null
                activeProductRequest = null
            }
        }
    }
}

// ── NSObject subclasses (ObjC protocol implementations) ──────────────────────

private val TX_PURCHASED = SKPaymentTransactionState.SKPaymentTransactionStatePurchased
private val TX_RESTORED = SKPaymentTransactionState.SKPaymentTransactionStateRestored
private val TX_FAILED = SKPaymentTransactionState.SKPaymentTransactionStateFailed
// ObjC constant SKErrorPaymentCancelled = 2
private const val SK_ERROR_PAYMENT_CANCELLED = 2

private class TransactionObserver(
    private val onSuccess: () -> Unit,
) : NSObject(), SKPaymentTransactionObserverProtocol {

    var purchaseContinuation: ((Result<Unit>) -> Unit)? = null
    var restoreContinuation: ((Result<Unit>) -> Unit)? = null

    override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
        updatedTransactions.filterIsInstance<SKPaymentTransaction>().forEach { tx ->
            when (tx.transactionState) {
                TX_PURCHASED, TX_RESTORED -> {
                    queue.finishTransaction(tx)
                    onSuccess()
                    val cb = purchaseContinuation.also { purchaseContinuation = null }
                    cb?.invoke(Result.success(Unit))
                }
                TX_FAILED -> {
                    val isCancelled = tx.error?.code?.toInt() == SK_ERROR_PAYMENT_CANCELLED
                    queue.finishTransaction(tx)
                    val cb = purchaseContinuation.also { purchaseContinuation = null }
                    cb?.invoke(
                        if (isCancelled) Result.success(Unit)
                        else Result.failure(Exception("Something went wrong — please try again.")),
                    )
                }
                else -> { /* purchasing / deferred — wait for next callback */ }
            }
        }
    }

    override fun paymentQueueRestoreCompletedTransactionsFinished(queue: SKPaymentQueue) {
        val cb = restoreContinuation.also { restoreContinuation = null }
        cb?.invoke(Result.success(Unit))
    }

    override fun paymentQueue(
        queue: SKPaymentQueue,
        restoreCompletedTransactionsFailedWithError: NSError,
    ) {
        val cb = restoreContinuation.also { restoreContinuation = null }
        cb?.invoke(Result.failure(Exception("Something went wrong — please try again.")))
    }
}

private class ProductFetchDelegate(
    private val onResult: (SKProduct?, Set<String>) -> Unit,
) : NSObject(), SKProductsRequestDelegateProtocol {

    override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
        val product = didReceiveResponse.products.filterIsInstance<SKProduct>().firstOrNull()
        val invalidIds = didReceiveResponse.invalidProductIdentifiers.filterIsInstance<String>().toSet()
        onResult(product, invalidIds)
    }

    override fun request(request: SKRequest, didFailWithError: NSError) {
        onResult(null, emptySet())
    }
}

private fun SKProduct.formattedPrice(): String? {
    val formatter = NSNumberFormatter()
    formatter.numberStyle = NSNumberFormatterCurrencyStyle
    formatter.locale = priceLocale
    return formatter.stringFromNumber(price)
}
