package inc.dimasic.taskcal.repository

import inc.dimasic.taskcal.model.ActivitiesPeriod
import inc.dimasic.taskcal.model.DateRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

private const val DAILY_CALORIE_BURN_GOAL = 600
private const val DAILY_STEP_GOAL = 10_000
private const val DAILY_DISTANCE_GOAL_METERS = 5_000

/** Mocked per-day activity totals. */
private data class DailyActivity(
    val date: LocalDate,
    val caloriesBurned: Int,
    val steps: Int,
    val distanceMeters: Int,
    val activeMinutes: Int,
    val avgHeartRate: Int,
)

data class ActivityTotals(
    val caloriesBurned: Int,
    val caloriesGoal: Int,
    val steps: Int,
    val stepsGoal: Int,
    val distanceMeters: Int,
    val distanceGoalMeters: Int,
    /** Total active minutes over the period; no goal. */
    val activeMinutes: Int,
    /** Mean of the per-day averages over the period; no goal. */
    val avgHeartRate: Int,
) {
    companion object {
        val Empty = ActivityTotals(
            caloriesBurned = 0,
            caloriesGoal = 0,
            steps = 0,
            stepsGoal = 0,
            distanceMeters = 0,
            distanceGoalMeters = 0,
            activeMinutes = 0,
            avgHeartRate = 0,
        )
    }
}

data class PeriodActivityTotals(
    val dateRange: DateRange,
    val totals: ActivityTotals,
) {
    companion object {
        val Empty = PeriodActivityTotals(
            dateRange = DateRange(LocalDate.now(), LocalDate.now()),
            totals = ActivityTotals.Empty,
        )
    }
}

@Singleton
class ActivitiesRepository @Inject constructor() {

    private val days = MutableStateFlow(mockDays())

    fun totalsForPeriod(
        period: Flow<ActivitiesPeriod>,
        offset: Flow<Int>,
    ): Flow<PeriodActivityTotals> =
        combine(days, period, offset) { allDays, selectedPeriod, selectedOffset ->
            val range = dateRangeFor(selectedPeriod, selectedOffset)
            val inRange = allDays.filter { range.contains(it.date) }
            PeriodActivityTotals(
                dateRange = range,
                totals = ActivityTotals(
                    caloriesBurned = inRange.sumOf { it.caloriesBurned },
                    caloriesGoal = DAILY_CALORIE_BURN_GOAL * range.dayCount,
                    steps = inRange.sumOf { it.steps },
                    stepsGoal = DAILY_STEP_GOAL * range.dayCount,
                    distanceMeters = inRange.sumOf { it.distanceMeters },
                    distanceGoalMeters = DAILY_DISTANCE_GOAL_METERS * range.dayCount,
                    activeMinutes = inRange.sumOf { it.activeMinutes },
                    avgHeartRate = if (inRange.isEmpty()) 0 else inRange.sumOf { it.avgHeartRate } / inRange.size,
                ),
            )
        }

    private fun dateRangeFor(period: ActivitiesPeriod, offset: Int): DateRange {
        val today = LocalDate.now()
        return when (period) {
            ActivitiesPeriod.DAY -> {
                val day = today.plusDays(offset.toLong())
                DateRange(start = day, end = day)
            }
            ActivitiesPeriod.WEEK -> {
                val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .plusWeeks(offset.toLong())
                DateRange(start = monday, end = monday.plusDays(6))
            }
        }
    }

    private fun mockDays(): List<DailyActivity> {
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0..6).mapNotNull { dayIndex ->
            val date = monday.plusDays(dayIndex.toLong())
            if (date.isAfter(today)) return@mapNotNull null
            val steps = 6_500 + (dayIndex * 1_700) % 6_000
            DailyActivity(
                date = date,
                caloriesBurned = 420 + (dayIndex * 130) % 420,
                steps = steps,
                distanceMeters = (steps * 0.75).toInt(),
                activeMinutes = 18 + (dayIndex * 7) % 30,
                avgHeartRate = 92 + (dayIndex * 4) % 28,
            )
        }
    }
}
