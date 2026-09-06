package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.model.DailyActivity
import inc.dimasic.taskcal.domain.model.DateRange

/**
 * Recorded activity, one entry per day. Choosing which days a period covers and summing them
 * against the user's goals are domain decisions and live in
 * [inc.dimasic.taskcal.domain.usecase.ActivitiesUseCase] instead.
 */
interface ActivitiesRepository {

    /** The days in [range] that have any recorded activity. */
    suspend fun dailyActivity(range: DateRange): List<DailyActivity>
}
