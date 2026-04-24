package xyz.ksharma.sumi.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import xyz.ksharma.sumi.BuildConfig

val androidModule = module {
    single<DataStore<Preferences>> {
        val ctx = androidApplication()
        PreferenceDataStoreFactory.createWithPath {
            (ctx.filesDir.absolutePath + "/sumi.preferences_pb").toPath()
        }
    }
    factory<Boolean>(qualifier = named("isDebug")) { BuildConfig.DEBUG }
}
