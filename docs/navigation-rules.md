# Sumi — Navigation Rules

> **Status: draft — please review and approve before changes are made based on this doc.**

---

## Stack structure

Sumi uses Navigation3 with a single active back-stack rooted at `SplashRoute`.
After the splash completes, `resetRoot(HomeRoute)` replaces `SplashRoute` in the stack.
`topLevelRoute` stays as `SplashRoute` throughout (this keeps `isSetupFlow = true`,
which controls whether the bottom nav bar appears — it is hidden for now).

Typical stack after a game session:

```
[HomeRoute] → launch, then game
[HomeRoute, GameRoute(Medium)]  → playing
```

---

## Rules per screen

### Splash → Home
- On timer expiry: `resetRoot(HomeRoute)` — SplashRoute leaves the stack.
- Back from Splash: not applicable (no back target).

### Home
- Tapping a difficulty tile: `goTo(GameRoute(difficulty))`.
- Tapping settings gear: `goTo(SettingsRoute)`.
- Back from Home: no-op (Home is the root).

### Game
- Back (←) during play: `pop()` → returns to Home. *(Phase 5: save game first.)*
- On game complete (isComplete = true): `resetRoot(HomeRoute)` then `goTo(WinRoute)`.
  This ensures Win is never stacked on top of Game — back from Win always goes to Home.
- "New Puzzle" from game-over overlay: `vm.init(diff)` — resets game in place, no navigation.
- "Return Home" from game-over overlay: `pop()` → Home.
- Game-over overlay visibility: derived from `state.isGameOver` (not `rememberSaveable`).
  This prevents the overlay from persisting across VM re-use.

### Win (result screen)
- Win screen is **single-visit only**. Navigation away removes it from the stack.
- "Return Home": `resetRoot(HomeRoute)`.
- "Next Practice": `resetRoot(HomeRoute)` then `goTo(GameRoute(difficulty))`.
  Stack after: `[HomeRoute, GameRoute]` — back from new game → Home ✓.
- System back from Win: calls `pop()` → currently returns to Home because
  Win is navigated to via `resetRoot + goTo`, so Home is the only item below.

### Settings
- Navigated to via `goTo(SettingsRoute)` from Home.
- Back (←) or system back: `pop()` → returns to Home.
- "View Licenses": `goTo(LicensesRoute)`.

### Licenses
- Navigated to via `goTo(LicensesRoute)` from Settings.
- Back (←) or system back: `pop()` → returns to Settings.

---

## Rules that apply everywhere

| Rule | Reason |
|---|---|
| Win screen is never below another screen on the stack | Back from any screen post-Win should go to Home, not the completed game |
| Game-over overlay is not a navigation destination | It is an in-screen overlay driven by `state.isGameOver` |
| No two instances of the same route in the active stack | `goTo` on a non-top-level route always pushes; same route twice would create two entries — use `resetRoot` instead when replacing |
| `resetRoot(HomeRoute)` is the canonical "go home and clear everything" action | Used by Win → Home, Game complete → Win, and any future "exit" actions |

---

## Decisions (resolved)

1. **System back from Game during play** → **save the game state** (Phase 5). Back = save-and-exit, not discard. Fresh game is only started when no save exists for that difficulty's daily puzzle.

2. **Bottom nav bar** → visible on all screens **except Splash and Onboarding**. It should appear as soon as the user lands on Home for the first time.

3. **Multiple difficulty slots** → **yes, auto-save**. Navigating away from an in-progress Medium game (back, or switching to a different difficulty) saves it to the Medium slot. The new game uses its own slot independently.
