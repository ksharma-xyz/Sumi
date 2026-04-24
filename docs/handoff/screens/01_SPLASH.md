# 01 · Splash

**One-shot intro on cold launch. Max 1.8s. Then auto-advance to Home or Onboarding.**

## ⚠ Do not hand-draw the logo

The splash logo is a **shipped vector asset**. Load it and reveal it. Do **not** animate a `Canvas` stroke, do **not** tween `stroke-dasharray`, do **not** redraw it with `drawArc`. The earlier implementation did this — it rendered a plain circle that did not match the brush feel of the asset. Fix.

**Use this file, and only this file, for the splash mark:**

```
composeApp/src/commonMain/composeResources/drawable/
  └── logo_enso.xml    ← import from handoff/svg/logo-enso.svg
```

Converted to Android Vector Drawable (AVD) or imported as a Compose `ImageVector`. The SVG already contains:
- The hand-painted Ensō arc path (with the open tail)
- A brush-ink displacement filter (texture — this is what made it feel like ink)
- Two small ink droplets at the tail

If the AVD import loses the `feDisplacementMap` filter, **re-export the SVG to a raster PNG at 4×** instead, ship it as `logo_enso.png`, and use `painterResource(Res.drawable.logo_enso)`. Do NOT try to recreate the texture in code. The raster PNG is the fallback.

Export path if you go the raster route:
```
drawable/
  ├── logo_enso.png         560 × 560 (4× of 140.dp)
  └── logo_enso@2x.png      for higher-dpi
```

## Background

`BG-1` (washi paper, light) — see BACKGROUNDS.md.

One decorative ink bleed: `ink_bleed_01.png` at 360.dp, alpha 0.12, centered behind the Ensō. Draw order:

```
z=0  BG-1 (paper + vignette)
z=1  ink_bleed_01.png    (360.dp, centered, alpha 0.12)
z=2  logo_enso           (140.dp, centered)
z=3  kanji 墨             (Shippori 64sp, centered inside the Ensō)
z=4  wordmark Sumi       (Cormorant Italic 46sp, below)
z=5  loading dots        (faint, near bottom)
```

## Layout (390 × 844 reference)

```
┌────────────────────────────┐ <- safe top
│                            │
│                            │
│          [ink bleed]       │  centered, 360.dp
│         ┌─────────┐        │
│         │  ENSŌ   │        │  logo_enso, 140.dp
│         │   墨    │        │  kanji inside, 64sp
│         └─────────┘        │
│                            │  gap 24.dp
│           Sumi             │  wordmark 46sp italic
│                            │
│                            │
│                            │
│      · · · · · · ·         │  dots, 48.dp from bottom
│                            │
└────────────────────────────┘ <- safe bottom
```

Ensō centered horizontally; vertical center at **40% from top**.

## Animation (revised — no manual drawing)

The whole point of using the shipped asset is that it already IS the brush-inked mark. The animation is a **reveal**, not a redraw.

```
t=0ms     paper + ink bleed appear (immediate)
t=0ms     logo_enso starts revealing (see below)
t=700ms   logo_enso reveal complete
t=800ms   kanji 墨 fades in (240ms, Sumi.Ease.paper)
t=1100ms  wordmark "Sumi" fades in (400ms, Sumi.Ease.paper)
          + subtle 2.dp lift (translateY 6.dp → 0)
t=1500ms  loading dots begin pulsing
t=1800ms  transition out to next screen (paperSlide)
```

### Reveal options for the Ensō — pick ONE

**A. Ink-bleed wipe (preferred).** Render `logo_enso` fully painted from t=0, but mask it with a radial alpha gradient that grows from the top-left anchor of the arc outward:

