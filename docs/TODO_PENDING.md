# Pending — requires your action before release

## Accounts & credentials

- [ ] **AdMob** — register app in Google AdMob console, get Android App ID + iOS App ID
  - Replace test ID in `androidApp/build.gradle.kts` → `manifestPlaceholders["admobAppId"]`
  - Add iOS App ID to `iosApp/Info.plist` → `GADApplicationIdentifier`
  - Create one Interstitial ad unit per platform; add the unit IDs to the billing config
- [ ] **RevenueCat** — create account + project at revenuecat.com
  - Register Android app (package `xyz.ksharma.sumi`) and iOS app (bundle ID)
  - Create entitlement named **`pro`**
  - Link products (see below) to the entitlement
  - Share the Android and iOS API public keys so the SDK can be wired in
- [ ] **Google Play Console** — create the in-app products:
  - Annual subscription: `sumi_pro_annual` · $29/year
  - Monthly subscription: `sumi_pro_monthly` · $3.99/month
- [ ] **App Store Connect** — create the in-app purchases:
  - Annual subscription: $29/year
  - Monthly subscription: $3.99/month
  - Submit for Apple review (IAPs reviewed separately — do this early)

## Legal & legal URLs

- [ ] **Privacy Policy** — write and host; replace `https://example.com/privacy` in `PaywallScreen.kt:53`
- [ ] **Terms of Service** — write and host; replace `https://example.com/terms` in `PaywallScreen.kt:54`

## Pro features — post-launch roadmap

- [ ] **Export PDF puzzle books** — requires PDF generation library; design spec in PRODUCT.md
- [ ] **The Salon** (weekly global leaderboard) — requires backend server + weekly reset job
- [ ] **Gold, Indigo, Edo themes** — theme tokens exist; UI selection in Settings is pending
- [ ] **Full 600-quote library** — only 13 free quotes ship today; Pro quote library needs content work
- [ ] **iCloud / Google Drive sync** — removed from v1 scope; post-launch

## iOS setup

- [ ] `GADMobileAds.sharedInstance().start()` in iOS `@main` / `AppDelegate`
- [ ] Add AdMob iOS App ID to `Info.plist`
- [ ] SKAdNetwork entries in `Info.plist` (required by Apple since iOS 14)

## Before submission

- [ ] Replace all placeholder screens with real content (no "Demo" or stub screens shipped)
- [ ] Final accessibility pass (Dynamic Type, VoiceOver)
- [ ] App Store screenshots + metadata
- [ ] Google Play store listing
