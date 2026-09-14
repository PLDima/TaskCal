package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.healthconnect.HealthConnectManager
import inc.dimasic.taskcal.data.model.RecordedWeight
import inc.dimasic.taskcal.domain.model.DateRange
import javax.inject.Inject
import javax.inject.Singleton

/** [WeightRepository] backed by Health Connect, read through [HealthConnectManager]. */
@Singleton
class HealthConnectWeightRepository @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
) : WeightRepository {

    override suspend fun latestWeight(): RecordedWeight? = healthConnectManager.latestWeight()

    override suspend fun weightHistory(range: DateRange): List<RecordedWeight> =
        healthConnectManager.weightHistory(range)
}