```kotlin
var progress by remember { mutableStateOf(0f) }
LaunchedEffect(Unit) {
    animate(0f, 1f, animationSpec = tween(700, easing = Sumi.Ease.brush)) { v, _ ->
        progress = v
    }
}

Image(
    painter = painterResource(Res.drawable.logo_enso),
    contentDescription = "Sumi",
    modifier = Modifier
        .size(140.dp)
        .graphicsLayer { alpha = 1f }
        .drawWithContent {
            drawContent()
            // Paint over the un-revealed portion with a radial mask
            val r = size.maxDimension * progress
            drawCircle(
                color = Sumi.Color.paper,
                radius = size.maxDimension - r,
                center = Offset(size.width * 0.2f, size.height * 0.2f),
                blendMode = BlendMode.DstIn,
            )
        }
)
```

This makes the brush appear to "bleed into existence" from the top-left anchor — consistent with how ink actually leaves a brush. No stroke-dash trickery.

**B. Alpha + subtle scale.** If A is too complex for the build target, do this instead:
- At t=0, Ensō alpha 0, scale 0.96
- Animate to alpha 1, scale 1 over 700ms with `Sumi.Ease.brush`
- No rotation, no bounce

Option B is acceptable. Option A is the target.

### What NOT to do

- ❌ `drawArc` with animated sweep angle
- ❌ `pathMeasure` + `stroke-dasharray` to "draw" the circle
- ❌ Building the Ensō from two Bezier curves in Compose and animating their path
- ❌ Rotating the logo during reveal
- ❌ Adding a gradient on top of the paper
- ❌ Using `FadeIn + slideInVertically` on the Ensō — it's a brush mark, not a toast

## Reduced motion

If `WindowInsets` / `AccessibilityManager` reports reduced motion:
- Ensō appears fully at t=0, alpha 1, no reveal
- Kanji appears fully at t=0
- Wordmark appears at t=200ms with a simple 200ms fade (no translate)
- Dots do not pulse, just sit at 24% alpha
- Total duration: 800ms then transition

## Wordmark

- Asset option: `handoff/svg/wordmark.svg` — if the font render is problematic, use this vector instead of live text
- Font fallback: Cormorant Garamond Italic weight 600, size 46sp, `Sumi.Color.ink`
- Text: `Sumi` exactly (no period, no decoration)

## Kanji 墨

- Font: Shippori Mincho Medium, 64sp, `Sumi.Color.ink`
- Centered inside the Ensō
- Import from Compose resources as a real glyph — do not use an image of the kanji

## Loading dots

- 7 dots, 3.dp diameter, 8.dp gap, center-aligned
- Base color: `Sumi.Color.ink` at 20% alpha
- Active dot (one at a time, left→right, 900ms loop) ramps to 60% alpha
- Position: 48.dp from bottom safe area
- Hidden entirely in reduced-motion mode

## Resources used (checklist)

| Resource | Path | Required |
|---|---|---|
| `washi_paper_light.png` | `drawable/` | ✓ |
| `ink_bleed_01.png` | `drawable/` | ✓ |
| `logo_enso.xml` OR `logo_enso.png` | `drawable/` | ✓ **one of** |
| Cormorant Garamond Italic 600 | `font/` | ✓ (or use wordmark.svg) |
| Shippori Mincho Medium | `font/` | ✓ |

## Acceptance checklist

- [ ] Splash logo is the shipped asset (`logo_enso`), NOT a `Canvas` redraw
- [ ] The Ensō has the brush-ink texture from the SVG filter — it is not a clean geometric arc
- [ ] The arc has the characteristic **open tail** at the bottom (not a closed circle)
- [ ] Two small ink droplets appear at the tail, inherited from the asset
- [ ] Reveal animation uses alpha/mask, not stroke-dash
- [ ] Kanji 墨 sits centered inside the Ensō, not beside it
- [ ] Wordmark is italic Cormorant, not upright
- [ ] Total splash time ≤ 1.8s; transitions out on `paperSlide`
- [ ] Reduced-motion: no reveal animation, all elements appear near-instantly
- [ ] Paper has visible fibre (it is a PNG, not a flat color)
- [ ] On 340×640, nothing clips; content is centered
