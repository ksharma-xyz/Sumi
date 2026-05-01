package xyz.ksharma.sumi.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import xyz.ksharma.sumi.ads.AdOrchestrator
import xyz.ksharma.sumi.ads.AdUnits
import xyz.ksharma.sumi.analytics.FirebaseSumiAnalytics
import xyz.ksharma.sumi.analytics.SumiAnalytics
import xyz.ksharma.sumi.game.di.gameModule
import xyz.ksharma.sumi.platform.AppInfo
import xyz.ksharma.sumi.preferences.DataStoreGameSaveRepository
import xyz.ksharma.sumi.preferences.DataStoreSumiPreferences
import xyz.ksharma.sumi.preferences.DebugPreferences
import xyz.ksharma.sumi.preferences.DebugProRepository
import xyz.ksharma.sumi.preferences.GameSaveRepository
import xyz.ksharma.sumi.preferences.ProRepository
import xyz.ksharma.sumi.preferences.SumiPreferences
import xyz.ksharma.sumi.preferences.ThemePreferences
import xyz.ksharma.sumi.screens.daily.DailyViewModel
import xyz.ksharma.sumi.screens.game.GameViewModel
import xyz.ksharma.sumi.screens.home.HomeViewModel
import xyz.ksharma.sumi.screens.settings.SettingsViewModel
import xyz.ksharma.sumi.screens.splash.SplashViewModel
import xyz.ksharma.sumi.screens.stats.StatsViewModel
import xyz.ksharma.sumi.screens.win.WinViewModel
import xyz.ksharma.sumi.screens.zen.ZenViewModel

val appModule = module {
    includes(gameModule)
    single<SumiAnalytics> { FirebaseSumiAnalytics() }
    single { DataStoreSumiPreferences(store = get()) }
    single<SumiPreferences> { get<DataStoreSumiPreferences>() }
    single<DebugPreferences> { get<DataStoreSumiPreferences>() }
    single<ThemePreferences> { get<DataStoreSumiPreferences>() }
    single<GameSaveRepository> { DataStoreGameSaveRepository(store = get()) }
    single<ProRepository> { DebugProRepository(debug = get()) }
    single { AdOrchestrator() }
    single { AdUnits(appInfo = get()) }
    viewModel { SplashViewModel(prefs = get()) }
    viewModel { GameViewModel(puzzleRepository = get(), saveRepository = get(), adOrchestrator = get()) }
    viewModel { HomeViewModel(prefs = get()) }
    viewModel { WinViewModel(prefs = get(), adOrchestrator = get()) }
    viewModel { DailyViewModel(prefs = get(), saveRepository = get()) }
    viewModel { StatsViewModel(prefs = get(), proRepo = get()) }
    viewModel { ZenViewModel(puzzleRepository = get(), exporter = get()) }
    viewModel {
        SettingsViewModel(
            debug = get(),
            gameSaves = get(),
            themes = get(),
            isDebug = get<AppInfo>().isDebugBuild,
        )
    }
}
