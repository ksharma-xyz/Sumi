package xyz.ksharma.sumi.preferences

import kotlinx.coroutines.flow.Flow

interface ProRepository {
    fun isPro(): Flow<Boolean>
}
