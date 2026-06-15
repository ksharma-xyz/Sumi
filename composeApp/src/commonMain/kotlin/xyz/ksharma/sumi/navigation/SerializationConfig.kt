package xyz.ksharma.sumi.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val sumiNavSerializationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(SplashRoute::class, SplashRoute.serializer())
            subclass(OnboardingRoute::class, OnboardingRoute.serializer())
            subclass(HomeRoute::class, HomeRoute.serializer())
            subclass(DailyRoute::class, DailyRoute.serializer())
            subclass(StatsRoute::class, StatsRoute.serializer())
            subclass(PaywallRoute::class, PaywallRoute.serializer())
            subclass(ZenRoute::class, ZenRoute.serializer())
            subclass(GameRoute::class, GameRoute.serializer())
            subclass(WinRoute::class, WinRoute.serializer())
            subclass(GameOverRoute::class, GameOverRoute.serializer())
            subclass(SettingsRoute::class, SettingsRoute.serializer())
            subclass(LicensesRoute::class, LicensesRoute.serializer())
        }
    }
}
