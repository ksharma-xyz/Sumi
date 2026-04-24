package xyz.ksharma.sumi.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import xyz.ksharma.sumi.game.di.gameModule
import xyz.ksharma.sumi.preferences.DataStoreGameSaveRepository
import xyz.ksharma.sumi.preferences.DataStoreSumiPreferences
import xyz.ksharma.sumi.preferences.GameSaveRepository
import xyz.ksharma.sumi.preferences.SumiPreferences
import xyz.ksharma.sumi.screens.daily.DailyViewModel
import xyz.ksharma.sumi.screens.game.GameViewModel
import xyz.ksharma.sumi.screens.home.HomeViewModel
import xyz.ksharma.sumi.screens.splash.SplashViewModel
import xyz.ksharma.sumi.screens.stats.StatsViewModel
import xyz.ksharma.sumi.screens.win.WinViewModel

val appModule = module {
    includes(gameModule)
    single<SumiPreferences> { DataStoreSumiPreferences(store = get()) }
    single<GameSaveRepository> { DataStoreGameSaveRepository(store = get()) }
    viewModel { SplashViewModel(prefs = get()) }
    viewModel { GameViewModel(puzzleRepository = get(), saveRepository = get()) }
    viewModel { HomeViewModel(prefs = get()) }
    viewModel { WinViewModel(prefs = get()) }
    viewModel { DailyViewModel(prefs = get()) }
    viewModel { StatsViewModel(prefs = get()) }
}
