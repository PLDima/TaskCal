package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.model.RecordedActivity
import inc.dimasic.taskcal.domain.model.DateRange

/**
 * Recorded activity, totalled over a date range. Choosing which days a period covers and measuring
 * the totals against the user's goals are domain decisions and live in
 * [inc.dimasic.taskcal.domain.usecase.ActivitiesUseCase] instead.
 */
interface ActivitiesRepository {

    /** Everything recorded across [range]; zeroes when there is nothing to read. */
    suspend fun activityTotals(range: DateRange): RecordedActivity
}
