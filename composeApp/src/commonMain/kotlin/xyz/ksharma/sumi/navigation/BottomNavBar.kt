package xyz.ksharma.sumi.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
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

private val TABS = listOf(
    NavTab(HomeRoute, "Home", SumiIcons.Menu),
    NavTab(DailyRoute, "Daily", SumiIcons.Calendar),
    NavTab(StatsRoute, "Stats", SumiIcons.Chart),
    NavTab(PaywallRoute, "Pro", SumiIcons.Trophy),
)

@Composable
fun BottomNavBar(
    currentTab: NavKey,
    onTabClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Sumi.Color.paperEdge)
            .background(Sumi.Color.paper)
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
    val color = if (selected) Sumi.Color.red else Sumi.Color.inkFaint
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        ),
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
