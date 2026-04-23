package xyz.ksharma.sumi

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.context.startKoin
import xyz.ksharma.sumi.di.appModule
import xyz.ksharma.sumi.di.iosModule
import xyz.ksharma.sumi.game.di.gameModule

fun MainViewController() = ComposeUIViewController {
    App()
}

fun initKoinIos() {
    startKoin {
        modules(appModule, gameModule, iosModule)
    }
}
