# Components — Compose API Contracts

Minimum surface per component. Match the HTML reference (`handoff/reference/sumi/primitives.jsx`, `board.jsx`, `icons.jsx`, `logos.jsx`) unless called out here.

All composables live in `composeApp/src/commonMain/kotlin/com/sumi/design/components/`.

---

## WashiBG

Paper-texture background. Every full-screen surface uses this as the base.

```kotlin
@Composable
fun WashiBG(
    modifier: Modifier = Modifier,
    dark: Boolean = false,
    intensity: Float = 1f,   // 0..1 — how strong the fibre texture reads
    content: @Composable BoxScope.() -> Unit = {},
)
```

Implementation:
- Base fill: `Sumi.Color.paper` (or `Night.paper` if dark).
- Two layered fibre/cloud noise passes drawn via `Canvas`.  Use `Modifier.drawBehind` + `drawIntoCanvas` with a `NativePaint` configured to a `ShaderBrush` wrapping a generated Perlin noise bitmap (commonMain-safe). For speed, generate the noise once at first composition, cache in a `remember` + `SideEffect`. If noise is expensive on first frame, you can fall back to a pre-rendered PNG under `composeResources/drawable/washi_paper_light.png` / `_dark.png`.
- An inner shadow ring for vignette: soft `0px 0px 60.dp` inset, color `rgba(100,70,30,0.18)` light / `rgba(0,0,0,0.4)` dark.

Reference: `reference/sumi/primitives.jsx` → `WashiBG`.

---

## SumiButton

```kotlin
enum class SumiButtonVariant { Primary, Ghost, Subtle, Red }
enum class SumiButtonSize { Sm, Md, Lg }

@Composable
fun SumiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SumiButtonVariant = SumiButtonVariant.Primary,
    size: SumiButtonSize = SumiButtonSize.Md,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
)
```

Visuals per variant:

| Variant | Background | Fg | Border |
|---|---|---|---|
| Primary | `ink` | `paper` | 1dp ink |
| Ghost | transparent | `ink` | 1dp ink |
| Subtle | `paperWarm` | `ink` | 1dp paperEdge |
| Red | `red` | `paper` | 1dp red |

Sizes: `Sm=8×14 (11sp)`, `Md=14×20 (13sp)`, `Lg=18×24 (14sp)`.
Text is UPPERCASE + letter-spacing 0.2em (`uiButton` role).
Disabled: 40% alpha, no pointer.
Corner radius: `Sumi.Radius.xs` (2.dp — NOT pill).

---

## SumiChip

```kotlin
enum class SumiChipTone { Ink, Red, Teal, Gold, Muted }

@Composable
fun SumiChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: SumiChipTone = SumiChipTone.Ink,
)
```

3×10dp padding, 1dp border, tone-colored text, transparent bg, UPPERCASE tiny-caps (`uiLabel` style, 10sp, tracking 0.25em).

---

## InkBleed

Decorative ink-blot accent.

```kotlin
@Composable
fun InkBleed(
    modifier: Modifier = Modifier,
    color: Color = Sumi.Color.ink,
    size: Dp = 80.dp,
    opacity: Float = 0.2f,
    seed: Int = 0,
)
```

Three concentric circles with increasing opacity, displaced by a noise texture (seeded Perlin). Drawn in `Canvas`. Pointer events: none.

---

## BrushStroke

Hand-painted underline / divider, rendered as two overlapping quadratic paths.

```kotlin
@Composable
fun BrushStroke(
    modifier: Modifier = Modifier,
    color: Color = Sumi.Color.ink,
    width: Dp = 120.dp,
    height: Dp = 10.dp,
)
```

---

## Seal

Red chop-stamp with kanji.

```kotlin
@Composable
fun Seal(
    kanji: String = "墨",
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    rotationDeg: Float = -5f,
)
```

Solid `red` fill, 6.dp radius, inner paper-colored 1.5dp border inset 4.dp, kanji centered in Shippori Mincho at 52% of size, `elevation.seal` shadow (4dp red-deep offset + 10dp ambient). Soft noise overlay at 40% with `BlendMode.Overlay`.

---

## QuoteRule

Horizontal divider with centered kanji ornament (default 墨).

```kotlin
@Composable
fun QuoteRule(
    modifier: Modifier = Modifier,
    ornament: String = "墨",
    color: Color = Sumi.Color.paperEdge,
)
```

`Row`: 1dp line — 14.dp gap — CJK ornament (16sp, `inkFaint`) — 14.dp gap — 1dp line.

