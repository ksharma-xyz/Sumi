package xyz.ksharma.sumi.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val androidModule = module {
    single<DataStore<Preferences>> {
        val ctx = androidApplication()
        PreferenceDataStoreFactory.createWithPath {
            (ctx.filesDir.absolutePath + "/sumi.preferences_pb").toPath()
        }
    }
}
