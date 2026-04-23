package xyz.ksharma.sumi.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import xyz.ksharma.sumi.theme.SumiTheme
import xyz.ksharma.sumi.theme.SumiTokens as Sumi

private data class NavTab(val route: NavKey, val label: String)

private val TABS = listOf(
    NavTab(HomeRoute, "Home"),
    NavTab(DailyRoute, "Daily"),
    NavTab(StatsRoute, "Stats"),
    NavTab(PaywallRoute, "Pro"),
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
                selected = currentTab == tab.route,
                onClick = { onTabClick(tab.route) },
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
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
        Text(
            text = label.uppercase(),
            style = SumiTheme.typography.uiMeta,
            color = color,
        )
    }
}
