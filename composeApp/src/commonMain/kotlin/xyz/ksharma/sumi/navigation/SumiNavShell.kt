package xyz.ksharma.sumi.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey

/** Window width from which layouts become multi-pane and the bottom bar becomes a rail. */
val ExpandedMinWidth: Dp = 840.dp

/**
 * Width the navigation rail has taken from the window, zero when there is none. Screens add it
 * back to their own width to learn the window class, so they agree with the shell about it.
 */
val LocalNavRailWidth = compositionLocalOf { 0.dp }

/**
 * The tab chrome around the current screen. Compact and medium windows keep the bottom bar;
 * Expanded windows (width >= [ExpandedMinWidth]) put a [SumiNavRail] on the left and give the
 * screen everything to its right. [content] receives the modifier to apply to the screen host.
 */
@Composable
fun SumiNavShell(
    showNav: Boolean,
    currentTab: NavKey,
    onTabClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val rail = showNav && maxWidth >= ExpandedMinWidth
        Row(modifier = Modifier.fillMaxSize()) {
            if (rail) SumiNavRail(currentTab = currentTab, onTabClick = onTabClick)
            // contentWindowInsets = 0 so Scaffold doesn't consume system bar insets.
            // Screens that need inset protection (title bars, bottom nav) handle it themselves.
            Scaffold(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (rail) {
                            // The rail already cleared the start inset; do not pad it twice.
                            Modifier.consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                        } else {
                            Modifier
                        },
                    ),
                contentWindowInsets = WindowInsets(0),
                bottomBar = {
                    if (showNav && !rail) BottomNavBar(currentTab = currentTab, onTabClick = onTabClick)
                },
            ) { innerPadding ->
                CompositionLocalProvider(LocalNavRailWidth provides if (rail) NavRailWidth else 0.dp) {
                    content(Modifier.padding(bottom = innerPadding.calculateBottomPadding()))
                }
            }
        }
    }
}
