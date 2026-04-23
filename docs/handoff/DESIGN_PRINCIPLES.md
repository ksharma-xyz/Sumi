# Design Principles

These are non-negotiable. Every decision — layout, copy, motion, a new feature — must pass these tests.

---

## 1. Paper, not Material

Sumi is a printed object, not a screen. The aesthetic heritage is:
- Japanese washi paper
- Hand-inked sudoku puzzlebooks
- Editorial magazine typography (mid-century craft journals)
- Zen brush practice

**Concrete rules:**
- No Material ripples. `Modifier.clickable(indication = null)` everywhere; draw custom press states as a 1-frame ink-darken.
- No elevation shadows except `SumiTokens.elevation.seal` (for the red chop stamp) and very soft seams. No floating cards.
- No gradients anywhere except: the **aurora sweep** (row/column/box/win celebrations) and the **seasonal border bloom** (ambient, very subtle).
- Surfaces are layered like paper: `paper` > `paperWarm` > `paperDeep`. Each is a distinct tone, not a shadow.

## 2. Sharp edges, not rounded

Most surfaces have **0 or 2.dp** corner radius — the edge of a sheet of washi, not a pebble.
- Cells, chips, panels, sheets → `Radius.xs` (2.dp) or `Radius.none`
- Buttons → `Radius.md` (8.dp)
- Full-screen sheets / dialogs → `Radius.lg` (12.dp)

Never use `Radius.pill` except where the spec explicitly calls for a toggle/switch.

## 3. Typography is the design

- **Italic display serif** (Cormorant Garamond) carries emotion and voice. Use for headlines, hero quotes, screen titles. Always italic at h1/h2/h3/subhead.
- **Body serif** (Source Serif 4) for long-form reading: daily quotes in context, onboarding prose.
- **UI sans** (Inter, semibold, UPPERCASE, widely tracked) for buttons, labels, metadata. This is the **only** place sans appears. Always `.uppercase()` and letter-spacing ≥ 0.2em.
- **Hand script** (Caveat) ONLY for user-entered digits on the Sudoku board. Nowhere else.
- **CJK** (Shippori Mincho) for kanji chops: 墨 (sumi/ink), 休 (rest — pause), 完 (complete — win).

The difference between "given" and "entered" digits is the whole board story: ink vs pen-on-ink.

## 4. The board is sacred

The 9×9 grid during an active solve is inviolable. No ads, no modals, no banners, no interstitials. The Pause button opens a blurred overlay, not a full screen takeover. Hints surface as small brush-stroke annotations on the grid edge.

## 5. Red means "today" and "done"

