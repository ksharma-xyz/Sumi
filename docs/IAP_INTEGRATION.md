# IAP Integration — pending work

`ProRepository` is the cross-platform contract the UI talks to for Sumi Pro
entitlement, price strings, restore, and purchase. Today only `DebugProRepository`
exists — a debug-toggle-driven stand-in. To ship paid Pro to users, replace
the `single<ProRepository>` binding in `di/AppModule.kt` with platform-specific
implementations.

## Cross-platform surface

```kotlin
interface ProRepository {
    fun isPro(): Flow<Boolean>
    fun observePrice(productId: String): Flow<String?>   // null while loading
    suspend fun restorePurchases(): Result<Unit>
    suspend fun purchase(productId: String): Result<Unit>
}
```

Product IDs are defined in `ProProducts`:
- `sumi_pro_yearly`
- `sumi_pro_monthly`

The exact same identifier strings must be configured in App Store Connect and
Play Console (auto-renewing subscriptions in a single subscription group).

## Android — Play Billing v6

1. Add `com.android.billingclient:billing-ktx` to `composeApp` Android source set.
2. Implement `PlayBillingProRepository` in `androidMain`:
   - Wrap `BillingClient` with a `connectionState` flow + reconnect-on-disconnect.
   - `queryProductDetails` for both product IDs on connect; cache results; emit
     `formattedPrice` from `subscriptionOfferDetails[0].pricingPhases.pricingPhaseList[0].formattedPrice`.
   - `queryPurchasesAsync` on connect to seed `isPro` flow; observe via
     `PurchasesUpdatedListener`.
   - `purchase()` builds a `BillingFlowParams` with the chosen offer token and
     calls `launchBillingFlow(activity, ...)` — needs an `Activity` reference
     (use a current-activity holder set from `MainActivity.onResume`).
   - `restorePurchases()` is `queryPurchasesAsync(SUBS) → acknowledge unacknowledged`.
3. Acknowledge purchases within 3 days or Google auto-refunds them.
4. Server-side validation: post `purchaseToken` + `productId` to a
   verification endpoint. Skip only if you accept revenue loss to fraud.

## iOS — StoreKit 2

1. Configure both products in App Store Connect under one subscription group.
2. Implement `StoreKitProRepository` in `iosMain` using `kotlinx.cinterop`:
   - `Product.products(for: [...])` (suspending) on init; expose
     `displayPrice` (already locale-formatted).
   - `Transaction.currentEntitlements` collected as a `Flow` for `isPro()`.
   - `purchase()` calls `product.purchase()` and handles
     `.success(.verified)` / `.success(.unverified)` / `.userCancelled`.
   - `restorePurchases()` calls `AppStore.sync()`.
   - Listen to `Transaction.updates` indefinitely in a long-running
     `viewModelScope`-equivalent so renewals + revocations propagate.
3. Add `StoreKit.framework` to the Xcode project.
4. App Store reviewers test with sandbox accounts — make sure price strings
   render in non-USD locales.

## DI binding

Replace in `composeApp/src/commonMain/.../di/AppModule.kt`:

```kotlin
single<ProRepository> { DebugProRepository(debug = get()) }
```

with `expect`/`actual` factories or a Koin `single` per platform module that
returns the real implementation in release builds and `DebugProRepository`
under `BuildKonfig.IS_DEBUG`.

## UI contract

- `PaywallScreen` already binds prices via `proRepo.observePrice(...)`. While
  the flow emits `null` it shows the localised "—" placeholder; reviewers
  should never see that in the App Store / Play Store sandbox builds.
- `Restore Purchase` button on the Zen tab + Paywall calls
  `proRepo.restorePurchases()` and surfaces a snackbar on failure.

## Compliance checklist (Apple + Google)

- [ ] Privacy policy + terms URLs (still placeholder URLs in
      `PaywallScreen.kt`).
- [ ] Restore Purchase is reachable from a non-paywall screen (Zen tab + Paywall — done).
- [ ] Subscription terms (length, auto-renew, cancellation) shown in-app
      before purchase.
- [ ] Price strings come from the store (no hardcoded currency / amount).
- [ ] No "lifetime" offer that conflicts with auto-renewing subscription metadata.
- [ ] Family Sharing flag in App Store Connect set deliberately.
- [ ] Server-side receipt validation (or accept the fraud risk in writing).
