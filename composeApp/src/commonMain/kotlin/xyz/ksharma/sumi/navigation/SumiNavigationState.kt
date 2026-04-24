package xyz.ksharma.sumi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

@Composable
fun rememberSumiNavigationState(): SumiNavigationState {
    val topLevelRouteState = remember { mutableStateOf<NavKey>(SplashRoute) }
    val topLevelRoutes: Set<NavKey> = setOf(SplashRoute, HomeRoute, DailyRoute, StatsRoute, PaywallRoute)
    val backStacks = topLevelRoutes.associateWith { key ->
        rememberNavBackStack(sumiNavSerializationConfig, key)
    }
    return remember {
        SumiNavigationState(
            startRoute = SplashRoute,
            topLevelRouteState = topLevelRouteState,
            backStacks = backStacks,
        )
    }
}

class SumiNavigationState(
    val startRoute: NavKey,
    topLevelRouteState: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    var topLevelRoute: NavKey by topLevelRouteState

    // Bottom nav is hidden on these routes — full-screen flows where the tab bar adds no value.
    private val noNavBarRoutes = setOf(SplashRoute, OnboardingRoute, SettingsRoute, LicensesRoute)

    val showBottomNav: Boolean
        get() = backStacks[topLevelRoute]?.lastOrNull().let { it != null && it !in noNavBarRoutes }

    private val stacksInUse: List<NavKey>
        get() = listOf(topLevelRoute)

    fun goTo(route: NavKey) {
        if (route in backStacks.keys) {
            topLevelRoute = route
        } else {
            backStacks[topLevelRoute]?.add(route)
        }
        logStack("goTo(${route::class.simpleName})")
    }

    fun pop() {
        val currentStack = backStacks[topLevelRoute] ?: return
        if (currentStack.last() == topLevelRoute) {
            if (topLevelRoute != startRoute) topLevelRoute = startRoute
        } else {
            currentStack.removeLastOrNull()
        }
        logStack("pop()")
    }

    fun resetRoot(route: NavKey) {
        val stack = backStacks[topLevelRoute] ?: return
        stack.clear()
        stack.add(route)
        logStack("resetRoot(${route::class.simpleName})")
    }

    private fun logStack(action: String) {
        val stackStr = backStacks[topLevelRoute]
            ?.joinToString(" → ") { routeLabel(it) }
            ?: "empty"
        println("[Nav] $action | tab=${routeLabel(topLevelRoute)} | stack=[$stackStr]")
    }

    private fun routeLabel(key: NavKey): String = key::class.simpleName ?: key.toString()

    @Composable
    fun toEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): SnapshotStateList<NavEntry<NavKey>> {
        val decoratedEntries = backStacks.mapValues { (_, stack) ->
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                entryProvider = entryProvider,
            )
        }
        return stacksInUse
            .flatMap { decoratedEntries[it] ?: emptyList() }
            .toMutableStateList()
    }
}
