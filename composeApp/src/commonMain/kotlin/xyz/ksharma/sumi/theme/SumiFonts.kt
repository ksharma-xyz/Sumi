package xyz.ksharma.sumi.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

// TODO: Download these fonts from Google Fonts and drop TTFs into
//       composeApp/src/commonMain/composeResources/font/ then replace
//       the FontFamily.* fallbacks below with Res.font.* equivalents:
//
// Cormorant Garamond — 500 italic, 600 italic
//   → cormorant_garamond_medium_italic.ttf
//   → cormorant_garamond_semibold_italic.ttf
//
// Source Serif 4 — 400, 500
//   → source_serif_4_regular.ttf
//   → source_serif_4_medium.ttf
//
// Inter — 500, 600, 700
//   → inter_medium.ttf
//   → inter_semibold.ttf
//   → inter_bold.ttf
//
// Caveat — 500
//   → caveat_medium.ttf
//
// Shippori Mincho — 500, 600
//   → shippori_mincho_medium.ttf
//   → shippori_mincho_semibold.ttf
//
// After adding files, run: ./gradlew generateCommonMainResourceAccessors
// Then replace below with:
//   import xyz.ksharma.sumi.composeapp.generated.resources.Res
//   import xyz.ksharma.sumi.composeapp.generated.resources.*
//   Font(Res.font.cormorant_garamond_medium_italic, FontWeight.Medium, FontStyle.Italic)

data class SumiFontBundle(
    val display: FontFamily,
    val body: FontFamily,
    val ui: FontFamily,
    val hand: FontFamily,
    val cjk: FontFamily,
)

@Composable
fun rememberSumiFonts(): SumiFontBundle = remember {
    SumiFontBundle(
        display = FontFamily.Serif,
        body    = FontFamily.Serif,
        ui      = FontFamily.SansSerif,
        hand    = FontFamily.Cursive,
        cjk     = FontFamily.Default,
    )
}