---

## SumiEyebrow

Tiny UPPERCASE label above a section title.

```kotlin
@Composable
fun SumiEyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Sumi.Color.red,
)
```

10sp, semibold, tracking 0.28em, uppercase. Use above h2/h3 titles.

---

## SumiBoard (the Sudoku board)

Full spec in `BOARD.md`. Short signature:

```kotlin
@Composable
fun SumiBoard(
    state: BoardState,
    modifier: Modifier = Modifier,
    cellSize: Dp = Sumi.Layout.cellSize,
    dark: Boolean = false,
    showNotes: Boolean = true,
    sweep: BoardSweep? = null,  // null | Row(idx) | Col(idx) | Box(idx) | Win
    onCellTap: ((r: Int, c: Int) -> Unit)? = null,
)
```

---

## Logos — as composables

Each of the 5 marks is a composable taking `size` + colors. Implementation strategy: ship them as **Compose `ImageVector`** (built with `ImageVector.Builder`) for infinite scaling, OR as SVG resources loaded via `compose-resources`. ImageVector is preferred for the 5 logos — see `handoff/svg/*.svg` for the drawing source.

```kotlin
@Composable fun LogoEnso(size: Dp = 120.dp, color: Color = Sumi.Color.ink, modifier: Modifier = Modifier)
@Composable fun LogoGrid(size: Dp = 120.dp, color: Color = Sumi.Color.ink, accent: Color = Sumi.Color.red, modifier: Modifier = Modifier)
@Composable fun LogoChop(size: Dp = 120.dp, bg: Color = Sumi.Color.red, fg: Color = Sumi.Color.paper, modifier: Modifier = Modifier)
@Composable fun LogoNine(size: Dp = 120.dp, color: Color = Sumi.Color.ink, modifier: Modifier = Modifier)
@Composable fun LogoStrokes(size: Dp = 120.dp, color: Color = Sumi.Color.ink, accent: Color = Sumi.Color.red, modifier: Modifier = Modifier)
@Composable fun LogoWordmark(scale: Float = 1f, color: Color = Sumi.Color.ink, accent: Color = Sumi.Color.red, modifier: Modifier = Modifier)
```

The Enso has a subtle noise-displacement filter in the HTML original. In Compose, approximate by drawing the arc with a slight `PathEffect.cornerPathEffect` + a jittered stroke using `drawPath` with a custom `Stroke(width, pathEffect = StampedPathEffect(...))` OR accept a clean vector (shipping difference is imperceptible at app icon sizes).

---

## Icons — 24 stroke icons

All icons live under `composeApp/src/commonMain/kotlin/com/sumi/design/icons/` as `ImageVector` constants or file-per-icon.

Spec:
- `viewBox` 24×24
- stroke 1.8.dp, `StrokeCap.Round`, `StrokeJoin.Round`
- fill transparent
- color `Color.Unspecified` → tints via `LocalContentColor` or explicit

24 icons (full list):

| Name | Glyph |
|---|---|
| IconBack | `M15 5l-7 7 7 7` |
| IconClose | `M6 6l12 12M18 6L6 18` |
| IconMenu | `M4 7h16M4 12h16M4 17h16` |
| IconMore | 3 dots |
| IconPause | `M8 5v14M16 5v14` |
| IconPlay | `M7 5l12 7-12 7z` |
| IconUndo | arrow + arc |
| IconErase | eraser |
| IconBrush | calligraphy brush |
| IconLantern | lantern |
| IconNote | 3×3 dot grid |
| IconFlame | streak flame |
| IconCalendar | calendar |
| IconChart | line chart |
| IconTrophy | trophy |
| IconCheck | check |
| IconBook | open book |
| IconQuote | quote marks |
| IconSettings | gear |
| IconShare | share arrow |
| IconSparkle | 8-pt sparkle |
| IconHeart | heart |
| IconLock | padlock |
| IconSound | speaker |

Full paths in `reference/sumi/icons.jsx`. You can also use the exported SVGs in `svg/icons/` as a reference.

---

## Cards / sheets

No named composable — use `Box` + `Modifier.background(paperWarm)` + `Modifier.border(1.dp, paperEdge)` + `Modifier.padding(20.dp)`. Corner radius always `xs` or `none`.

---

## Reminder

Every component should feel like paper and ink. If Compose's default behavior contradicts that (ripple, elevation shadow, pill shape), override it explicitly. See `DESIGN_PRINCIPLES.md §1`.
