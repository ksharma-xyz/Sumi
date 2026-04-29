# Sumi — Publish Checklist

Everything that needs to happen on **your** end before tapping Submit on the App
Store / Play Console. Code-side work is done; what's left is store config,
real billing, hosting, and a sanity test.

Mark items off as you go.

---

## 1. Real-device sanity test (do this first)

Run a release build on real hardware, golden-path + a few edge cases.

### Android
```bash
./gradlew :androidApp:installRelease
```
Or use Android Studio's **Run → Edit Configurations → Build Variant: release**.

- [ ] Onboarding flows through to Home (gold dot visible bottom-centre of logo)
- [ ] Daily Today card doesn't clip the Play CTA at large system font size
      (Settings → Display → Font size: largest)
- [ ] Master / Edo first-time → loading overlay appears with enso draw + petals,
      then the puzzle. Resume → no flash.
- [ ] Solve a puzzle → 3-second petal shower → Win screen. Time, mistakes,
      moves all correct. Stats updated.
- [ ] Win → "Share Result" opens system share sheet with the card image.
- [ ] Win → "Next Practice" → new puzzle, no 0-min flicker.
- [ ] Settings → Simulate Pro toggle → Zen tab opens, ads gone.
- [ ] Zen → Design Zen Sudoku → set mix (capped at 15/diff, 50 total) → Generate
      → crafting screen → result reveal → tap to share PDF.
- [ ] PDF cover says "Zen Sudoku" not "Puzzle Book". Per-page wordmark is italic
      serif "Sumi", not uppercase sans.
- [ ] Stay on Home for 1 min → first ambient petal gust + invite glow.
- [ ] Tap "Pass Sumi along" → share sheet with INVITE_TEXT.
- [ ] Toggle Simulate Pro off → Zen tab opens paywall with single "Unlock Sumi
      Pro" button + "One-time purchase. No subscriptions." sub-line.
- [ ] AdMob banner + interstitial appear (test ad IDs in debug, real IDs in release).

### iOS
Open `iosApp/iosApp.xcodeproj` → select a real device → Run.

- [ ] First launch shows ATT prompt (iOS 14.5+).
- [ ] Same golden path as Android.
- [ ] Confirm GoogleMobileAds linked via SPM (no missing-symbol crash on first
      ad load).

If any of these fails, tell me before submitting.

---

## 2. Hosting (privacy / terms / invite landing)

The paywall and the "Pass Sumi along" copy reference URLs that need to exist.

- [ ] Host a Privacy Policy page → replace `URL_PRIVACY` in
      `composeApp/.../paywall/PaywallScreen.kt:60`.
- [ ] Host a Terms of Service page → replace `URL_TERMS` in
      `composeApp/.../paywall/PaywallScreen.kt:61`.
- [ ] Replace the placeholder URL `https://sumi.app` in `INVITE_TEXT`
      (`composeApp/.../navigation/entries/HomeEntry.kt:35`) with the real
      App Store / Play Store listing URLs once the apps are live. A simple
      landing page that links to both stores is the cleanest answer.

Tip: a one-page Markdown on GitHub Pages or a Notion public page is enough.

---

## 3. App Store Connect setup (iOS)

- [ ] Create the app in App Store Connect.
- [ ] Bundle ID matches `iosApp/iosApp.xcodeproj` settings.
- [ ] Add **non-consumable IAP** with product ID exactly: `sumi_pro_lifetime`
      (matches `xyz.ksharma.sumi.preferences.ProProducts.LIFETIME`).
- [ ] Set price tier per region.
- [ ] Add at least one App Store screenshot per required device class.
- [ ] App Privacy questionnaire — declare AdMob data collection (Identifier
      for Advertisers, Coarse Location, Device ID).
- [ ] Confirm `NSUserTrackingUsageDescription` is in `iosApp/iosApp/Info.plist`
      (it already is — value: "This identifier helps show you relevant ads…").
- [ ] App Review notes: include a sandbox test account that has and hasn't
      purchased Pro, so reviewers can test both states.

---

## 4. Play Console setup (Android)

- [ ] Create the app in Play Console.
- [ ] Package name matches `androidApp` Gradle config.
- [ ] **In-app product** with product ID `sumi_pro_lifetime`, type
      *Managed product (one-time)*. Set base price.
- [ ] Build a signed App Bundle: `./gradlew :androidApp:bundleRelease`.
- [ ] Upload to Internal testing track first, install on a tester device.
- [ ] Data Safety form — declare AdMob (Advertising ID, Crash logs).
- [ ] Content rating — should land at ESRB Everyone / PEGI 3.
- [ ] App Access — provide credentials if needed.
- [ ] Confirm `AD_ID` permission is declared (already in
      `androidApp/src/main/AndroidManifest.xml`).

---

## 5. Real billing wiring (currently stubbed)

The only `ProRepository` binding is `DebugProRepository`, which fakes
purchase/restore. To take real money you need actual store integrations.

Full step-by-step + compliance checklist is in
[`docs/IAP_INTEGRATION.md`](IAP_INTEGRATION.md). Summary:

- [ ] **Android**: implement `PlayBillingProRepository` in `androidMain` using
      `com.android.billingclient:billing-ktx`. Wire `acknowledgePurchase` (Google
      auto-refunds within 3 days otherwise).
- [ ] **iOS**: implement `StoreKitProRepository` in `iosMain` using StoreKit 2
      via cinterop. Listen to `Transaction.updates` for renewals/revocations.
- [ ] In `di/AppModule.kt`, swap the `single<ProRepository>` line per platform
      (debug build can keep `DebugProRepository`, release uses real impl).
- [ ] Optional but recommended: server-side receipt validation. Without it,
      receipt forgery costs you money.

---

## 6. Crashlytics post-publish

- [ ] After first publish, upload native debug symbols so Crashlytics
      stack traces deobfuscate:
  - Android: `firebase crashlytics:symbols:upload`
  - iOS: Xcode Organizer → Distribute App → enable upload symbols
- [ ] Trigger a smoke-test non-fatal crash from a hidden Settings option
      (or just wait for the first real one) and verify it lands in the
      Crashlytics dashboard, labelled by ViewModel class.

---

## 7. Build commands cheat-sheet

```bash
# Quality gate (compile + lint)
./qa.sh

# Android
./gradlew :androidApp:assembleRelease            # APK for sideload testing
./gradlew :androidApp:bundleRelease              # AAB for Play Console upload

# iOS
# Xcode → Product → Archive → Distribute App
```

---

## 8. After Submit

- [ ] App Store review usually 24–48h.
- [ ] Play Store review usually 1–7 days for first release (longer than updates).
- [ ] Set up release-notes template for future versions.
- [ ] Watch Crashlytics for the first 48h after launch.
- [ ] Monitor App Store Connect Sales + Play Console Statistics for IAP signal.

---

When everything in §1 passes on real devices, tell me and I'll help with §3–§5.
