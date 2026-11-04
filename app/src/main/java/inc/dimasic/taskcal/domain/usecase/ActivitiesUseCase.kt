package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.repository.ActivitiesRepository
import inc.dimasic.taskcal.data.repository.SettingsRepository
import inc.dimasic.taskcal.domain.model.ActivitiesPeriod
import inc.dimasic.taskcal.domain.usecase.model.ActivityTotals
import inc.dimasic.taskcal.domain.usecase.model.PeriodActivityTotals
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Sums recorded activity over a period and measures it against the user's goals. A goal covers
 * one day, so it scales with the number of days the period spans.
 */
class ActivitiesUseCase @Inject constructor(
    private val activitiesRepository: ActivitiesRepository,
    settingsRepository: SettingsRepository,
) {

    private val userProfile = settingsRepository.userProfile

    /**
     * One-shot: the caller (a [inc.dimasic.taskcal.common.CommonViewModel]) owns when to reload,
     * so this doesn't need to react to a period/offset flow itself.
     */
    suspend fun totalsForPeriod(period: ActivitiesPeriod, offset: Int): PeriodActivityTotals {
        val range = period.dateRangeAt(offset)
        val recorded = activitiesRepository.activityTotals(range)
        val profile = userProfile.first()

        return PeriodActivityTotals(
            dateRange = range,
            totals = ActivityTotals(
                caloriesBurned = recorded.caloriesBurned,
                caloriesGoal = profile.caloriesBurnedGoal * range.dayCount,
                steps = recorded.steps,
                stepsGoal = profile.stepsGoal * range.dayCount,
                distanceMeters = recorded.distanceMeters,
                distanceGoalMeters = profile.distanceGoalMeters * range.dayCount,
                activeMinutes = recorded.activeMinutes,
                avgHeartRate = recorded.avgHeartRate,
            ),
        )
    }
}
