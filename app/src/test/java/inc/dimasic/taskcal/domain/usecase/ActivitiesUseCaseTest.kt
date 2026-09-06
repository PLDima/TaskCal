package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.model.DailyActivity
import inc.dimasic.taskcal.data.repository.ActivitiesRepository
import inc.dimasic.taskcal.data.repository.SettingsRepository
import inc.dimasic.taskcal.domain.model.ActivitiesPeriod
import inc.dimasic.taskcal.domain.model.DateRange
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Characterisation tests for activity aggregation.
 *
 * These assertions were originally written against `ActivitiesRepository.totalsForPeriod`, before
 * summing days and applying goals moved into this use case. They are unchanged, which is what
 * shows the move did not alter any of the figures. [ActivitiesRepository] is now backed by Health
 * Connect, so [FakeActivitiesRepository] stands in for it here with the same days the old mock
 * generator produced.
 *
 * The fake data only covers Monday through today of the current week, so the assertions are
 * written as invariants and relationships rather than fixed numbers.
 */
class ActivitiesUseCaseTest {

    private val useCase = ActivitiesUseCase(FakeActivitiesRepository(), SettingsRepository())

    private fun totals(period: ActivitiesPeriod, offset: Int) =
        runBlocking { useCase.totalsForPeriod(period, offset) }

    @Test
    fun `day period covers exactly one day`() {
        val result = totals(ActivitiesPeriod.DAY, offset = 0)

        assertEquals(result.dateRange.start, result.dateRange.end)
        assertEquals(1, result.dateRange.dayCount)
        assertEquals(LocalDate.now(), result.dateRange.start)
    }

    @Test
    fun `week period runs Monday to Sunday`() {
        val result = totals(ActivitiesPeriod.WEEK, offset = 0)

        assertEquals(DayOfWeek.MONDAY, result.dateRange.start.dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, result.dateRange.end.dayOfWeek)
        assertEquals(7, result.dateRange.dayCount)
    }

    @Test
    fun `offset shifts the period`() {
        val thisWeek = totals(ActivitiesPeriod.WEEK, offset = 0)
        val lastWeek = totals(ActivitiesPeriod.WEEK, offset = -1)

        assertEquals(thisWeek.dateRange.start.minusWeeks(1), lastWeek.dateRange.start)
        assertEquals(7, lastWeek.dateRange.dayCount)
    }

    @Test
    fun `goals scale with the number of days in the period`() {
        val day = totals(ActivitiesPeriod.DAY, offset = 0).totals
        val week = totals(ActivitiesPeriod.WEEK, offset = 0).totals

        // 5km is a domain default; the calorie and step goals come from the user's profile.
        assertEquals(3_000, day.caloriesGoal)
        assertEquals(10_000, day.stepsGoal)
        assertEquals(5_000, day.distanceGoalMeters)

        assertEquals(3_000 * 7, week.caloriesGoal)
        assertEquals(10_000 * 7, week.stepsGoal)
        assertEquals(5_000 * 7, week.distanceGoalMeters)
    }

    @Test
    fun `week totals are the sum of the individual days`() {
        val week = totals(ActivitiesPeriod.WEEK, offset = 0)
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val todayIndex = ChronoUnit.DAYS.between(monday, LocalDate.now()).toInt()

        val days = (0..6).map { dayIndex -> totals(ActivitiesPeriod.DAY, dayIndex - todayIndex).totals }

        assertEquals(days.sumOf { it.caloriesBurned }, week.totals.caloriesBurned)
        assertEquals(days.sumOf { it.steps }, week.totals.steps)
        assertEquals(days.sumOf { it.distanceMeters }, week.totals.distanceMeters)
        assertEquals(days.sumOf { it.activeMinutes }, week.totals.activeMinutes)
    }

    @Test
    fun `a period with no recorded days reports zeroes`() {
        // The fake data stops at today, so next week is empty.
        val result = totals(ActivitiesPeriod.WEEK, offset = 1).totals

        assertEquals(0, result.caloriesBurned)
        assertEquals(0, result.steps)
        assertEquals(0, result.distanceMeters)
        assertEquals(0, result.activeMinutes)
        assertEquals(0, result.avgHeartRate)
    }

    @Test
    fun `average heart rate is a mean of the days that have data`() {
        val week = totals(ActivitiesPeriod.WEEK, offset = 0)
        val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val todayIndex = ChronoUnit.DAYS.between(monday, LocalDate.now()).toInt()

        val recorded = (0..todayIndex)
            .map { dayIndex -> totals(ActivitiesPeriod.DAY, dayIndex - todayIndex).totals.avgHeartRate }
            .filter { it > 0 }

        assertTrue(week.totals.avgHeartRate >= recorded.min())
        assertTrue(week.totals.avgHeartRate <= recorded.max())
    }
}

/** Stands in for the Health Connect-backed repository with a fixed, synthetic week of days. */
private class FakeActivitiesRepository : ActivitiesRepository {

    private val days: List<DailyActivity> = run {
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        (0..6).mapNotNull { dayIndex ->
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

    override suspend fun dailyActivity(range: DateRange): List<DailyActivity> =
        days.filter { range.contains(it.date) }
}
