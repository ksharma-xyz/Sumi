# Product

The two-tier model. Read alongside `DESIGN_PRINCIPLES.md §6` (Gold = Pro) and `SCREENS.md §09 Paywall`.

---

## Free · Sumi 墨

A gracious, complete app for the casual solver. Never hobbled.

- The Daily puzzle (one per morning)
- Three additional puzzles per day
- **Easy and Medium difficulty only**
- 3 hints per puzzle
- Basic streak tracking
- Paper theme only (light mode only)
- One full-bleed interstitial ad between puzzles (never during, never banner)
- One literary quote per day from the short anthology (13 quotes in `kotlin/Quotes.kt`)

## Pro · Sumi 墨 · Gold

For the solver who wants room to go further.

- **The Salon 墨会** — weekly global register. Top 50 times each week, entered in vermilion by Monday morning. Presented like an old guestbook: names, cities, times.
- **Hard, Master, Edo** difficulties
- Unlimited puzzles (not just 3/day)
- Unlimited hints
- The full quote library — 600 passages across seven libraries (Eastern classics, Western classics, craft, silence, mathematics, poetry, house)
- Practice log — stats, improvement curves, PBs by difficulty
- **Gold**, **Indigo**, and **Edo Pattern** themes
- Night mode
- iCloud / Google Drive sync across devices
- PDF puzzle book export — for trains, tea rooms, travel
- No advertisements, anywhere

## Pricing (suggested)

- $3.99/month
- $29/year (save 38%)
- $79 lifetime

Present annually by default in the paywall. Monthly is a secondary toggle.

## Ad policy

1. **Never during a solve.** The board is sacred.
2. **Only at natural pauses.** Between puzzles, or on return-to-home.
3. **Maximum one ad per 3 puzzle completions.**
4. **Full-bleed, never boxed.** If an ad appears, it takes the full screen for a few seconds and exits cleanly. No 320×50 banners, ever.
5. **Never on Daily puzzle screens** — the Daily is the ritual.

## The Salon (Pro leaderboard)

This is Pro's single most differentiating feature. Design it to feel like a 19th-century register, not a game leaderboard.

- Weekly cycle (Monday 00:00 UTC reset).
- Entry criteria: solve a Hard/Master/Edo puzzle.
- Top 50 presented in a single paginated page:
  - Rank in vermilion numeric (tabular)
  - Name (UI sans, small caps)
  - City (italic body, smaller, inkSoft)
  - Time (display italic)
  - Difficulty chip (gold for Master, red for Edo)
- Current user's entry (if top 50) highlighted with a subtle gold seam.
- If not in top 50: footer shows "Your best this week: 14:22 · Rank 312" in UI meta.
- Weekly winner gets a small Enso stamp next to their name on subsequent weeks.

## Streaks

Intentionally understated. `IconFlame` + "12 days" in UI meta on Home — never flashing, never warning. A 1-day grace for missed days ("quiet days"). Never a popup when you break a streak.

## Notifications

Off by default. If enabled, exactly one per day:
- Sent 8am local time
- Copy: *"Today's puzzle is ready. — Sumi"* (no emoji, no urgency)
- Tapping opens Home scrolled to today's card.