`SUMI.color.red` (#A8342A — vermilion chop-stamp red) is the most expensive color in the system. It appears only in three places:
1. The app logo's red kanji seal
2. "Today" / the daily puzzle flag
3. Celebration stamps (win screen seals, solved rows/columns via aurora)

Never use red as a generic accent. If you find yourself reaching for it for a tag, a button, or a border — stop. Use ink or paperEdge.

## 6. Gold means "Pro"

`SUMI.color.gold` (#8A6B2A) appears ONLY on Pro surfaces: the Pro membership card, The Salon (weekly leaderboard) chop, Pro theme names. A free user never sees gold anywhere in the app. Seeing it is the reward of paying.

## 7. Teal is the user's pen

`SUMI.color.teal` (#2A5A6E, fountain-pen blue) is the color of user-entered digits on the board, and nothing else. It distinguishes the player's marks from the given clues without shouting. The cognitive metaphor: given clues are printed in ink; your entries are fountain-pen.

## 8. Copy is literary, not chirpy

Write like you're writing for a craft magazine or a contemplative app like Stoic — not a freemium mobile game.

| ❌ Don't | ✅ Do |
|---|---|
| "Great job! 🔥" | "The grid is quiet again." |
| "Unlock premium!" | "Sumi Pro — an uninterrupted practice." |
| "You've solved 486 puzzles!" | "486 puzzles solved, all time." |
| "Daily Challenge" | "Today's puzzle" |
| "Pause" | "休" (with English "Rest" below, at smaller size) |

No exclamation marks. No emoji. No "challenges", "streaks!", "levels", "XP". Use "practice", "today", "the day's puzzle", "your log".

## 9. Motion is breath

All motion uses `Ease.paper` by default — soft settle, quick start. Longer celebrations use `Ease.bleed` (ink spreading). Everything feels like wet ink meeting dry paper.

- 160–260ms for small confirmations
- 380–560ms for chrome/screen transitions
- 900ms–1.6s for rare, meaningful celebrations

No springs with bounce except at the single "snap" confirmation moment. Bounce is for joy; most of Sumi is for calm.

## 10. Silence is the default

Sound is off by default. When on, sources are organic: brush on paper, water drop, wood clack, paper unrolling. No synthesized chimes, no "success!" fanfare. See `SOUND_HAPTICS.md`.

Haptics are subtle: `.light` on taps, `.rigid` on mistakes (one, small), `.success` doubled on puzzle solve. Nothing else.

## 11. Dark mode is lamplight, not a screen

Sumi's dark mode is **warm ink on warm black** — never cool white on pure black. It should feel like reading by a paper lantern at night, not staring into an OLED.

**Concrete rules:**
- **Surfaces are warm-black, not pure black.** `Night.paper` is `#1A1410` (a soft sumi-ink black with red-brown undertone), never `#000000`. `Night.paperWarm` (`#231C15`) sits one step warmer still.
- **Text is warm-cream, not white.** `Night.ink` is `#F4ECE0` — the same cream as day-mode paper, inverted onto black. Never `#FFFFFF`. The eye should rest, not strain.
- **Accents warm and brighten proportionally.** Red shifts from `#A8342A` → `#E84A3E` (a brighter vermilion so it still reads as a chop, not a shadow). Teal lifts from `#2A5A6E` → `#6FA8BC`. Gold warms from `#8A6B2A` → `#D9A855`. These are **not** automatic tints — they are hand-tuned to hold the same emotional weight on black paper.
- **The user's pen stays legible.** Caveat entries in `Night.teal` must remain the single strongest signal on the board. Test every night surface with a real night-mode solve.
- **Celebrations dim, not brighten.** `AURORA_HUES_DARK` is a separate, darker hue set — ember and ash tones, not neon. The celebration is still visible but never aggressive against a dark surface.
- **No pure-black tab bars, sheets, or modals.** Every surface in night mode is a shade of `Night.paper*`. The washi texture (`WashiBG`) still renders — the noise just layers over dark instead of cream.

**The test:** hold the phone at arm's length in a dim room. The screen should glow like a lantern, not a flashlight. If any element looks white or pure-black, it's wrong.

**User control:** Settings offers `Follow system / Always light / Always dark`. Default is follow system. The toggle lives in Settings, not the top bar — switching modes is an occasional choice, not a repeated action.

---

## Anti-patterns to reject (and push back on)

If a PM / designer / Claude Code suggests any of the below, refuse or request review:

- ❌ **Gradients on cards or buttons.** Reject.
- ❌ **Animated backgrounds in-game.** The board is static during a solve.
- ❌ **Emoji anywhere.** Even the good ones.
- ❌ **Bottom navigation bar with tab badges.** Sumi has a top-left brand chop and a single bottom-right action. No tab bar.
- ❌ **"Level" or "rank" progression.** The practice is the reward.
- ❌ **Leaderboards framed as competition.** The Salon is a weekly register — names, cities, times. Not a "who won".
- ❌ **Notifications with numbers.** Daily reminder is allowed, once per day, in the morning, quiet copy only.
- ❌ **Paywall modal over an active solve.** Paywalls only appear on natural pauses (between puzzles, on entering Pro-only difficulty).
- ❌ **Dark patterns for Pro upgrade.** Free tier must be complete and gracious, not hobbled.

---

## When in doubt

Ask: *"Would this belong in a hand-bound puzzle book on a train, read by someone with tea?"*

If no, cut it.
