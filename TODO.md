# Sumi — Build Status

Last updated: 2026-04-24

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

- [ ] **Paywall entry point** — decide when/where to show Paywall (e.g., after N free puzzles,
  or when tapping "Zen" tab). Currently reachable only via bottom nav tab.

### Phase 3 — Monetisation

- [ ] **In-app purchases (IAP)** — platform-specific billing
  - Android: Google Play Billing Library
  - iOS: StoreKit 2
  - Gate: Edo difficulty and/or unlimited hints behind "Sumi Pro"

- [ ] **Restore purchases** — required for App Store submission

### Phase 4 — Future Features

- [ ] **Seasonal themes** — alternate colour palettes (Spring cherry, Autumn maple, Winter indigo)
  - Design tokens already support `isDark` toggle; seasons would need a third axis
- [ ] **Daily challenge leaderboard** — same seed for all users on a given day; share solve time
- [ ] **Puzzle replay** — `fromSeed()` exists in PuzzleRepository; needs UI entry point
- [ ] **Settings expansion** — theme picker, sound/haptic toggles, difficulty preference

---

### Phase 5 — Game Save / Resume

**Business logic (spec — implement in a future phase):**

**Save slots:** one per difficulty level (5 total: Easy / Medium / Hard / Master / Edo).
Multiple difficulties can have live saves simultaneously; they are fully independent.

**Save trigger:** the moment the user correctly places their first digit. A blank grid is not
worth saving. Incorrect digits don't count (they're never saved state anyway).

**On Home → tap difficulty tile:**
- Check: does this difficulty have a live save for today's puzzle? (epoch day stored with save)
  - **Yes** → resume immediately, no prompt
  - **No** → start a fresh game
- If the daily puzzle has rolled over since the save was written, the save is stale → discard
  it silently and start fresh

**New game / discard from within an active game:**
- The Pause overlay should expose a **"New Game"** button (in addition to Resume / Home)
- Tapping "New Game" clears the save slot for that difficulty and restarts fresh
- Win screen navigated to → auto-clears that difficulty's save slot
- Game-over (3 marks) → auto-clears that difficulty's save slot

**What to persist per slot (DataStore, one key per difficulty):**
- `epochDay: Int` — the day the game was started (for staleness check)
- `cells: String` — serialised 81-cell user-entered values (0 = empty; givens are re-derived)
- `elapsedMs: Long`
- `mistakeCount: Int`
- `hintsRemaining: Int`
- Notes mode resets to `false` on resume

**Navigation invariant:** you cannot have two `GameRoute` entries on the stack simultaneously
(Home → Game is always a push; going back pops back to Home). If a user is on GameScreen and
taps a difficulty from Home, that path is not reachable — they must navigate back first.

---

## Known Limitations

- `recordSolve()` counts every win as +1 to total puzzles; if the app is killed mid-solve and
  re-launched, the win event fires correctly via WinEntry. No double-count risk.
- Best streak is only updated on a NEW day's solve (not when continuing the same day).
- Paywall screen buttons (`onSubscribe`, `onRestore`) are no-ops — not wired to IAP.
