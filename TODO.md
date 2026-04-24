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

- [ ] **Paywall entry point** — UI and paywalling logic to implement; billing SDK integration deferred to last.
  - Gate Edo difficulty and unlimited hints behind "Sumi Pro"
  - Show paywall when user taps a gated feature, not on cold launch

### Phase 3 — Monetisation (UI first, SDK integration deferred)

- [ ] **In-app purchases (IAP)** — build the full purchase flow UI; wire to real billing last
  - Android: Google Play Billing Library *(deferred — integrate last)*
  - iOS: StoreKit 2 *(deferred — integrate last)*

- [ ] **Restore purchases** — required for App Store submission *(deferred with billing SDK)*

- [ ] **Ads** — integration deferred to last (post-IAP). Reserve ad placement slots in UI now.

### Phase 4 — Future Features

- [ ] **Seasonal themes** — alternate colour palettes (Spring cherry, Autumn maple, Winter indigo)
  - Design tokens already support `isDark` toggle; seasons would need a third axis
- [ ] **Daily challenge leaderboard** — same seed for all users on a given day; share solve time
- [ ] **Puzzle replay** — `fromSeed()` exists in PuzzleRepository; needs UI entry point
- [ ] **Settings expansion** — theme picker, sound/haptic toggles, difficulty preference

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

## Known Limitations

- `recordSolve()` counts every win as +1 to total puzzles; if the app is killed mid-solve and
  re-launched, the win event fires correctly via WinEntry. No double-count risk.
- Best streak is only updated on a NEW day's solve (not when continuing the same day).
- Paywall screen buttons (`onSubscribe`, `onRestore`) are no-ops — not wired to IAP.
