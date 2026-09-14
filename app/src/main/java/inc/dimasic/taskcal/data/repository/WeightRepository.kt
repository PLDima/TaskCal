package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.model.RecordedWeight
import inc.dimasic.taskcal.domain.model.DateRange

/**
 * Recorded weigh-ins. Measuring them against the user's goal, and reading a trend out of them,
 * are domain decisions and live in [inc.dimasic.taskcal.domain.usecase.WeightUseCase] instead.
 */
interface WeightRepository {

    /** The most recent weigh-in on record, or null when there has never been one. */
    suspend fun latestWeight(): RecordedWeight?

    /** Every weigh-in across [range], oldest first. */
    suspend fun weightHistory(range: DateRange): List<RecordedWeight>
}
