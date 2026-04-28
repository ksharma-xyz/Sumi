package xyz.ksharma.sumi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
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
    val topLevelRoutes: Set<NavKey> = setOf(SplashRoute, HomeRoute, DailyRoute, StatsRoute, ZenRoute)
    val routeByName = remember { topLevelRoutes.associateBy { it::class.simpleName!! } }

    // Persist selected tab across configuration changes using the route class name as key
    val topLevelRouteState = rememberSaveable(
        saver = Saver(
            save = { it.value::class.simpleName!! },
            restore = { name -> mutableStateOf<NavKey>(routeByName[name] ?: SplashRoute) },
        ),
    ) { mutableStateOf<NavKey>(SplashRoute) }

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

    private val _designerOpen = mutableStateOf(false)

    val showBottomNav: Boolean
        get() = !_designerOpen.value && backStacks[topLevelRoute]?.lastOrNull()
            .let { it != null && it !in noNavBarRoutes }

    fun setDesignerOpen(open: Boolean) {
        _designerOpen.value = open
    }

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
        when {
            currentStack.last() == topLevelRoute -> {
                if (topLevelRoute != startRoute) topLevelRoute = startRoute
            }
            currentStack.size > 1 -> currentStack.removeLastOrNull()
            // Single non-root item — can't pop without emptying; do nothing.
        }
        logStack("pop()")
    }

    fun resetRoot(route: NavKey) {
        val stack = backStacks[topLevelRoute] ?: return
        // Add the new root first so the stack is never empty (NavDisplay requires non-empty entries).
        stack.add(route)
        while (stack.size > 1) stack.removeAt(0)
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
