@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package xyz.ksharma.sumi.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import xyz.ksharma.sumi.platform.AppInfo
import xyz.ksharma.sumi.platform.IosAppInfo
import xyz.ksharma.sumi.preferences.DataStoreSumiPreferences
import xyz.ksharma.sumi.preferences.DebugPreferences
import xyz.ksharma.sumi.preferences.DebugProRepository
import xyz.ksharma.sumi.preferences.ProPreferences
import xyz.ksharma.sumi.preferences.ProRepository
import xyz.ksharma.sumi.preferences.StoreKitProRepository

val iosModule = module {
    single<DataStore<Preferences>> {
        val docDir = NSFileManager.defaultManager
            .URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, true, null)
            ?.path ?: error("Cannot resolve iOS document directory")
        PreferenceDataStoreFactory.createWithPath {
            "$docDir/sumi.preferences_pb".toPath()
        }
    }
    single<AppInfo> { IosAppInfo() }
    single<ProPreferences> { get<DataStoreSumiPreferences>() }
    single<ProRepository> {
        if (get<AppInfo>().isDebugBuild) DebugProRepository(debug = get<DebugPreferences>())
        else StoreKitProRepository(prefs = get())
    }
}
