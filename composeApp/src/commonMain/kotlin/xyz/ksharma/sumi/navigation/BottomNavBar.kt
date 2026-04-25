package xyz.ksharma.sumi.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import xyz.ksharma.sumi.design.components.SumiIcon
import xyz.ksharma.sumi.design.icons.SumiIcons
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

private data class NavTab(val route: NavKey, val label: String, val icon: ImageVector)

// "Play" maps to SplashRoute because that is the actual root of the play stack.
// All Home/Game/Win entries are pushed onto backStacks[SplashRoute], so this tab
// must switch to SplashRoute to restore the in-progress play session.
private val TABS = listOf(
    NavTab(SplashRoute, "Play", SumiIcons.Book),
    NavTab(DailyRoute, "Daily", SumiIcons.Calendar),
    NavTab(StatsRoute, "Stats", SumiIcons.Chart),
    NavTab(PaywallRoute, "Zen", SumiIcons.Quote),
)

@Composable
fun BottomNavBar(
    currentTab: NavKey,
    onTabClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SumiTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = colors.paperEdge)
            .background(colors.paper)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = Sumi.Space.s4, vertical = Sumi.Space.s3),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        TABS.forEach { tab ->
            BottomNavItem(
                label = tab.label,
                icon = tab.icon,
                selected = currentTab == tab.route,
                onClick = { onTabClick(tab.route) },
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = SumiTheme.colors
    val color = if (selected) colors.red else colors.inkFaint
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    ) {
        SumiIcon(icon = icon, contentDescription = label, tint = color, size = 20.dp)
        Spacer(Modifier.height(Sumi.Space.s1))
        Text(
            text = label.uppercase(),
            style = SumiTheme.typography.uiMeta,
            color = color,
        )
    }
}
