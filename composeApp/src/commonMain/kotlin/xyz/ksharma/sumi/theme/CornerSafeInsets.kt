package xyz.ksharma.sumi.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val CORNER_CLEARANCE = 16.dp

/**
 * Widest a screen's content column gets. Wider windows (iPad, iPhone Duo's open display,
 * resizable windows, landscape) centre the column instead of stretching it.
 */
val MaxContentWidth = 640.dp

/** Bottom space for the pinned AdMob banner on screens that scroll underneath it. */
val BannerReserve = 64.dp

/**
 * Caps the width at [MaxContentWidth], filling narrower windows. Place the column with
 * `Alignment.TopCenter` so it centres on wide windows. Depends on window width only.
 */
fun Modifier.contentMaxWidth(): Modifier = widthIn(max = MaxContentWidth).fillMaxWidth()

/**
 * Keeps content out of the left/right system areas: landscape notch or Dynamic Island, and the
 * status-bar + camera column on iPhone Duo's outer display (it sits on the RIGHT edge there).
 *
 * Uses `safeDrawing` restricted to the horizontal sides, not `displayCutout`: Compose on iOS
 * reports `displayCutout` on the top edge only, so it is zero on the Duo's outer display.
 *
 * Apply it to content, never to a background, so backgrounds stay full-bleed.
 */
@Composable
fun Modifier.safeHorizontalPadding(): Modifier =
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))

/**
 * Side insets plus the [MaxContentWidth] cap, centred. For a full-screen column that sits next to
 * (not inside) `WashiBG`: `Modifier.fillMaxSize().safeContentColumn()`.
 */
@Composable
fun Modifier.safeContentColumn(): Modifier =
    safeHorizontalPadding().wrapContentWidth(Alignment.CenterHorizontally).contentMaxWidth()

/**
 * Status bar padding followed by [cornerClearanceTop]. Use this wherever a screen used
 * `windowInsetsPadding(WindowInsets.statusBars)` for its top edge.
 *
 * Deliberately not `safeDrawing` on top: under a parent that already consumed the status bars the
 * gap would double.
 */
@Composable
fun Modifier.statusBarsWithCornerClearance(): Modifier =
    windowInsetsPadding(WindowInsets.statusBars).padding(top = cornerClearanceTop())

/**
 * Extra top padding to add *after* the status bar padding so content stays out of the
 * top-leading display corner. Zero except where the status bar inset is too small to do that job.
 *
 * Phones in portrait have a status bar taller than the clearance, and phones in landscape have a
 * side inset that moves content off the corner. It exists for iPhone Duo's outer display, where
 * the status bar and camera sit on the right edge, so the top and leading insets are both zero.
 */
@Composable
fun cornerClearanceTop(): Dp {
    val statusTop = statusBarTop()
    return if (needsCornerClearance(statusTop)) CORNER_CLEARANCE - statusTop else 0.dp
}

@Composable
private fun statusBarTop(): Dp {
    val density = LocalDensity.current
    return with(density) { WindowInsets.statusBars.getTop(density).toDp() }
}

@Composable
private fun needsCornerClearance(statusTop: Dp): Boolean {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val insets = WindowInsets.systemBars
    val leadingPx = if (layoutDirection == LayoutDirection.Ltr) {
        insets.getLeft(density, layoutDirection)
    } else {
        insets.getRight(density, layoutDirection)
    }
    val leading = with(density) { leadingPx.toDp() }
    return statusTop < CORNER_CLEARANCE && leading < CORNER_CLEARANCE
}
