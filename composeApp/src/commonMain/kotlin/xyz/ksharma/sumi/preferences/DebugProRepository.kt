package xyz.ksharma.sumi.preferences

import kotlinx.coroutines.flow.Flow

class DebugProRepository(private val debug: DebugPreferences) : ProRepository {
    override fun isPro(): Flow<Boolean> = debug.observeSimulatePro()
}
