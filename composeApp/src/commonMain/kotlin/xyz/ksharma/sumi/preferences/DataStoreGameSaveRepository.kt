package xyz.ksharma.sumi.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import xyz.ksharma.sumi.game.model.Difficulty
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class DataStoreGameSaveRepository(private val store: DataStore<Preferences>) : GameSaveRepository {

    override suspend fun loadSave(difficulty: Difficulty): GameSave? {
        val prefs = store.data.first()
        val slot = difficulty.slot
        val day = prefs[slot.dayKey] ?: return null
        if (day != todayEpochDay()) return null // stale — daily puzzle has rolled over
        return GameSave(
            epochDay = day,
            cells = prefs[slot.cellsKey] ?: return null,
            elapsedMs = prefs[slot.elapsedKey] ?: 0L,
            mistakeCount = prefs[slot.mistakesKey] ?: 0,
            hintsRemaining = prefs[slot.hintsKey] ?: 3,
        )
    }

    override suspend fun writeSave(difficulty: Difficulty, save: GameSave) {
        val slot = difficulty.slot
        store.edit { prefs ->
            prefs[slot.dayKey] = save.epochDay
            prefs[slot.cellsKey] = save.cells
            prefs[slot.elapsedKey] = save.elapsedMs
            prefs[slot.mistakesKey] = save.mistakeCount
            prefs[slot.hintsKey] = save.hintsRemaining
        }
    }

    override suspend fun clearSave(difficulty: Difficulty) {
        val slot = difficulty.slot
        store.edit { prefs ->
            prefs.remove(slot.dayKey)
            prefs.remove(slot.cellsKey)
            prefs.remove(slot.elapsedKey)
            prefs.remove(slot.mistakesKey)
            prefs.remove(slot.hintsKey)
        }
    }

    override suspend fun clearAllSaves() {
        Difficulty.entries.forEach { clearSave(it) }
    }
}

private val Difficulty.slot: SaveSlot
    get() = SaveSlot(name.lowercase())

private class SaveSlot(private val diff: String) {
    val dayKey = longPreferencesKey("save_${diff}_day")
    val cellsKey = stringPreferencesKey("save_${diff}_cells")
    val elapsedKey = longPreferencesKey("save_${diff}_elapsed")
    val mistakesKey = intPreferencesKey("save_${diff}_mistakes")
    val hintsKey = intPreferencesKey("save_${diff}_hints")
}

@OptIn(ExperimentalTime::class)
private fun todayEpochDay(): Long {
    val now = Clock.System.now()
    val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
    return local.date.toEpochDays()
}
