package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.local.healthconnect.HealthConnectManager
import inc.dimasic.taskcal.data.model.RecordedActivity
import inc.dimasic.taskcal.domain.model.DateRange
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Paging back through statistics touches one range per step; this is plenty and stays small. */
private const val MAX_CACHED_RANGES = 64

/** [ActivitiesRepository] backed by Health Connect, read through [HealthConnectManager]. */
@Singleton
class HealthConnectActivitiesRepository @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
) : ActivitiesRepository {

    /** Least-recently-read range is dropped once full. Guarded by `synchronized(cache)`. */
    private val cache = object : LinkedHashMap<DateRange, RecordedActivity>(MAX_CACHED_RANGES, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<DateRange, RecordedActivity>): Boolean =
            size > MAX_CACHED_RANGES
    }

    override suspend fun activityTotals(range: DateRange): RecordedActivity {
        synchronized(cache) { cache[range] }?.let { return it }

        val recorded = healthConnectManager.activityTotals(range)
        if (range.isCacheable() && recorded != RecordedActivity.Empty) {
            synchronized(cache) { cache[range] = recorded }
        }
        return recorded
    }

    /** Called when the connection changes: figures read under the old permissions are no longer the truth. */
    override fun invalidate() {
        synchronized(cache) { cache.clear() }
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
