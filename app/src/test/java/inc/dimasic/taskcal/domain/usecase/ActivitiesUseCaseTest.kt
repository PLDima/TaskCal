package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.model.RecordedActivity
import inc.dimasic.taskcal.data.repository.ActivitiesRepository
import inc.dimasic.taskcal.data.repository.SettingsRepository
import inc.dimasic.taskcal.domain.model.ActivitiesPeriod
import inc.dimasic.taskcal.domain.model.DateRange
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The domain rules left in this use case are which dates a period covers and how the recorded
 * figures measure against the user's goals — Health Connect does the totalling itself, so the use
 * case no longer sums anything.
 *
 * [FakeActivitiesRepository] stands in for the Health Connect-backed repository, reporting a fixed
 * total for ranges that have started and nothing for ranges in the future.
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
    fun `recorded figures are reported as they came back`() {
        val result = totals(ActivitiesPeriod.DAY, offset = 0).totals

        assertEquals(FakeActivitiesRepository.RECORDED.caloriesBurned, result.caloriesBurned)
        assertEquals(FakeActivitiesRepository.RECORDED.steps, result.steps)
        assertEquals(FakeActivitiesRepository.RECORDED.distanceMeters, result.distanceMeters)
        assertEquals(FakeActivitiesRepository.RECORDED.activeMinutes, result.activeMinutes)
        assertEquals(FakeActivitiesRepository.RECORDED.avgHeartRate, result.avgHeartRate)
    }

    @Test
    fun `a period with nothing recorded reports zeroes, and no heart rate at all`() {
        // The fake has nothing for ranges that haven't started, so next week is empty.
        val result = totals(ActivitiesPeriod.WEEK, offset = 1).totals

        assertEquals(0, result.caloriesBurned)
        assertEquals(0, result.steps)
        assertEquals(0, result.distanceMeters)
        assertEquals(0, result.activeMinutes)
        // Counts can honestly be zero; an average with no samples is absent, not zero.
        assertNull(result.avgHeartRate)
    }
}

/** Stands in for the Health Connect-backed repository with one fixed total. */
private class FakeActivitiesRepository : ActivitiesRepository {

    override suspend fun activityTotals(range: DateRange): RecordedActivity =
        if (range.start.isAfter(LocalDate.now())) RecordedActivity.Empty else RECORDED

    companion object {
        /** Deliberately all different, so a swapped field shows up as a failure. */
        val RECORDED = RecordedActivity(
            caloriesBurned = 2_412,
            steps = 8_310,
            distanceMeters = 6_240,
            activeMinutes = 47,
            avgHeartRate = 96,
        )
    }
}
