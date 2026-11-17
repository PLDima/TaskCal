package inc.dimasic.taskcal.domain.usecase.healthconnect

import inc.dimasic.taskcal.domain.model.HealthConnectStatus
import kotlinx.coroutines.flow.Flow

interface HealthConnectUseCase {
    val status: Flow<HealthConnectStatus>

    val permissions: Set<String>

    suspend fun refreshStatus()
}
