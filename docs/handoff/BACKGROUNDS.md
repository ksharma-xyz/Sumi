# Backgrounds, Textures & Paper Noise

This doc covers all the "not quite flat" surfaces in Sumi — the washi noise, ink bleeds, vignettes, edge foxing, faint grid watermarks, and seasonal ambient layers. These are what make the app feel like a *printed object* rather than a screen. In the HTML reference they're drawn live with SVG filters (`feTurbulence` + `feDisplacementMap`). On a real mobile app, you have three options per surface; this doc tells you **which to use where**, and how each behaves in light vs. dark mode.

---

## The layer stack (mental model)

Every Sumi screen composes up to **five layers**, back to front. Most screens use 2–3. Never more than five.

```
┌──────────────────────────────────────────┐
│ 5. Ink bleeds / seals      (optional, decorative)
│ 4. Edge vignette           (always — soft inner shadow)
│ 3. Ambient tint            (optional — red/teal/gold wash)
│ 2. Paper noise / fibre     (always — the washi texture)
│ 1. Flat paper / ink fill   (always — Sumi.Color.paper | Night.paper)
└──────────────────────────────────────────┘
```

Layers 1–2–4 together = the `WashiBG` composable. Layer 3 is sometimes added per screen; layer 5 is used only on Win, Paywall, and Splash.

---

## Layer 1 · Flat fill

Just `Sumi.Color.paper` (light) or `Sumi.Color.Night.paper` (dark). No gradient, no stops. This is the base.

---

## Layer 2 · Paper noise (the washi fibre)

This is the most important texture in the app — it's what sells "paper". Every full-screen surface has it.

### What it looks like

Fine, non-repeating fibre grain: cloudy low-frequency base (`baseFrequency ~0.85`) + a subtle high-frequency overlay (`baseFrequency ~3.2`) giving occasional fibre specks. Not a uniform static-noise hiss. Think: cotton washi under a loupe.

### Light mode

- Base: warm beige Perlin noise multiplied onto `paper`.
- Tint: slight brown-gold tone (`~#C9B48A` at 8% alpha through a `multiply` blend).
- Intensity: `0.6–0.8` of full noise strength.

### Dark mode

- Base: warm-dark Perlin noise multiplied onto `Night.paper`.
- Tint: dark-umber undertone (`~#3D3022` at 18% alpha, lightly screened).
- Intensity: `0.4–0.5` — more restrained so the grain doesn't shimmer on OLED.

### Three ways to produce it (pick one per platform target)

**Option A — Pre-baked PNGs (recommended for v1).** Simplest, most reliable, no runtime cost.

- Generate two 2048×2048 tileable PNGs: `washi_paper_light.png`, `washi_paper_dark.png`.
- Use Photoshop / Affinity / Procreate / `imagemagick`, or run the SVG noise filter in a browser and screenshot a 2K crop, then tile-clean it.
- Prompt for an image-gen LLM (Midjourney / SDXL / DALL-E):
  > *"Seamless 2K tileable texture of warm cream washi paper. Subtle plant-fibre specks and cotton grain. Very soft, uniform, muted. Warm beige `#F4ECE0` base, gentle darker flecks of `#C9B48A`. No watermark, no text, no strong lighting, no creases. Render as a flat top-down scan."*
  > *Dark variant:* *"Same seamless washi paper texture but in warm black ink tones — base `#1A1410`, fibre specks `#3D3022`. Still warm, not cold. Feels like paper at night by lamplight, never like a screen."*
- Drop into `composeResources/drawable/`. Tile with `TileMode.Repeated` and apply at ~30–40% alpha on top of the flat fill.

**Option B — Procedural Perlin noise at runtime.** More work, but infinite variation (no tile seams) and theoretically perfect DPI.

