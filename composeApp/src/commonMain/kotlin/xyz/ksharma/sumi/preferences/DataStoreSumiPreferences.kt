package xyz.ksharma.sumi.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

private val KEY_SEEN_ONBOARDING = booleanPreferencesKey("seen_onboarding")

class DataStoreSumiPreferences(private val store: DataStore<Preferences>) : SumiPreferences {
    override suspend fun hasSeenOnboarding(): Boolean =
        store.data.first()[KEY_SEEN_ONBOARDING] ?: false

    override suspend fun setSeenOnboarding() {
        store.edit { it[KEY_SEEN_ONBOARDING] = true }
    }
}
