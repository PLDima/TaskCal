package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.healthconnect.HealthConnectManager
import inc.dimasic.taskcal.data.model.RecordedActivity
import inc.dimasic.taskcal.domain.model.DateRange
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/** [ActivitiesRepository] backed by Health Connect, read through [HealthConnectManager]. */
@Singleton
class HealthConnectActivitiesRepository @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
) : ActivitiesRepository {

    private val cache = ConcurrentHashMap<DateRange, RecordedActivity>()

    override suspend fun activityTotals(range: DateRange): RecordedActivity {
        cache[range]?.let { return it }

        val recorded = healthConnectManager.activityTotals(range)
        if (range.isCacheable() && recorded != RecordedActivity.Empty) {
            cache[range] = recorded
        }
        return recorded
    }

    /**
     * Only ranges that have finished are worth keeping: today's totals climb as the day goes on,
     * so caching them would serve stale figures. Empty results are never cached either — they are
     * also what a not-yet-granted permission reads as, and that changes the moment it's granted.
     */
    private fun DateRange.isCacheable(): Boolean = end.isBefore(LocalDate.now())

    // Not cached yet: a broader cache across all Health Connect statistics reads is planned for a
    // later version. This one reads through every time for now.
    override suspend fun dailyCaloriesBurned(range: DateRange): Map<LocalDate, Int> =
        healthConnectManager.dailyCaloriesBurned(range)
}