- In `commonMain`, generate a 256×256 noise bitmap at first composition. Seed from `stableRandom(0)`. Cache in a `remember`.
- Use Kotlin noise lib like [`kotlin-noise`](https://github.com/SudoPlayGames/JNoise) (JVM-only — write an `expect`/`actual` wrapper; iOS can use a direct port of Ken Perlin's reference impl, it's ~100 LOC).
- Draw as a `Brush.bitmap(...)` with `TileMode.Mirror`.
- Regenerate only on theme switch, never on every frame.

**Option C — SVG filter via `compose-resources` loadSvg.** Only on Android/desktop; iOS's Compose SVG loader ignores `feTurbulence`. Avoid for MVP.

### Which to pick

Ship **Option A** for v1. It's a day of texture work and zero runtime cost. If the grain ever feels flat or users notice tiling, upgrade to Option B in a later release.

---

## Layer 3 · Ambient tint (screen-specific)

A very soft colored wash that sits above the noise but below the vignette. Used sparingly to set emotional temperature per screen.

| Screen | Tint | Light alpha | Dark alpha | Position |
|---|---|---|---|---|
| Home | none | — | — | — |
| Game (active) | none | — | — | — |
| Win | `red` | 4% | 8% | Radial from center |
| Pause | `teal` | 3% | 6% | Radial from top |
| Paywall | `gold` | 5% | 10% | Radial from top-left |
| Splash | `red` | 3% | 5% | Radial from center-bottom (behind seal area) |
| Daily | none | — | — | — |
| Stats (Pro unlocked) | `gold` | 3% | 6% | Top-right corner bloom |
| Onboarding (slide 4) | seasonal | 4% | 8% | Radial from off-screen edge |

Implementation: a `Box` with `Modifier.background(Brush.radialGradient(...))` over the noise layer. Compose radial brush takes `colors`, `center`, `radius` — keep radius ~1.4× screen diagonal so the gradient is a very slow falloff.

**Prompt for image-gen if you'd rather bake these:**
> *"A soft radial bloom of warm vermilion red (#A8342A) at the center fading to transparent. Subtle, like candlelight through paper. 2K square. Used as an overlay, so the surrounding area is pure transparent PNG. No hard edges."*

---

## Layer 4 · Edge vignette

Every full-screen surface has a soft inner shadow — the feeling of looking *into* the paper, not at it. This also grounds content away from the screen edges.

### Light mode

- Inset shadow, 60.dp soft blur, color `rgba(100, 70, 30, 0.18)` (warm brown, not black).
- Stronger at corners (radial falloff) than edges.

### Dark mode

- Inset shadow, 60.dp soft blur, color `rgba(0, 0, 0, 0.42)`.
- Slightly more pronounced — deepens the lamplight feel.

### Implementation

Compose doesn't have a built-in inset shadow modifier. Draw it manually with a `Canvas` that fills the Box bounds and strokes a soft-blurred path just inside, or use a 9-patch PNG overlay (`vignette_light.9.png` / `vignette_dark.9.png`).

Prompt if you bake it:
> *"Transparent PNG vignette overlay for a mobile screen, 1080×2400 portrait. Soft inner shadow around all edges, strongest in corners, fading to fully transparent in the center 60% of frame. Color: warm brown rgba(100,70,30,0.18). No hard edges. Intended as a multiply overlay."*

---

## Layer 5 · Ink bleeds, seals, foxing (decorative)

These are the scattered ink spots and age marks that appear on specific screens. They are **not** random — each has a fixed position and seed to be reproducible between sessions.

### Ink bleeds

Small irregular ink-blots at 15–25% opacity. Placed off-grid so they feel accidental.

- **Home:** one small bleed behind the daily card's eyebrow. `size=80.dp, seed=1, opacity=0.18`.
- **Win:** a larger one behind the seal stamp. `size=160.dp, seed=3, opacity=0.22`.
- **Paywall:** a gold bleed in the top-right. `size=220.dp, seed=4, opacity=0.20`.
- **Splash:** a red bleed at the exact pixel under where the seal will land. `size=120.dp, seed=7, opacity=0.25`.

Implementation: the `InkBleed` composable (see `COMPONENTS.md`). Three concentric circles displaced by Perlin noise.

### Seals (chops)

Red square stamps with kanji. Only on:
- **Splash** — the "墨" chop lands at t=900ms under the Enso.
- **Win** — the "完" chop stamps into center at completion.
- **Salon** (Pro) — small red "墨会" chop in the register header.

See `Seal` in `COMPONENTS.md`.

### Edge foxing (optional, recommended for Paywall + Win only)

The faint brown age-specks you see on old paper. Irregularly scattered 2–4.dp dots at 20–30% opacity using `paperEdge`.

- 25–40 foxing specks per screen, fixed positions by seed.
- Never near the center. Always within 40.dp of an edge.
- Light mode: `rgba(139, 107, 74, 0.22)`
- Dark mode: `rgba(110, 90, 55, 0.35)`

Bake as a transparent PNG overlay (`foxing_light.png` / `foxing_dark.png`) or draw programmatically with seeded random.

**Prompt for bake:**
> *"Transparent PNG overlay, 1080×2400. Scattered tiny brown specks and foxing marks (old paper age spots), concentrated near edges and corners, almost none in the center. 30 specks, each 2–6 pixels, color rgba(139,107,74,0.22). Feels like a 50-year-old paperback. Pure transparent elsewhere."*

---

## Screen-by-screen background recipes

Exactly which layers each screen uses.

### Splash
1. Flat `paper`
2. Washi noise at 0.8 intensity
3. Red radial tint (3% light / 5% dark) from center-bottom
4. Vignette
5. One large red ink bleed under seal position
6. Falling petals (optional first-run only — see `ANIMATIONS.md`)

### Onboarding
1. Flat `paper`
2. Washi noise at 0.7
3. Seasonal tint on slide 4 only
4. Vignette
5. (No ink bleeds)

### Home
1. Flat `paper`
2. Washi noise at 0.8
3. (No ambient tint)
4. Vignette
5. One small ink bleed behind daily card eyebrow
6. Seasonal border bloom at very low intensity (see `ANIMATIONS.md` — `PaperBreath`)

### Game (active solve)
1. Flat `paperGlow` (a hair lighter than `paper` — the board is elevated)
2. Washi noise at **0.4** (reduced — don't distract from numerals)
3. (No ambient tint)
4. Vignette at 60% normal strength
5. (No ink bleeds on the board area — sacred)

Board background specifically uses `paperGlow` to visually lift it above the surrounding paper. The surrounding chrome (timer, tools) sits on normal `paper`.

### Win
1. Flat `paper`
2. Washi noise at 0.8
3. Red radial tint at 4% light / 8% dark from center
4. Vignette
5. Large red ink bleed behind seal
6. Seal "完" stamps in
7. Optional: foxing overlay at 50% for extra "old puzzle book" feel

### Pause
1. Gaussian-blurred snapshot of the paused game (16.dp blur)
2. `paper` overlay at 88% alpha (this becomes the "paper" layer)
3. Washi noise at 0.5 on top of that
4. Teal radial tint 3% from top
5. Vignette

### Daily
1. Flat `paper`
2. Washi noise at 0.8
3. (No ambient tint)
4. Vignette
5. The heatmap cells themselves use a mini-noise texture (smaller scale) to differentiate from flat color

### Stats (Pro)
1. Flat `paper`
2. Washi noise at 0.8
3. Gold radial bloom from top-right (3% light / 6% dark)
4. Vignette
5. (No ink bleeds)

### Paywall
1. Flat `Night.paper` (always dark)
2. Washi noise at 0.5 (dark mode intensity)
3. Gold radial bloom from top-left at 5% light / 10% dark
4. Vignette (dark, stronger)
5. Gold ink bleed top-right
6. Foxing overlay for texture

---

## Dark mode translation rules

When the active theme is dark, every layer swaps by rule (not by invert):

| Layer | Light mode | Dark mode |
|---|---|---|
| 1 flat | `paper` `#F4ECE0` | `Night.paper` `#1A1410` |
| 2 noise | beige fibre PNG, 35% alpha | umber fibre PNG, 25% alpha |
| 3 red tint | `rgba(168,52,42,0.04)` | `rgba(232,74,62,0.08)` |
| 3 gold tint | `rgba(138,107,42,0.05)` | `rgba(217,168,85,0.10)` |
| 3 teal tint | `rgba(42,90,110,0.03)` | `rgba(111,168,188,0.06)` |
| 4 vignette | warm-brown inner shadow 18% | pure warm-black inner shadow 42% |
| 5 bleeds | use night accent variants | use night accent variants |
| 5 foxing | beige specks 22% | warmer beige specks 35% |

The rule: dark mode tints are **more saturated** and **more alpha** than light, because warm-dark surfaces absorb color more than paper does. A 3% red wash on cream looks equivalent to a 6–8% red wash on sumi-black.

---

## Asset list (to build)

If you're doing Option A (baked PNGs), this is your texture-asset shopping list. Deliver as `composeResources/drawable/` PNGs.

| File | Size | Role | Notes |
|---|---|---|---|
| `washi_paper_light.png` | 2048×2048 | Light washi fibre | Seamless tileable, ~400KB |
| `washi_paper_dark.png` | 2048×2048 | Dark washi fibre | Seamless tileable |
| `washi_paper_glow.png` | 2048×2048 | Slightly brighter board surface | Lighter than the default |
| `vignette_light.9.png` | 1080×2400 | Soft inner shadow, brown | 9-patch |
| `vignette_dark.9.png` | 1080×2400 | Soft inner shadow, black | 9-patch |
| `foxing_light.png` | 1080×2400 | Age specks overlay | Transparent PNG |
| `foxing_dark.png` | 1080×2400 | Age specks overlay for night | Transparent PNG |
| `bloom_red.png` | 2048×2048 | Soft red radial bloom | Center, alpha falloff |
| `bloom_gold.png` | 2048×2048 | Soft gold radial bloom | For Paywall/Stats |
| `bloom_teal.png` | 2048×2048 | Soft teal radial bloom | For Pause |
| `bloom_seasonal_spring.png` | 2048×2048 | Soft pink bloom | Home seasonal tint |
| `bloom_seasonal_summer.png` | 2048×2048 | Soft green bloom | |
| `bloom_seasonal_autumn.png` | 2048×2048 | Soft maple-red bloom | |
| `bloom_seasonal_winter.png` | 2048×2048 | Soft indigo bloom | |
| `petal_shape_1.png` through `petal_shape_5.png` | 256×256 | Cherry-petal silhouettes | Transparent, for PetalFall |

**Total asset weight budget:** ≤3 MB. If exceeded, subsample to 1024×1024 (noise still reads fine at scale).

---

## Prompt cookbook (for image-gen LLMs)

Paste these into Midjourney / SDXL / DALL-E / Firefly to generate the source art. Then clean up in Affinity / Photoshop — remove watermarks, retouch any obvious repeats, ensure tileability.

### Washi paper (light)
> *Seamless 2048×2048 tileable texture. Warm cream washi paper, `#F4ECE0` base, visible plant-fibre specks in `#C9B48A`, subtle cotton grain, very soft and muted. Flat top-down scan, no lighting, no shadows, no creases, no text, no watermark. Uniform fibre distribution — no clustering.*

### Washi paper (dark)
> *Seamless 2048×2048 tileable texture of the same washi paper but dyed in sumi ink. Warm black `#1A1410` base, fibre specks in `#3D3022`, preserves the fibre grain from a light scan. Feels like paper lit by a paper lantern, never cool or blue. Flat scan, no lighting, no watermark.*

### Ambient bloom (template)
> *Transparent 2048×2048 PNG. A soft radial bloom of [COLOR] centered at [POSITION], fading to fully transparent by 80% radius. Extremely gentle, like candlelight through paper. No hard edges, no noise, no text. Transparent background.*

Where `[COLOR]` is the accent hex + 25% alpha, and `[POSITION]` is `center` / `top-left` / `bottom-right` etc.

### Foxing overlay
> *Transparent 1080×2400 portrait PNG overlay. Scattered small brown age specks (paper foxing), 25–40 dots total, 2–6 pixels each, color `rgba(139,107,74,0.25)`, concentrated near the edges and corners, virtually none in the center 60% of the frame. Feels like a 50-year-old paperback. Pure transparent elsewhere.*

### Petal shape
> *Silhouette of a single cherry blossom petal on pure transparent background, 256×256 PNG. Soft rounded teardrop shape, slight curl at the tip, pale pink `#F0D4D0` with gentle inner shadow for dimensionality. No stem. Rotated 15 degrees. Soft edges, photorealistic but minimal.*

---

## Performance notes

- **All PNG backgrounds decode on first compose** — preload during splash so the paint is ready at first frame of home.
- **Never animate the washi noise during an active solve.** Ambient breath only runs on home / pause / splash. In-game, the board stays absolutely still.
- **Vignette as 9-patch, not fullscreen PNG** — saves memory on large devices.
- **Bloom PNGs can be 512×512** and stretched; they're soft radial gradients and don't lose fidelity.

---

## Quick reference

| Surface | Layers used |
|---|---|
| Splash | 1, 2, 3 (red), 4, 5 (bleed + seal + petals) |
| Home | 1, 2, 4, 5 (small bleed) + breath |
| Game (chrome) | 1, 2, 4 |
| Game (board area) | paperGlow, noise 0.4, no vignette |
| Win | 1, 2, 3 (red), 4, 5 (bleed + seal + foxing) |
| Pause | blur-snapshot, paper-overlay, 2, 3 (teal), 4 |
| Daily | 1, 2, 4 |
| Stats (Pro) | 1, 2, 3 (gold), 4 |
| Paywall | 1 (Night), 2 (dark), 3 (gold), 4, 5 (bleed + foxing) |
| Onboarding | 1, 2, 3 (seasonal on slide 4), 4 |

If a screen isn't in this table, it inherits Home's recipe.
