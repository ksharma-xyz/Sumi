package xyz.ksharma.sumi.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import xyz.ksharma.sumi.preferences.InMemorySumiPreferences
import xyz.ksharma.sumi.preferences.SumiPreferences
import xyz.ksharma.sumi.screens.splash.SplashViewModel

val appModule = module {
    single<SumiPreferences> { InMemorySumiPreferences() }
    viewModel { SplashViewModel(prefs = get()) }
}
