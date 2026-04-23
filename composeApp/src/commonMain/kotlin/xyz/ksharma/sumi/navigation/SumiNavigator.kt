package xyz.ksharma.sumi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey

@Composable
fun rememberSumiNavigator(state: SumiNavigationState): SumiNavigator =
    remember(state) { SumiNavigator(state) }

class SumiNavigator(private val state: SumiNavigationState) {
    fun goTo(route: NavKey) = state.goTo(route)
    fun pop() = state.pop()
    fun resetRoot(route: NavKey) = state.resetRoot(route)
    fun switchTab(tab: NavKey) = state.goTo(tab)
}
