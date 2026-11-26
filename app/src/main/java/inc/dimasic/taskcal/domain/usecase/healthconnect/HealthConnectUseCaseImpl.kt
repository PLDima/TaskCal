package inc.dimasic.taskcal.domain.usecase.healthconnect

import inc.dimasic.taskcal.data.repository.ActivitiesRepository
import inc.dimasic.taskcal.data.repository.HealthConnectRepository
import inc.dimasic.taskcal.domain.model.HealthConnectStatus
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class HealthConnectUseCaseImpl @Inject constructor(
    private val healthConnectRepository: HealthConnectRepository,
    private val activitiesRepository: ActivitiesRepository,
) : HealthConnectUseCase {

    override val status: Flow<HealthConnectStatus> = healthConnectRepository.status

    override val permissions: Set<String> get() = healthConnectRepository.permissions

    /**
     * Re-checks the connection. If it changed — permissions granted or revoked, provider installed
     * — anything the activities source kept from earlier reads was read under the old conditions,
     * so it's dropped rather than served again.
     */
    override suspend fun refreshStatus() {
        val before = status.first()
        healthConnectRepository.refreshStatus()
        if (status.first() != before) activitiesRepository.invalidate()
    }
}
