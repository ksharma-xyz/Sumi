package xyz.ksharma.sumi.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import xyz.ksharma.sumi.platform.AndroidAppInfo
import xyz.ksharma.sumi.platform.AppInfo
import xyz.ksharma.sumi.preferences.DataStoreSumiPreferences
import xyz.ksharma.sumi.preferences.DebugPreferences
import xyz.ksharma.sumi.preferences.DebugProRepository
import xyz.ksharma.sumi.preferences.ProPreferences
import xyz.ksharma.sumi.preferences.ProRepository

val androidModule = module {
    single<DataStore<Preferences>> {
        val ctx = androidApplication()
        PreferenceDataStoreFactory.createWithPath {
            (ctx.filesDir.absolutePath + "/sumi.preferences_pb").toPath()
        }
    }
    single<AppInfo> { AndroidAppInfo(context = androidApplication()) }
    single<ProPreferences> { get<DataStoreSumiPreferences>() }
    // TODO: replace with PlayBillingProRepository once Play Billing is wired up.
    single<ProRepository> { DebugProRepository(debug = get<DebugPreferences>()) }
}
