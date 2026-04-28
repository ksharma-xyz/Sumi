# Sumi — Build Status

Last updated: 2026-04-25

## Done

### Game Engine
- [x] Puzzle generation (seed-based, deterministic per difficulty + date)
- [x] 5 difficulty levels: Easy / Medium / Hard / Master / Edo (一二三四五)
- [x] Board state machine: select, enter, erase, undo, hint, toggle-notes
- [x] Mistake counting (max 3 marks → game over)
- [x] Hints (3 per puzzle, marks cell as given)
- [x] Notes mode (per-cell pencil marks, cleared on digit entry)
- [x] Timer (1s ticks, stops on win/game-over; separate flow → no board recompose)
- [x] House completion detection: completedRows / completedCols / completedBoxes / completedDigits
- [x] 36 unit tests passing (`./gradlew :game:testAndroidHostTest`)

### Screens
- [x] Splash — animated enso logo, sakura petals, routes to onboarding or home
- [x] Onboarding — 4-slide pager, persisted via DataStore (won't re-show)
- [x] Home — streak card, daily quote, 5 difficulty tiles (incl. Edo), settings icon
- [x] Game — 9×9 board, timer, pause overlay, game-over overlay, number pad, tools (undo/notes/erase/hint)
- [x] Win — completion seal, time/marks/streak stats, difficulty label, next-puzzle + home buttons
- [x] Daily — 30-day heatmap (solved/today/empty), streak summary, legend
- [x] Stats — total puzzles solved (all-time), streak, best streak, days played
- [x] Settings — view licenses link
- [x] Licenses — typeface (5) + library (7) credits with license types
- [x] Paywall — "Sumi Pro" hero, feature rows, pricing UI (no purchase logic)

### Animations & UX
- [x] Aurora sweep on completed row / column (1200ms)
- [x] Aurora sweep on completed 3×3 box (1400ms)
- [x] Cell fade-in on digit entry (120ms)
- [x] Spring scale animation on user-placed digits (0.80→1f, MediumBouncy)
- [x] Same-digit highlight, unit (row/col/box) highlight, selected-cell highlight
- [x] Error cells shown in red
- [x] Pause overlay (resumes on button, hides board)
- [x] Game-over overlay (3 marks → "誤")

### Architecture & DI
- [x] ViewModel at Entry level only (screens are pure composables)
- [x] Business logic in VM — no calculations in composables
- [x] Immutable `List<T>` / `Set<T>` for all state (no mutableStateListOf)
- [x] `rememberSaveable` for primitive UI flags (paused, gameOver)
- [x] `rememberUpdatedState` for pointer-input callbacks (stable gesture detector)
- [x] Koin DI: SplashVM, GameVM, HomeVM, WinVM, DailyVM, StatsVM all registered

### Persistence (DataStore)
- [x] Onboarding seen flag
- [x] Current streak (consecutive days)
- [x] Best streak (all-time)
- [x] Last solve epoch day (for streak continuity)
- [x] Solve days set (for 30-day heatmap)
- [x] Total puzzles solved (all-time counter, incremented per win)

### Navigation
- [x] Bottom nav: Play / Daily / Stats / Zen (Paywall)
- [x] Full game flow: Home → Game → Win → Home
- [x] Settings → Licenses sub-flow
- [x] All 10 routes wired with real Entries

---

## Pending

### Phase 2 — Polish

- [x] **Haptics** — `HapticEngine` expect/actual; `tick` / `confirm` / `error` / `win` levels
  - Android: `View.performHapticFeedback` (no VIBRATE permission)
  - iOS: `UIImpactFeedbackGenerator` (light/medium) + `UINotificationFeedbackGenerator`
  - Wired at `GameEntry` (all callbacks) and `WinEntry` (win event)

- [x] **Number pad disabled when digit is complete** — `enabled = remaining > 0` on the
  clickable modifier; exhausted digits are visually dimmed and not tappable

- [x] **Digit entry animation** — spring scale (0.80→1f, MediumBouncy) + alpha fade (160ms)
  on user-placed digits; given cells fade-only; `CellDigitLayout` extracted for clean separation

- [x] **Petal burst on completion** — `SumiPetalBurst(trigger)` composable; same physics engine
  as splash `SumiPetals` (shared `renderPetalAtProgress`); wind always left→right; seed from
  `celebrationCount` so each burst is uniquely random; triggered in `GameViewModel` via
  `completionKey()` diff on every board state change (rows, cols, boxes, grid)

- [x] **Paywall entry point** — UI and paywalling logic to implement; billing SDK integration deferred to last.
  - Gate Edo difficulty and unlimited hints behind "Sumi Pro"
  - Show paywall when user taps a gated feature, not on cold launch

- [x] **Petal wind physics overhaul** — Natural wind-blown blizzard effect
  - All petals drift diagonally (wind angle 30–70°), not purely vertical
  - 2× density: 20 ambient petals on splash, 32 per burst in game
  - 40% of petals enter from left edge (side wind), rest from top
  - Stronger turbulence (swayAmp 45–110px), aggressive tumbling rotation
  - Season-coloured petals on season selection in onboarding

- [x] **Season tile liquid / bubble animation** — Onboarding season picker
  - On tap: tile inflates to 1.20× (120ms ease-in) then spring settles to 1× (MediumBouncy)
  - Border animates from thin+grey to 2dp+accent colour
  - Petal burst triggers with season-accent colours on selection; old petals fade when switching

- [x] **Paywall floating entrance animation**
  - Feature rows stagger in one by one (each 60ms offset), fade + translate from below
  - Sumi enso logo animates in last, reusing the LogoEnso appear transition from Splash
  - Consistent logo-appear animation shared across Splash → Paywall → any future brand screen

- [x] **Game unit tests — full coverage** (87 tests total)
  - `RealBoardManager`: 22 tests — state emission, select/enter/erase/undo/hint/toggleNotes/tick
  - `SudokuGenerator`: 5 tests — valid grid, all difficulties, seed determinism
  - `BoardState`: 50 tests — notes cleared on digit entry, remainingCounts invariant, all difficulties
  - `PuzzleRepository`: 10 tests — daily() determinism + validity, fromSeed(), getOptions()

- [x] **Logo composables** — `LogoEnso`, `LogoGrid` (nine-dot 3×3), `LogoNine`, `LogoChop`, `LogoStrokes`, `LogoWordmark` all implemented in `Logos.kt`

- [x] **Ink bleeds** — All screens now have ink bleed textures per BACKGROUNDS.md spec:
  - Splash: `ink_bleed_01` (360dp, alpha 0.12, centered)
  - Win: `ink_bleed_02` (280dp, top-left, alpha 0.08) + `ink_bleed_03` (220dp, bottom-right, alpha 0.10)
  - Paywall: `ink_bleed_01` (400dp, top-center, alpha 0.14)
  - Daily: `ink_bleed_01` (120dp, top-right, alpha 0.06)
  - Stats: `ink_bleed_01` (200dp, center, alpha 0.08)

- [x] **Accessibility — Phase 1** — Core semantics wired across all screens:
  - `SumiEyebrow` → `semantics { heading() }` (propagates to all section labels app-wide)
  - `DifficultyTile` → `semantics(mergeDescendants=true) { contentDescription = "Start Easy game, about 3 min" }` per tile; decorative kanji marked `hideFromAccessibility()`
  - `StreakCard` → merged with spoken label "N day streak"
  - Win `完` / separator → `hideFromAccessibility()`; level kanji → `contentDescription = difficulty`
  - Win stats cells → `mergeDescendants = true` so label + value read as one unit
  - Stats hero → `mergeDescendants` + `contentDescription = "$total puzzles solved, all time"`
  - Stats cells → `mergeDescendants = true`
  - Stats eyebrow "練習" → `contentDescription = "Practice Stats"` (no raw CJK to screen reader)

- [x] **Accessibility — Phase 2**
  - Board cells: `CellA11yGrid` overlay in `SumiBoard` — per-cell `contentDescription` ("Row 3, column 5. Given: 7"); `semantics { onClick }` for assistive-tech activation; board `Box` marked `isTraversalGroup = true`
  - Game-state live region: `GameAnnouncer` composable in `GameScreen` — polite live region announces row/col/box completions, conflicts, puzzle solved
  - `minimumInteractiveComponentSize()` on number pad cells, tool row items, and bottom nav items
  - Reduced motion: `rememberReducedMotion()` expect/actual (Android: `ANIMATOR_DURATION_SCALE == 0`; iOS: `UIAccessibilityIsReduceMotionEnabled`) — wired into `SumiPetals` (ambient + burst) and `AuroraSweep` (sweep skipped entirely)
  - Pause overlay: 0.82 (light) / 0.88 (dark) paper scrim in `PauseOverlay.kt` per BACKGROUNDS.md §BG-5
  - Firebase Analytics + Crashlytics: `SumiAnalytics` interface + `FirebaseSumiAnalytics` impl; events: game_started, game_completed, game_over, hint_used, onboarding_completed; wired at GameEntry, WinEntry, OnboardingEntry

### Phase 3 — Monetisation (UI first, SDK integration deferred)

- [ ] **In-app purchases (IAP)** — build the full purchase flow UI; wire to real billing last
  - Android: Google Play Billing Library *(deferred — integrate last)*
  - iOS: StoreKit 2 *(deferred — integrate last)*

- [ ] **Restore purchases** — required for App Store submission *(deferred with billing SDK)*

- [x] **AdMob infrastructure** — Gradle deps wired (basic-ads, admob, android-ump); `BasicAds.Initialize()` in App.kt; test App IDs in AndroidManifest + Info.plist; ad composables not yet placed in screens (deferred post-IAP)
- [x] **Firebase Analytics + Crashlytics** — GitLive KMP deps wired; `SumiAnalytics` interface + `FirebaseSumiAnalytics` impl; events: game_started, game_completed, game_over, hint_used, onboarding_completed; Crashlytics auto-collects crashes

### Phase 4 — Future Features

- [x] **Seasonal themes** — Spring / Autumn / Winter palettes implemented; season picker in onboarding + settings
- [ ] **Daily challenge leaderboard** — same seed for all users on a given day; share solve time
- [ ] **Puzzle replay** — `fromSeed()` exists in PuzzleRepository; needs UI entry point
- [ ] **Settings expansion** — haptics toggle done; season picker done; further expansion TBD

---

### Phase 5 — Game Save / Resume

- [x] **Save slots** — one per difficulty (5 total: Easy / Medium / Hard / Master / Edo); independent
- [x] **Auto-save on every user move** — `GameViewModel.startSync()` writes to DataStore after each board state change; no data loss on process death
- [x] **Resume on re-launch** — `GameViewModel.init()` checks for a live save (same day) and restores board, timer, mistake count, hints; notes mode resets to false
- [x] **Staleness check** — saves stamped with `epochDay`; save from a previous day is silently discarded, fresh puzzle starts
- [x] **Clear on win/game-over** — save slot cleared automatically when the game ends
- [x] **"New Game" from pause overlay** — calls `clearSave()` before `init()`, discarding the current slot and starting fresh
- [x] **`GameSave` / `GameSaveRepository` / `DataStoreGameSaveRepository`** — DataStore-backed, 5 keys per difficulty slot
- [x] Registered in Koin (`AppModule`); `GameViewModel` factory updated

---

## Your Action Items (needs you)

---

> **Build note:** The Kotlin code compiles fine without these files. Building an actual APK (`assembleDebug` / `assembleRelease`) or an iOS `.ipa` will fail until `google-services.json` and `GoogleService-Info.plist` are in place. Complete steps 1–3 before running the app on a device.

---

### 1 · Firebase — Console setup

Go to **console.firebase.google.com** → your project (or create one named "Sumi").

**Android — register TWO apps** (same project, two package IDs):

| App | Package name | What it's for |
|---|---|---|
| Debug | `xyz.ksharma.sumi.debug` | Development builds on your device / CI |
| Release | `xyz.ksharma.sumi` | Production / App Store |

For each registration, Firebase will offer you a `google-services.json`. Download the one for **debug** and the one for **release** separately — they will contain different App IDs and API keys.

- [ ] Place the **debug** `google-services.json` at → `androidApp/src/debug/google-services.json`
- [ ] Place the **release** `google-services.json` at → `androidApp/src/release/google-services.json`
- [ ] Both files are already in `.gitignore` — do not commit them

**iOS — register ONE app:**

| App | Bundle ID |
|---|---|
| iOS | `xyz.ksharma.sumi` |

Firebase will give you `GoogleService-Info.plist`.

- [ ] Place it at → `iosApp/iosApp/GoogleService-Info.plist`
- [ ] Open Xcode → in the Project Navigator drag `GoogleService-Info.plist` into the `iosApp/iosApp/` group → in the dialog that appears tick **"Add to target: iosApp"** → click Finish
- [ ] File is already in `.gitignore` — do not commit it

---

### 2 · Firebase — Xcode: add firebase-ios-sdk via SPM

The Kotlin/GitLive side is already wired. iOS still needs the native SDK linked.

- [ ] Xcode → **File → Add Package Dependencies…**
- [ ] Paste URL: `https://github.com/firebase/firebase-ios-sdk`
- [ ] Version rule: **Up to Next Major Version** from `11.6.0`
- [ ] In the product list, select **only these two** and add them to the **iosApp target**:
  - `FirebaseAnalyticsWithoutAdIdSupport` ← use this one (NOT `FirebaseAnalytics`) because AdMob is also in the project and both claim IDFA access — using both causes a rejection
  - `FirebaseCrashlytics`

---

### 3 · Firebase — Xcode: Crashlytics dSYM upload script

Crashlytics needs this to symbolicate crash reports (turn memory addresses into readable function names).

- [ ] Xcode → select the **iosApp** target → **Build Phases** tab
- [ ] Click **`+`** → **New Run Script Phase**
- [ ] Drag the new phase so it sits **after "Compile Sources"**
- [ ] Paste this into the script box:
  ```sh
  "${SHARED_PRECOMPS_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"
  ```
- [ ] Under **Input Files**, click `+` and add:
  ```
  $(SRCROOT)/$(BUILT_PRODUCTS_DIR)/$(INFOPLIST_PATH)
  ```

---

### 4 · AdMob — Console setup

Go to **apps.admob.com** → **Apps → Add app** — register **separately** for Android and iOS (each gets its own App ID and ad unit IDs).

**What you'll get and where it goes:**

| ID | Where to put it |
|---|---|
| Android App ID (`ca-app-pub-…~…`) | Open `androidApp/build.gradle.kts` → find the `release` block → replace `ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX` with your real ID. The `debug` block already uses Google's public test ID — leave it as-is. |
| iOS App ID (`ca-app-pub-…~…`) | Open `iosApp/iosApp/Info.plist` → find `<key>GADApplicationIdentifier</key>` → replace the value with your real iOS App ID |

**Create ad units** (do this after the apps are registered):

For each platform create three units: **Banner**, **Interstitial**, **Rewarded** (not Rewarded Interstitial — different format).
Save all six ad unit IDs — they'll be needed when wiring ad composables into screens.

---

### 5 · AdMob — Xcode: add GoogleMobileAds via SPM

- [ ] Xcode → **File → Add Package Dependencies…**
- [ ] Paste URL: `https://github.com/googleads/swift-package-manager-google-mobile-ads`
- [ ] Version rule: **Up to Next Major Version**
- [ ] Select **`GoogleMobileAds`** and add it to the **iosApp target**

---

### 6 · Assets

- [x] **SVG → Vector XML: `logo-enso.svg`** — Converted to `logo_enso.xml`; `LogoEnso` composable updated with `ColorFilter.tint`.
- [ ] **SVG → Vector XML: `wordmark.svg`** — Optional; live Cormorant text renders correctly today.
- [ ] **SVG → Vector XML: `icons/`** — Audit `docs/handoff/svg/icons/` against `SumiIcons.kt` for any missing icons.
- [ ] **README screenshots** — Take screenshots of Splash, Home, Game, and Win screens; drop them into `docs/screenshots/` and ask me to wire them into the README.
- [ ] **App Store assets** — App Store Connect needs 6.7″ and 6.1″ screenshots for iOS. Play Console needs phone + 7″ tablet screenshots. App icon SVGs are in `docs/handoff/svg/`.

---

---

## Zen Pro Screen — AI Visual Design Prompt

Use this prompt with a visual design AI (v0, Lovable, Figma AI, etc.) to get high-fidelity designs for the Zen Pro tab before implementing the final UI.

```
Design the "Zen" tab for Sumi Pro — a minimalist Japanese-inspired sudoku app (Kotlin Multiplatform).

Design system constraints:
- Palette: ink (#1A1210 light / #E8E0D5 dark), paper (#FBF7F1 light / #1A1210 dark), gold (#B8860B), red (#8B0000), soft ink (#6B5E56), faint ink (#9A9087)
- Background: washi paper texture (linen-cream, subtle tooth)
- No gradients except aurora (row/col completion sweep)
- No rounded corners except buttons (2dp radius) and bottom sheets (12dp radius)
- No Material Design defaults, no drop shadows
- Typography: Cormorant Garamond (display, quotes, CJK labels), Noto Sans (UI labels, numbers)
- Spacing tokens: s1=2dp, s2=4dp, s3=6dp, s4=8dp, s5=12dp, s6=16dp, s7=24dp, s8=32dp, s9=48dp

Screens to design (all on the same tab, scrollable):

1. PRO THANK-YOU CARD (top of Zen tab)
   - Thin gold-tinted border rectangle
   - Centered enso circle (partial brushstroke arc, gold)
   - "Sumi Pro" in Cormorant h2, gold
   - Italic tagline: "Thank you for your practice."
   - Benefits in Noto Sans micro text, ink-faint, centered with · separators:
     "Ad-free  ·  Unlimited hints  ·  All difficulties"
     "Full quote library  ·  PDF puzzle books  ·  Themes"
   - Fade-in animation on load

2. QUOTE LIBRARY BROWSER (horizontal pager card)
   - Thin paper-edge border card
   - Swipeable horizontal pager: one quote per page
   - Large italic Cormorant text (16sp) centered, with curly quotes
   - "— Author Name" in Noto Sans micro below, muted
   - Thin horizontal progress bar at bottom of card (ink-faint fill, paper-edge track)
   - Left/right arrow icons (subtle, ink-faint) with "n / total" counter for accessibility
   - 20 quotes total from a philosophical/zen library

3. PUZZLE BOOK ENTRY CARD (tappable)
   - Thin paper-edge border card
   - Centered kanji 活 (36sp, ink-faint) as decoration
   - "Design a Puzzle Book" heading (Cormorant h2)
   - Two-line description: "Choose difficulty, count, and theme. Generate a print-ready PDF to share or print."
   - Tap opens full-screen slide-up designer overlay

4. BOOK DESIGNER OVERLAY (full-screen slide-up)
   - Same washi paper background
   - Header: "Design Your Book" (Cormorant h2) + close × icon
   - Thin ornamental rule with kanji 設 centered
   - DIFFICULTY section: FlowRow of chips (Easy / Medium / Hard / Master / Edo)
     - Chip: thin border, uppercase Noto Sans, fills with 6% ink tint when selected, border turns ink-dark
   - PUZZLES section: Row of chips (25 / 50 / 100)
   - THEME section: 3-column grid top row (Light / Dark / Gold) + 2-column bottom row (Winter / Summer / empty)
     - Theme swatch: colored rectangle (shows actual PDF background color), theme name centered
     - Same height for selected and unselected (show check icon always, transparent when not selected)
     - Border turns ink-dark when selected, paper-edge otherwise
   - GENERATE button (full-width, large): share icon + "Generate & Share" text
     - Below button: "$count puzzles · print-ready PDF" in micro text, ink-faint
   - Loading state: circular progress + "Generating…" text
   - Error state: error message in red + "Dismiss" ghost button

PDF output (for reference):
- A4 page (595×842pt)
- Header: enso circle arc logo + "Sumi" italic + page number right-aligned + horizontal rule
- 9×9 sudoku grid centered, thin cell lines + bold 3×3 box lines
- Footer: difficulty + quote text (truncated)
- 5 themes: Light (cream paper), Dark (near-black), Gold (warm amber), Winter (cool blue-grey), Summer (warm sand)
```

---

## Known Limitations

- `recordSolve()` counts every win as +1 to total puzzles; if the app is killed mid-solve and
  re-launched, the win event fires correctly via WinEntry. No double-count risk.
- Best streak is only updated on a NEW day's solve (not when continuing the same day).
- Paywall screen buttons (`onSubscribe`, `onRestore`) are no-ops — not wired to IAP.
