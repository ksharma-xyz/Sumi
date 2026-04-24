# Accessibility (A11y)

Sumi must be usable by everyone. The literary tone and paper aesthetic do not excuse skipping a11y — they demand it, because our users include people reading by lamplight in waiting rooms, on the commute, with reading glasses, or with low vision.

**Action for the LLM:** this is a hard contract. Every component must meet these criteria. The verification framework (`VERIFICATION.md`) checks these explicitly.

---

## 1. Contrast targets (WCAG)

All body text and UI controls must meet **WCAG AA** minimum; display type meets AA Large.

| Role | Size | Required ratio | Status in tokens |
|---|---|---|---|
| Body (16sp) | normal | 4.5:1 | `ink #1A1410` on `paper #F4ECE0` → 12.6:1 ✓ |
| Body S (14sp) | normal | 4.5:1 | `inkSoft #4A3E30` on `paperWarm` → 7.1:1 ✓ |
| UI Label (12sp) | bold | 4.5:1 | `ink` on `paperWarm` → 11:1 ✓ |
| Display (≥18sp bold / ≥24sp) | large | 3:1 | `ink` italic on paper → 12.6:1 ✓ |
| Button text | normal | 4.5:1 | `paper` on `ink` → 12.6:1 ✓ |
| Disabled text | N/A | 3:1 recommended | Use `inkSoft` @ 50% alpha, NOT `inkFaint` |
| Chip borders | — | 3:1 non-text | Use `ink` or `paperEdge`, never `inkFaint` |

**Dark mode:**
- `Night.ink #F4ECE0` on `Night.paper #1A1410` → 14.2:1 ✓
- `Night.inkSoft #C9B48A` on `Night.paper` → 7.8:1 ✓
- Red `#E84A3E` on `Night.paper` → 5.1:1 ✓ (AA)
- Gold `#D9A855` on `Night.paper` → 7.9:1 ✓

**Banned combinations (fail AA):**
- `inkFaint` on `paperWarm` (was used for captions — replace with `inkSoft`)
- `paperEdge` as text color on any paper surface
- Gold on paper (only appears on dark Pro surfaces)
- Red on `paperWarm` for body text (red is for chops + single words only)

**Tooling:** run every color pair through a WCAG checker before shipping. Add a test in `commonTest` that iterates `Sumi.Color.*` pairs and asserts ratios.

## 2. Touch targets

- Minimum: **48×48.dp** (Material + WCAG 2.5.5)
- Comfortable: 56.dp for primary actions
- Use `Modifier.minimumInteractiveComponentSize()` on every clickable

Number pad buttons must be 48.dp at Compact, scaling up on larger screens. Tool row icons (undo/erase/notes/hint) are 26.dp icons inside 48.dp hit areas.

## 3. Focus order & keyboard nav

Every interactive must be reachable via keyboard + external switch.

- Logical traversal order on every screen (declare with `Modifier.focusRequester` + `focusProperties { next = ... }` when needed)
- Visible focus indicator: 2.dp outline in `SUMI.color.red` offset by 2.dp — contrasts with paper (AA non-text)
- No focus traps. Pause overlay and paywall must be escapable via back/Escape
- Tab cycles: top bar → game board → number pad → tool row → bottom nav

## 4. Screen readers (TalkBack / VoiceOver)

**Board cells:**
```
"Row 3, column 5. Given clue: 7."
"Row 4, column 2. Empty. Double-tap to select."
"Row 5, column 8. Your entry: 4. Double-tap to clear or change."
```

Label every cell with row/column and content. Use `Modifier.semantics` with `stateDescription` for given/empty/entered/conflict.

**Game state announcements (live regions):**
- House completion → announce "Row 5 complete" (aurora is visual; screen reader gets words)
- Puzzle solved → announce "Puzzle complete. Time: 14 minutes 22 seconds. 2 mistakes."
- Conflict detected → announce "Conflict with row 5" (polite, not assertive)

**Kanji:** every 墨 / 休 / 完 has an English `contentDescription` — "Sumi", "Rest", "Complete". Never let a screen reader read raw Unicode fallback.

**Decorative:** `WashiBG`, `InkBleed`, `BrushStroke`, petal fall → `Modifier.semantics { invisibleToUser() }`.

**Icons:** every `IconButton` has a non-empty `contentDescription`. No "Button" / "Icon" / "Image" — always describe the action: "Pause game", "Undo last move", "Toggle notes mode".

## 5. Motion preferences

Respect `prefers-reduced-motion` (iOS) and `Settings.Global.ANIMATOR_DURATION_SCALE` (Android).

When reduced:
- PetalFall → off entirely
- PaperBreath → off (static noise, no oscillation)
- AuroraSweep on house complete → replaced with a 200ms bg-tint fade (no sweep)
- AuroraSweep on win → replaced with 400ms bg fade to `paperGlow`
- Seal stamp → no spring; fade in over 200ms
- Ink settle on digit placement → instant, no animation

The celebration is still meaningful, just quieter.

## 6. Color blindness

Sumi's color coding:
- **Red** = today / done → also marked with seal shape + "完" kanji + caps label "TODAY"
- **Teal** = user entry → also a different font family (Caveat vs Cormorant); colorblind users see the font difference
- **Gold** = Pro → also marked with the wordmark + "Pro" label

**Never rely on color alone.** Test screens with deuteranopia / protanopia / tritanopia simulators in Figma or Xcode Accessibility Inspector.

## 7. Dynamic Type / Font scale

See `ADAPTIVE.md §2`. Support **0.85× to 2.0×** without clipping. Use `sp` for every text size, not `dp`. No `letterSpacing` values over 0.3em at 2.0× scale — widen letters too much and they break word shapes.

## 8. Haptics & sound

- Haptics respect system haptics setting
- Sound respects system mute + in-app toggle
- Never use sound OR haptics as the only signal for an event — always accompany with a visual change

## 9. Captions & alternative text

- No videos / audio stories in v1, so no captions needed
- If onboarding grows an intro video: ship with captions, off by default, toggle in control bar
- All images that carry meaning have alt text (currently none — the Enso is meaningful but the `LogoEnso` composable uses `contentDescription = "Sumi logo"`)

## 10. Platform conventions

- **iOS:** respect VoiceOver rotors, Dynamic Type, Bold Text, Increase Contrast, Reduce Motion, Reduce Transparency (turn off washi noise).
- **Android:** respect TalkBack, font-scale, `prefersReducedMotion`, high-contrast text mode.
- **Desktop (if shipped):** keyboard shortcuts documented in-app, Escape always closes overlays.

## 11. Quick audit checklist

When reviewing a screen, check all of:

- [ ] Every text pair meets AA in both modes
- [ ] Every touch target ≥ 48.dp
- [ ] Every interactive has a `contentDescription` that describes the action
- [ ] Decorative elements are invisible to screen readers
- [ ] Layout holds at 2.0× font scale without clipping
- [ ] Focus order is logical and keyboard-traversable
- [ ] Color is never the only signal (pair with shape, label, font, or position)
- [ ] Motion has a reduced alternative
- [ ] Haptic + sound never the only signal
- [ ] Screen reader dry-run reads sensibly top-to-bottom
