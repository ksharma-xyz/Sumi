# Backgrounds — the authoritative recipe

This is the single source of truth for every background in Sumi. If you read nothing else, read this. The first implementation got this wrong — this doc fixes it with exact values and resource paths.

---

## TL;DR for the LLM implementer

1. **Never** paint a background procedurally with noise generators at render time unless the spec below says so. Use the shipped PNG assets.
2. **Never** invent opacity values. Use the exact values in this doc.
3. **Pause overlay is 82% opaque, not 40%.** If pause looks transparent and you can read the game underneath, it is wrong. Fix.
4. **Paper is warm (#F4ECE0), not white.** If you see white anywhere, it is wrong.
5. **Win screen is paper + faint ink bleed + seal.** Not dark. Not blurred. Not gradient.

---

## The 5 canonical backgrounds

Sumi uses exactly **5** background treatments. Every screen picks one. Do not invent new ones.

| Id | Name | Used on | Resource |
|---|---|---|---|
| **BG-1** | Paper (light) | Splash, Onboarding, Home, Daily, Stats (unlocked), Win | `washi_paper_light.png` |
| **BG-2** | Paper (night) | Same as above in dark mode | `washi_paper_dark.png` |
| **BG-3** | Paper quiet | Game (active solve) — less fibre, more stillness | `washi_paper_light.png` at 70% noise intensity |
| **BG-4** | Ink night | Paywall, Pro screens, Stats (locked) | `washi_ink_dark.png` |
| **BG-5** | Pause scrim | Pause overlay — layered on top of whatever is behind | Composed in code (see §Pause) |

## Resources to ship

Create these PNGs and drop them into:

```
composeApp/src/commonMain/composeResources/drawable/
  ├── washi_paper_light.png      2048 × 2048, sRGB, 8-bit, tileable
  ├── washi_paper_dark.png       2048 × 2048, sRGB, 8-bit, tileable
  ├── washi_ink_dark.png         2048 × 2048, sRGB, 8-bit, tileable
  ├── ink_bleed_01.png           512  × 512,  sRGB + alpha, tileable no
  ├── ink_bleed_02.png           512  × 512,  sRGB + alpha, tileable no
  └── ink_bleed_03.png           512  × 512,  sRGB + alpha, tileable no
```

Use the "How to generate" section below if you don't have them yet.

### How to generate the PNGs

Run this once, commit the PNGs, do not regenerate at runtime.

**`washi_paper_light.png`** — open Photoshop / Affinity / GIMP:
1. Fill 2048×2048 with `#F4ECE0`.
2. Add Noise filter: Gaussian, monochromatic, amount 2.4%.
3. Add Clouds filter (Filter → Render → Clouds) on a new layer at 18% opacity, blend mode Multiply, colors `#E8DCC5` → `#F4ECE0`.
4. Add paper-fibre texture layer: scan any Japanese washi paper OR use a free washi texture, desaturate, set blend mode Overlay at 22%.
5. Seam the edges tileable (Filter → Other → Offset with wraparound, clone-stamp seams).
6. Export 8-bit PNG.

**`washi_paper_dark.png`** — same process with base color `#1A1410`, clouds `#211812` → `#1A1410`, fibre overlay at 18%.

**`washi_ink_dark.png`** — same as dark paper but base `#0F0A07` (deeper), cloud range `#1A1208` → `#0F0A07`, fibre overlay at 15%, add one soft vignette circle at 18% opacity black from the center outward.

**`ink_bleed_*.png`** — 512×512 transparent PNG. Paint 3 concentric soft-edge ink blobs per file, each with seed-varied edge noise. These are decorative ink accents dropped on Win / Paywall / Splash. See INK_BLEED.md section for exact blob anatomy.

If you prefer, generate them in Figma once and export. Do not procedural-noise these every frame at render time — it burns battery and the result is inconsistent across platforms.

---

## BG-1 · Paper (light)

**When:** any light-mode non-game screen.

**Recipe (Compose):**

```kotlin
@Composable
fun WashiBG(
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    variant: WashiVariant = WashiVariant.Full,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val resource = when {
        dark -> Res.drawable.washi_paper_dark
        else -> Res.drawable.washi_paper_light
    }
    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1 — base color (MUST be drawn even under the PNG, in case it fails to load)
        Spacer(
            Modifier.fillMaxSize().background(
                if (dark) Sumi.Color.Night.paper else Sumi.Color.paper
            )
        )
        // Layer 2 — the paper PNG, repeating or contained
        Image(
            painter = painterResource(resource),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = when (variant) {
                WashiVariant.Full   -> 1f
                WashiVariant.Quiet  -> 0.7f   // less texture during gameplay
                WashiVariant.Faint  -> 0.35f
            },
        )
        // Layer 3 — inner vignette ring (warm shadow around the edges)
        Box(
            Modifier.fillMaxSize().drawBehind {
                val color = if (dark) Color(0x660A0704) else Color(0x33645820)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Transparent, color),
                        center = center,
                        radius = size.maxDimension * 0.8f,
                    ),
                )
            }
        )
        content()
    }
}
```

**Exact color tokens used:**
- Base: `Sumi.Color.paper = #F4ECE0`
- Vignette (light): `#645820` at 20% alpha
- Vignette (night): `#0A0704` at 40% alpha

## BG-2 · Paper (night)

Identical composition to BG-1 but uses `washi_paper_dark.png` and warm-dark vignette. The base palette swap is handled by `dark = true` in `WashiBG`.

Warm-dark means: not pure black, not bluish. `#1A1410` base with warm-brown highlights. If dark mode looks cold or blue-tinted, the wrong PNG is loaded.

## BG-3 · Paper quiet (Game)

Same as BG-1 but `WashiVariant.Quiet` — the fibre layer drops to 70%, the vignette radius tightens, and **no ambient PaperBreath animation runs** (see ANIMATIONS.md — breath pauses during solve).

The board is the focus. The background retreats.

## BG-4 · Ink night

**When:** Paywall, Pro-only sections when rendered on dark, Stats locked state.

Same layered model as BG-1 but:
- Base `Sumi.Color.Night.paper = #1A1410`
- PNG `washi_ink_dark.png`
- Vignette stronger: `#000000` at 60% alpha, radius 0.7 of maxDimension

The feeling is "held in shadow", not "blackout". You should still see paper fibre faintly under the gold wordmark.

## BG-5 · Pause scrim (the one that was wrong)

**When:** the Pause overlay modal.

This is NOT a regular background — it is a 3-layer composite drawn on top of the live game beneath.

```
Layer A (bottom):   the live Game screen (frozen), z = 0
Layer B (middle):   a 24.dp gaussian blur of Layer A, z = 1
Layer C (top):      a paper-tinted scrim over Layer B, z = 2
                    then: the kanji 休, "Rest" label, timer, buttons
```

### Exact values

| Layer | Treatment |
|---|---|
| A | Live game snapshot, unchanged |
| B | Apply `Modifier.blur(24.dp, BlurredEdgeTreatment.Unbounded)` — OR draw a captured bitmap through RenderScript / Core Image blur at radius 24 |
| C | A `Box` with `background = Sumi.Color.paper.copy(alpha = 0.82f)` (light mode) OR `Sumi.Color.Night.paper.copy(alpha = 0.88f)` (dark mode) |

**The alpha is 0.82 / 0.88, NOT 0.40.** This is the key fix.

If you can read the board cells behind the pause screen, the scrim is too transparent. The game behind must be a soft, undistinguishable wash of color — like frosted glass holding a memory of the puzzle, not a window onto it.

### Why so opaque?

- Pause is a genuine "step away" moment. The player should feel the game recede.
- The kanji 休 must sit clearly on its own ground.
- At 0.40 alpha the board cells read through and fight the kanji; contrast fails WCAG.
- At 0.82 / 0.88 the blur + scrim reads as tinted frosted glass and contrast passes.

### Compose sketch

```kotlin
@Composable
fun PauseOverlay(
    gameSnapshot: Bitmap,   // or ImageBitmap — captured when pause triggered
    onResume: () -> Unit,
    onExit: () -> Unit,
    dark: Boolean = false,
    elapsedMs: Long,
) {
    Box(Modifier.fillMaxSize()) {
        // Layer A + B — snapshot blurred
        Image(
            bitmap = gameSnapshot.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(24.dp, BlurredEdgeTreatment.Unbounded),
            contentScale = ContentScale.Crop,
        )
        // Layer C — paper scrim
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    if (dark) Sumi.Color.Night.paper.copy(alpha = 0.88f)
                    else Sumi.Color.paper.copy(alpha = 0.82f)
                )
        )
        // Content — kanji, label, timer, buttons
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("休", style = SumiTypography.kanjiHero, color = Sumi.Color.red)
            Spacer(Modifier.height(12.dp))
            Text("A moment of rest", style = SumiTypography.displayM, color = Sumi.Color.ink)
            Spacer(Modifier.height(8.dp))
            Text("Your time is paused. Return when you are ready.",
                 style = SumiTypography.bodyM, color = Sumi.Color.inkSoft,
                 textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            Text(formatElapsed(elapsedMs), style = SumiTypography.displayL.italic(),
                 color = Sumi.Color.ink)
            Spacer(Modifier.height(40.dp))
            SumiButton(onClick = onResume, variant = Primary, modifier = Modifier.fillMaxWidth()) {
                Text("Resume")
            }
            Spacer(Modifier.height(12.dp))
            SumiButton(onClick = onExit, variant = Ghost, modifier = Modifier.fillMaxWidth()) {
                Text("End session")
            }
        }
    }
}
```

**If the Pause overlay you built does not match this screenshot description, rebuild it with these exact values.**

Visual acceptance:
- ✓ Game grid is *perceivable as distant shape*, not readable
- ✓ Body text, buttons, kanji read with clear AA contrast
- ✓ The whole screen reads "warm paper held over game" in light mode, "deep ink held over game" in dark mode

## Ink bleeds

Decorative accents layered on top of the paper background on certain screens. Always as `Image` with alpha, not drawn procedurally.

| Screen | Recipe |
|---|---|
| Splash | 1 bleed (`ink_bleed_01.png`), 360.dp, alpha 0.12, position center-behind-enso |
| Win | 2 bleeds: `ink_bleed_02.png` at top-left corner, 280.dp, alpha 0.08; `ink_bleed_03.png` at bottom-right, 220.dp, alpha 0.10 |
| Paywall | 1 bleed (`ink_bleed_01.png` tinted gold), 400.dp, alpha 0.14, positioned behind the Pro mark |
| Home | None — keep quiet |
| Game | None — absolutely none during active play |
| Pause | None — the blur + scrim is the treatment |
| Daily | 1 very small bleed at top-right, 120.dp, alpha 0.06 |
| Stats (unlocked) | 1 bleed behind the hero number, 200.dp, alpha 0.08 |
| Onboarding | 1 bleed per slide, rotated and sized per slide, alpha 0.08–0.12 |

Implementation: `Image(painter = painterResource(Res.drawable.ink_bleed_02), contentDescription = null, alpha = 0.08f, ...)`. Never `Canvas { drawCircle(...) }` with random seeds — that caused the inconsistency.

## Common mistakes to reject

- ❌ Building the paper texture with live `drawRect` + noise at render time. Use the PNG.
- ❌ Using `Modifier.alpha(0.4f)` on the pause scrim. It must be 0.82 / 0.88.
- ❌ Gradients as "paper". The paper is a texture PNG, not a gradient.
- ❌ Dark mode rendered as a blue-black instead of warm-black. Check the hex — `#1A1410`, not `#111827`.
- ❌ `ContentScale.Fit` on the paper PNG — leaves edges blank. Use `ContentScale.Crop`.
- ❌ Tinting the paper PNG with a `colorFilter` at runtime. Ship two PNGs (light + dark) instead; do not re-tint.
