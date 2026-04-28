# Sumi — Pending Work

> Single source of truth for what's outstanding before v1.0 ships. Grouped by phase.
> Tick boxes as you complete; move stale items into "Done / superseded" at the bottom.

---

## ☀️ When you wake up — start here

Latest pass shipped (commits through `f249241`):

- ✅ Stats / Daily "0 puzzles solved" bug **fixed** (Flow-based prefs observation)
- ✅ Daily screen rebuilt per handoff (calendar grid + Today card + previous months)
- ✅ Stats screen rebuilt per handoff (hero + 2×2 THIS WEEK + Improvement chart + Personal Bests + Locked state)
- ✅ Per-difficulty best-time tracking + recent-solve-times rolling list (drives the chart)
- ✅ Win share image now includes the **completed Sudoku grid** + uses the real **logo_chop** stamp
- ✅ "MARKS" → "MISTAKES" relabel on Game HUD + Win share card (clearer to first-time players)
- ✅ Stronger selected-cell wash (red 8% → 20% light / 32% dark) via `BoardSelectionAlphas` tokens
- ✅ Sudoku grid borders fixed: dp-based stroke widths + perimeter inset so the right/bottom edges
  no longer look thinner than the interior box dividers (commit `f249241`)
- ✅ Debug tools: "Seed: 5-day streak" and "Open Win screen" buttons in Settings → Debug
- ✅ InkBleed component (weather + dark-mode aware) replaces static PNG bleed accents on Daily/Stats
- ✅ Game screen now has `verticalScroll` so landscape orientation + small phones don't clip the
  number pad (commit pending — bundled with the size revert)
- ✅ Reverted the 42dp / 26-28sp size bump back to the original 38dp / 22-24sp — was making the
  layout overflow viewports
- ✅ All compiles clean on iOS + Android, detekt green
- ✅ **Debug-build AdMob shield** — basic-ads' `InterstitialAd` and `RewardedAd` composables
  crash in debug when no real AdMob app is registered for the package (InterstitialAd
  hits `setListeners` before any ad is loaded; RewardedAd retry-storms on "Cannot determine
  request type"). All three ad placements (Win interstitial, Game idle interstitial, hint
  rewarded ad) now skip in debug — debug builds grant +1 hint instantly when the rewarded
  ad would have shown. **Production builds with real AdMob app + unit IDs serve ads
  normally** — the shield is `!BuildKonfig.IS_DEBUG` gated.

**What you need to do tomorrow morning before shipping** — these need a human / device / store account, can't be done from code:

1. **Sanity-test on a real device** (Android + iOS): solve a puzzle → check Stats counter increments,
   Daily calendar fills in today's cell, Win share image shows the grid + red chop seal
2. **Tap Settings → Debug → Seed: 5-day streak**, then open Stats — confirm Personal Bests + Improvement
   chart populate. Confirm Locked state is visible if Simulate Pro is **off**.
3. **Tap Settings → Debug → Open Win screen** — verify the result page renders end-to-end
4. **AdMob console — set up the Sumi app first (~10 min)**. The current `RewardedAd` failures
   ("Cannot determine request type") are because no AdMob app is registered for the
   `xyz.ksharma.sumi` package — Google's test ad service errors out without a registered
   association. Steps:
   - Register the Android app at https://apps.admob.com → get its real `~`-style app ID
   - Replace the test app ID in `androidApp/build.gradle.kts` debug + release `manifestPlaceholders["admobAppId"]`
   - Same for iOS — register the iOS app, replace the test `GADApplicationIdentifier` in `iosApp/Info.plist`
   - Generate real Banner / Interstitial / Rewarded units per platform; replace the test IDs in
     `composeApp/build.gradle.kts` `defaultConfigs("release")` block (debug can keep test IDs once
     the real app is registered)
5. **Privacy policy URL** — host the template at `app-release-playbooks/shared/PRIVACY_POLICY_TEMPLATE.md`
   on GitHub Pages or similar (5 min)
6. **App Store Connect record** — see `docs/SUBMISSION.md`. Likely needs
   `Sumi: Zen Sudoku` since plain `Sumi` is taken.
7. **Google Play Console record** — register the app, complete identity verification (1–3 days, do this NOW)
8. **Real device ATT prompt verification on iOS** — install via TestFlight, confirm the prompt fires once
9. **Sanity-check landscape orientation** — `verticalScroll` was just added to GameScreen, but worth
   eyeballing the actual landscape layout to see if the board + tools + numpad scroll cleanly.
10. **Decide IAP for v1.0**: ship with the "Simulate Pro" debug toggle only, or wire real Play Billing +
    StoreKit (~half-day per platform). I'd recommend deferring to v1.1 and shipping the ad-supported
    free tier first.

