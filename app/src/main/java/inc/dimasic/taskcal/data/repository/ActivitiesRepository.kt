package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.model.RecordedActivity
import inc.dimasic.taskcal.domain.model.DateRange
import java.time.LocalDate

/**
 * Recorded activity, totalled over a date range. Choosing which days a period covers and measuring
 * the totals against the user's goals are domain decisions and live in
 * [inc.dimasic.taskcal.domain.usecase.activities.ActivitiesUseCase] instead.
 */
interface ActivitiesRepository {

    /** Everything recorded across [range]; zeroes when there is nothing to read. */
    suspend fun activityTotals(range: DateRange): RecordedActivity

    /**
     * Calories burned per calendar day across [range]. A day with nothing recorded is absent from
     * the map rather than present as zero.
     */
    suspend fun dailyCaloriesBurned(range: DateRange): Map<LocalDate, Int>

    /** Forgets anything held back from earlier reads, so the next one goes to the source. */
    fun invalidate() = Unit
}
