package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.repository.ActivitiesRepository
import inc.dimasic.taskcal.data.repository.SettingsRepository
import inc.dimasic.taskcal.domain.model.ActivitiesPeriod
import inc.dimasic.taskcal.domain.usecase.model.ActivityTotals
import inc.dimasic.taskcal.domain.usecase.model.PeriodActivityTotals
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

// The step goal is part of the user's profile; these two have no home there yet, so they stay
// as domain defaults until the Profile screen can set them.
private const val DAILY_CALORIE_BURN_GOAL = 600
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

    fun totalsForPeriod(
        period: Flow<ActivitiesPeriod>,
        offset: Flow<Int>,
    ): Flow<PeriodActivityTotals> =
        combine(
            activitiesRepository.dailyActivity,
            userProfile,
            period,
            offset,
        ) { days, profile, selectedPeriod, selectedOffset ->
            val range = selectedPeriod.dateRangeAt(selectedOffset)
            val inRange = days.filter { range.contains(it.date) }
            PeriodActivityTotals(
                dateRange = range,
                totals = ActivityTotals(
                    caloriesBurned = inRange.sumOf { it.caloriesBurned },
                    caloriesGoal = DAILY_CALORIE_BURN_GOAL * range.dayCount,
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