The detailed checklists below cover each of these in depth — links into `app-release-playbooks/`
for canonical steps.

---

## Phase 1 — Bug fixes + debug tooling (in progress)

- [x] Identify root cause: `StatsViewModel` and `DailyViewModel` read prefs **once at init** via suspend functions; never refresh after a solve. Confirmed in code: `getTotalPuzzlesSolved()` is a one-shot `store.data.first()` read.
- [ ] **Add Flow-based prefs accessors** to `SumiPreferences`: `observeStreak()`, `observeBestStreak()`, `observeTotalPuzzlesSolved()`, `observeSolveDays()`
- [ ] Implement Flow accessors in `DataStoreSumiPreferences` (DataStore is already Flow-based — just expose `store.data.map { ... }`)
- [ ] Update `StatsViewModel` to `combine` the four flows into a `StateFlow<StatsState>` via `stateIn`
- [ ] Update `DailyViewModel` to use `observeSolveDays()` + `observeStreak()` instead of the one-shot `getSolveDays()` / `getStreak()`
- [ ] **Debug tools** in Settings → Debug section:
   - "Seed: streak=5, totalPuzzles=20, last 5 days marked" — calls `prefs.seedSolveData(streak=5, total=20)` to fake history
   - "Open Win screen (sample)" — pushes `WinRoute(elapsedMs=420_000, mistakeCount=2, moveCount=37, difficulty="Medium")` so the screen can be QA'd without solving
- [ ] Verify on device: solve a puzzle → open Stats → counter is non-zero, streak shows correctly

---

## Phase 2 — Daily screen per `docs/handoff/screens/07_DAILY.md`

Current implementation is a 30-day heatmap; spec wants a full calendar.

- [ ] Header bar (← back, "Daily" title, Cormorant Italic 22sp)
- [ ] Month hero — current month name + "X of Y days solved"
- [ ] **Calendar grid** — 7 cols, M T W T F S S header, 40×40dp day-number cells
   - Solved → seal-red dot below number, number in Cormorant Italic 15sp ink
   - Today → Cormorant Italic 17sp weight 600 + 1.5dp seal ring around cell
   - Future → Inter Regular 12sp inkSoft @ 40%
   - Unsolved past → 70% alpha
- [ ] **Today card** — 88dp tall, 1dp ink-8% border, no fill
   - Top: `二 · Medium · #1420` (kanji + difficulty + puzzle id)
   - Bottom: `Unstarted` / `In progress · 4:32` + `Play →` / `Continue →`
- [ ] **Previous months** section — collapsible rows per past month
- [ ] Background ink bleed → use the new `InkBleed` component (top-right, alpha auto-handled)
- [ ] Acceptance checklist (in handoff doc): all 8 items pass

---

## Phase 3 — Stats screen per `docs/handoff/screens/08_STATS.md`

Current implementation has the hero + a 3-cell summary; spec wants 4 sections.

- [ ] Header bar (← back, "Practice Log" title)
- [x] Hero number 88sp Cormorant Italic + subtitle ✓ already done
- [ ] **Switch hero ink-bleed** to `InkBleed` component, 200dp behind hero, alpha 0.08 (token-driven)
- [ ] **THIS WEEK** section — 2×2 grid (Solved / Avg time / Streak / Level)
   - 160×96dp cells, 8dp radius, 1dp ink-8% border
   - Top value Cormorant Italic 26sp ink (or kanji 28sp Shippori for Level)
   - Bottom label Inter Medium 11sp UPPER WIDEST inkSoft
- [ ] **IMPROVEMENT** card — 240dp tall, full-width SVG/Canvas line chart
   - 30-day average solve time
   - Stroke ink 2dp, no fill, no grid, no dots
- [ ] **PERSONAL BESTS** list — 5 rows for Easy/Medium/Hard/Master/Edo
   - Kanji 28sp + label Inter Medium 14sp on left, best time Cormorant Italic 22sp on right
   - 56dp row, 0.5dp ink-8% divider between rows
- [ ] **Locked state** for Pro-only sections (free user beyond 7 days)
   - BG-4 ink-night background
   - Hero replaced with kanji `錠` (lock) 96sp Shippori goldIvory
   - "Full history is part of Sumi Pro." + "See Pro" CTA → paywall
- [ ] Track `bestTimeFor(difficulty: Difficulty)` in prefs — currently not stored anywhere
- [ ] Track per-puzzle elapsed times (rolling 30 entries) for the improvement chart — needs new prefs key

---

## Phase 4 — Win screen polish

