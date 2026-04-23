package xyz.ksharma.sumi.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import xyz.ksharma.sumi.resources.Res
import xyz.ksharma.sumi.resources.caveat_medium
import xyz.ksharma.sumi.resources.cormorant_garamond_medium
import xyz.ksharma.sumi.resources.cormorant_garamond_medium_italic
import xyz.ksharma.sumi.resources.cormorant_garamond_semibold
import xyz.ksharma.sumi.resources.cormorant_garamond_semibold_italic
import xyz.ksharma.sumi.resources.inter_bold
import xyz.ksharma.sumi.resources.inter_medium
import xyz.ksharma.sumi.resources.inter_semibold
import xyz.ksharma.sumi.resources.shippori_mincho_medium
import xyz.ksharma.sumi.resources.shippori_mincho_semibold
import xyz.ksharma.sumi.resources.source_serif_4_medium
import xyz.ksharma.sumi.resources.source_serif_4_regular

@Composable
fun rememberSumiFonts(): SumiFontBundle {
    val display = FontFamily(
        Font(Res.font.cormorant_garamond_medium_italic, FontWeight.Medium, FontStyle.Italic),
        Font(Res.font.cormorant_garamond_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),
        Font(Res.font.cormorant_garamond_medium, FontWeight.Medium),
        Font(Res.font.cormorant_garamond_semibold, FontWeight.SemiBold),
    )
    val body = FontFamily(
        Font(Res.font.source_serif_4_regular, FontWeight.Normal),
        Font(Res.font.source_serif_4_medium, FontWeight.Medium),
    )
    val ui = FontFamily(
        Font(Res.font.inter_medium, FontWeight.Medium),
        Font(Res.font.inter_semibold, FontWeight.SemiBold),
        Font(Res.font.inter_bold, FontWeight.Bold),
    )
    val hand = FontFamily(Font(Res.font.caveat_medium, FontWeight.Medium))
    val cjk = FontFamily(
        Font(Res.font.shippori_mincho_medium, FontWeight.Medium),
        Font(Res.font.shippori_mincho_semibold, FontWeight.SemiBold),
    )
    return remember(display, body, ui, hand, cjk) {
        SumiFontBundle(display, body, ui, hand, cjk)
    }
}
