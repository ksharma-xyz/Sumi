package xyz.ksharma.sumi.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.ui.theme.AppTheme

/** iPhone Duo outer display in dp (1398 x 2034 px at 3 px/dp). */
const val DUO_OUTER_WIDTH_DP = 466
const val DUO_OUTER_HEIGHT_DP = 678

/** iPhone Duo inner display in dp (2007 x 2853 px at 3 px/dp). */
const val DUO_INNER_WIDTH_DP = 669
const val DUO_INNER_HEIGHT_DP = 951

/** A 10-inch Android tablet in dp (1280 x 800 landscape). */
const val TABLET_LONG_DP = 1280
const val TABLET_SHORT_DP = 800

/**
 * Lays [content] out in a window of exactly [widthDp] x [heightDp], whatever size the capture
 * frame is, so layouts that react to window size can be snapshotted at iPhone Duo or iPad sizes.
 *
 * The snapshot harness captures every preview at one fixed device size and ignores a preview's
 * own `widthDp` / `heightDp`. This works around that by shrinking the density until the window
 * fits the frame: the layout still sees the full dp size, the capture is just a scaled-down
 * picture of it. Window insets are not simulated (they are zero under Robolectric).
 */
@Composable
fun PreviewWindow(
    widthDp: Int,
    heightDp: Int,
    modifier: Modifier = Modifier,
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color(FRAME_COLOR))) {
        val density = LocalDensity.current
        val scale = minOf(maxWidth.value / widthDp, maxHeight.value / heightDp, 1f)
        CompositionLocalProvider(LocalDensity provides Density(density.density * scale, density.fontScale)) {
            AppTheme(dark = dark) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .requiredSize(widthDp.dp, heightDp.dp)
                        .background(SumiTheme.colors.paper),
                ) {
                    content()
                }
            }
        }
    }
}

private const val FRAME_COLOR = 0xFF2A2A2A
