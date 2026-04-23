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

val iosModule = module {
    single<DataStore<Preferences>> {
        val docDir = NSFileManager.defaultManager
            .URLForDirectory(NSDocumentDirectory, NSUserDomainMask, null, true, null)
            ?.path ?: error("Cannot resolve iOS document directory")
        PreferenceDataStoreFactory.createWithPath {
            "$docDir/sumi.preferences_pb".toPath()
        }
    }
}
