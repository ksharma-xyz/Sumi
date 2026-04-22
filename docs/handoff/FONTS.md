# Fonts

Five families. All available on Google Fonts (free, OFL).

| Role | Family | Weights needed | Google Fonts |
|---|---|---|---|
| Display (italic) | Cormorant Garamond | 500, 600 italic | https://fonts.google.com/specimen/Cormorant+Garamond |
| Body | Source Serif 4 | 400, 500 | https://fonts.google.com/specimen/Source+Serif+4 |
| UI | Inter | 500, 600, 700 | https://fonts.google.com/specimen/Inter |
| Hand (user digits) | Caveat | 500 | https://fonts.google.com/specimen/Caveat |
| CJK (kanji chops) | Shippori Mincho | 500, 600 | https://fonts.google.com/specimen/Shippori+Mincho |

## Installation

1. Download TTF files for each family from Google Fonts.
2. Drop into `composeApp/src/commonMain/composeResources/font/`.
3. Register via `compose-resources`:

```kotlin
// Theme.kt
@Composable
fun rememberSumiFonts(): SumiFontBundle {
    val display = FontFamily(
        Font(Res.font.cormorant_medium_italic, FontWeight(500), FontStyle.Italic),
        Font(Res.font.cormorant_semibold_italic, FontWeight(600), FontStyle.Italic),
    )
    val body = FontFamily(
        Font(Res.font.source_serif_4_regular, FontWeight(400)),
        Font(Res.font.source_serif_4_medium, FontWeight(500)),
    )
    val ui = FontFamily(
        Font(Res.font.inter_medium, FontWeight(500)),
        Font(Res.font.inter_semibold, FontWeight(600)),
        Font(Res.font.inter_bold, FontWeight(700)),
    )
    val hand = FontFamily(Font(Res.font.caveat_medium, FontWeight(500)))
    val cjk = FontFamily(
        Font(Res.font.shippori_mincho_medium, FontWeight(500)),
        Font(Res.font.shippori_mincho_semibold, FontWeight(600)),
    )
    return SumiFontBundle(display, body, ui, hand, cjk)
}
```

## License

All five are SIL Open Font License 1.1 — free to bundle and redistribute. Include `OFL.txt` in your app's attributions screen.

## Subsetting (optional, for app size)

- Cormorant: Latin + Latin Extended
- Source Serif 4: Latin + Latin Extended
- Inter: Latin
- Caveat: Latin (digits 0–9 are the only characters used)
- Shippori Mincho: Japanese subset + the three kanji 墨 休 完 at minimum

Use `fonttools` or `glyphhanger` to subset before ship.
