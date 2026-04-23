package xyz.ksharma.sumi.navigation.entries

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.koin.compose.viewmodel.koinViewModel
import xyz.ksharma.sumi.navigation.SplashRoute
import xyz.ksharma.sumi.navigation.SumiNavigator
import xyz.ksharma.sumi.screens.splash.SplashScreen
import xyz.ksharma.sumi.screens.splash.SplashViewModel

@Suppress("ComposableNaming")
@androidx.compose.runtime.Composable
fun EntryProviderScope<NavKey>.SplashEntry(navigator: SumiNavigator) {
    entry<SplashRoute> {
        val viewModel: SplashViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        SplashScreen(
            uiState = uiState,
            onNavigate = { navigator.resetRoot(it) },
        )
    }
}
