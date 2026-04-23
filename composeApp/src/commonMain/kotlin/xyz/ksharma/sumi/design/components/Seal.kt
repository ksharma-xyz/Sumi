@file:Suppress("MagicNumber")

package xyz.ksharma.sumi.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.ksharma.sumi.theme.SumiTheme

@Composable
fun Seal(
    modifier: Modifier = Modifier,
    kanji: String = "墨",
    size: Dp = 72.dp,
    rotationDeg: Float = -5f,
) {
    val shape = RoundedCornerShape(6.dp)
    val colors = SumiTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .rotate(rotationDeg)
            .shadow(elevation = 4.dp, shape = shape, ambientColor = colors.redDeep)
            .size(size)
            .background(color = colors.red, shape = shape)
            .border(width = 1.5.dp, color = colors.paper.copy(alpha = 0.5f), shape = shape),
    ) {
        Text(
            text = kanji,
            style = SumiTheme.typography.cjk.copy(
                fontSize = (size.value * 0.52f).sp,
                color = colors.paper,
            ),
        )
    }
}

@Composable
fun SealComplete(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
) {
    Seal(kanji = "完", modifier = modifier, size = size, rotationDeg = -5f)
}

@Composable
fun SealInk(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
) {
    Seal(kanji = "墨", modifier = modifier, size = size, rotationDeg = -3f)
}
