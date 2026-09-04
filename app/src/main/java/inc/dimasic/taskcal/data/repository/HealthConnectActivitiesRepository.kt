package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.healthconnect.HealthConnectManager
import inc.dimasic.taskcal.data.model.DailyActivity
import inc.dimasic.taskcal.domain.model.DateRange
import javax.inject.Inject
import javax.inject.Singleton

/** [ActivitiesRepository] backed by Health Connect, read through [HealthConnectManager]. */
@Singleton
class HealthConnectActivitiesRepository @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
) : ActivitiesRepository {

    override suspend fun dailyActivity(range: DateRange): List<DailyActivity> =
        healthConnectManager.dailyActivity(range)
}
