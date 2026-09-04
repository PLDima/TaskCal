package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.repository.ActivitiesRepository
import inc.dimasic.taskcal.data.repository.SettingsRepository
import inc.dimasic.taskcal.domain.model.ActivitiesPeriod
import inc.dimasic.taskcal.domain.usecase.model.ActivityTotals
import inc.dimasic.taskcal.domain.usecase.model.PeriodActivityTotals
import javax.inject.Inject
import kotlinx.coroutines.flow.first

// The calorie and step goals come from the user's profile; the distance goal has no home there
// yet, so it stays a domain default until the Profile screen can set it.
private const val DAILY_DISTANCE_GOAL_METERS = 5_000

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
        val days = activitiesRepository.dailyActivity(range)
        val profile = userProfile.first()
        val inRange = days.filter { range.contains(it.date) }

        return PeriodActivityTotals(
            dateRange = range,
            totals = ActivityTotals(
                caloriesBurned = inRange.sumOf { it.caloriesBurned },
                caloriesGoal = profile.nutritionGoal.calories * range.dayCount,
                steps = inRange.sumOf { it.steps },
                stepsGoal = profile.stepsGoal * range.dayCount,
                distanceMeters = inRange.sumOf { it.distanceMeters },
                distanceGoalMeters = DAILY_DISTANCE_GOAL_METERS * range.dayCount,
                activeMinutes = inRange.sumOf { it.activeMinutes },
                avgHeartRate = if (inRange.isEmpty()) 0 else inRange.sumOf { it.avgHeartRate } / inRange.size,
            ),
        )
    }
}