- [ ] Share image should include the **filled Sudoku grid** in the share card
   - Plumb solution through `WinRoute` (add `solution: String` field, 81 chars)
   - New `SudokuThumbnail` composable in `design/components/`
   - Render between hero and stats row in `WinShareCard`
- [ ] Replace `SealComplete(120dp)` with `Image(painter = painterResource(Res.drawable.logo_chop))` — 120dp red carved-kanji stamp from the handoff vector

---

## Phase 5 — Android banner ad not showing

- [ ] Capture logcat: `adb logcat | grep -iE "ads|gma|admob"` while app is launched
- [ ] If "Starting ad request" never logs → composable not entering composition (gating bug or `bottomBanner` slot collapses to 0 height)
- [ ] If logged but visible banner is missing → reserve explicit height on the slot:
   ```kotlin
   Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(60.dp)) { bottomBanner() }
   ```
- [ ] Test on real device with internet (test ad unit IDs require connectivity)

---

## Phase 6 — Pre-submission housekeeping

### Accounts & credentials

- [ ] **AdMob** — register real apps (Android + iOS), generate real Banner / Interstitial / Rewarded unit IDs
   - Replace test IDs in `composeApp/build.gradle.kts` `defaultConfigs("release")` block
   - Replace `manifestPlaceholders["admobAppId"]` for the release build type
   - Replace `Info.plist` `GADApplicationIdentifier`
- [ ] **App Store Connect** — register the app (likely `Sumi: Zen Sudoku` since `Sumi` is taken), Apple Team ID + cert + provisioning + ASC API key — see `docs/SUBMISSION.md`
- [ ] **Google Play Console** — create app, identity verification, service account JSON for CI

### IAP — one-time Pro purchase, no subscription

- [ ] **Decision locked**: native Play Billing (Android) + StoreKit (iOS). **No RevenueCat, no subscription tier.**
- [ ] Single product: `sumi_pro` — one-time purchase, suggested ~$3.99
- [ ] Wire Play Billing in `androidMain` (BillingClient v8+)
- [ ] Wire StoreKit 2 in `iosMain` (`Product.purchase()`)
- [ ] Replace `DebugProRepository` with a `BillingProRepository` that reads real entitlement state
- [ ] Update `PaywallScreen.kt` copy: remove subscription pricing tiers, show single "Unlock Sumi Pro · $3.99" CTA
- [ ] Add to App Privacy → Identifiers → Purchase History

### Legal URLs

- [ ] Host privacy policy + support page (GitHub Pages on a `sumi.app` or `ksharma-xyz.github.io/sumi-site` URL)
- [ ] Update `PaywallScreen.kt:53–54` placeholder URLs

### Pro feature roadmap (defer to v1.1+)

- [x] Export PDF puzzle books — Android done; iOS done (via `IosPuzzleBookExporter`)
- [ ] **The Salon** (weekly global leaderboard) — requires backend server, post-launch
- [ ] **Gold / Indigo / Edo themes** — theme tokens exist, UI selection pending
- [ ] **Full 600-quote library** — currently 13 free + 20 Pro; content work needed
- [ ] **iCloud / Google Drive sync** — explicitly out of v1.0 scope

### iOS setup

- [x] `BasicAds.Initialize()` wired in `App()`
- [x] AdMob iOS App ID + SKAdNetwork in `Info.plist`
- [x] `NSUserTrackingUsageDescription` (ATT) in `Info.plist`
- [ ] `PrivacyInfo.xcprivacy` (iOS 17+ Privacy Manifest) — add to `iosApp/iosApp/`
- [ ] Verify ATT prompt appears on first launch (real device test)

### Final pre-submission

- [ ] Final accessibility pass (Dynamic Type, VoiceOver, contrast)
- [ ] App Store screenshots — 5–7 × iPhone 17 Pro Max + iPad if shipping iPad (see `app-release-playbooks/ios/SCREENSHOTS_SPEC.md`)
- [ ] Play Store listing assets — 512×512 icon, 1024×500 feature graphic, 5+ phone screenshots
- [ ] All TODOs in this file resolved or moved to v1.1

---

## Done / superseded

- ~~RevenueCat integration~~ — superseded; native Play Billing + StoreKit only
- ~~Annual + monthly subscription tiers~~ — superseded; one-time purchase only
- ~~iOS PDF export blocked~~ — `IosPuzzleBookExporter` shipped commit `603a541`
- ~~Koin not initialized on iOS crash~~ — fixed commit `353b328`
- ~~Mistakes ending the game~~ — removed; mistakes are tracked but no longer end play
- ~~`BuildConfig.DEBUG` only on Android~~ — replaced with `BuildKonfig.IS_DEBUG` cross-platform
