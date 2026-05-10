package xyz.ksharma.sumi.preferences

import kotlinx.coroutines.flow.Flow

interface ProPreferences {
    fun observeIsPro(): Flow<Boolean>
    suspend fun setIsPro(value: Boolean)
}
