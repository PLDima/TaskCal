package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.database.FakeFoodEntryDao
import inc.dimasic.taskcal.data.local.database.FoodEntryEntity
import inc.dimasic.taskcal.data.model.RecordedActivity
import inc.dimasic.taskcal.data.repository.ActivitiesRepository
import inc.dimasic.taskcal.data.repository.FoodRepository
import inc.dimasic.taskcal.data.repository.FakeSettingsRepository
import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.MacroType
import inc.dimasic.taskcal.domain.model.MealType
import inc.dimasic.taskcal.domain.model.NutritionPeriod
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `CaloriesUseCase`'s consumed-side figures were already covered indirectly by real Room data
 * before this test existed; the domain rules worth locking in now are the ones this task actually
 * introduced: averages only count days at least one item was logged for, while every day/bucket
 * still keeps its chart slot (a day with nothing logged renders as an empty bar, never a dropped
 * one); the period total comes from [ActivitiesRepository.activityTotals], not summed from the
 * per-day breakdown; best/worst-day only compares days Health Connect actually reported; and the
 * macro chart never touches Health Connect at all.
 *
 * Fixtures are anchored to the current week's Monday rather than fixed dates, per this project's
 * convention of asserting on relationships rather than dates that would drift with the calendar.
 */
class CaloriesUseCaseTest {

    private val monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private val tuesday = monday.plusDays(1)

    private fun useCase(
        entries: List<FoodEntryEntity> = emptyList(),
        activitiesRepository: ActivitiesRepository = FakeCaloriesActivitiesRepository(),
    ): CaloriesUseCase {
        val foodUseCase = FoodUseCase(FoodRepository(FakeFoodEntryDao(entries)))
        return CaloriesUseCase(foodUseCase, activitiesRepository, FakeSettingsRepository())
    }

    @Test
    fun `average consumed only counts days something was logged`() {
        // Only Monday has anything logged; the other six days of the week are untracked, not zero.
        val entries = listOf(entry(monday, calories = 490))
        val summary = runBlocking { useCase(entries).periodSummary(NutritionPeriod.WEEK, 0) }

        // Diluting this over all 7 days would read 70 — asserting 490 is what proves the exclusion.
        assertEquals(490, summary.averages.avgConsumed)
    }

    @Test
    fun `average burned only counts the same tracked days, from the daily breakdown`() {
        val entries = listOf(entry(monday, calories = 500), entry(tuesday, calories = 700))
        // Wednesday has a reported burned figure despite nothing being logged there, and must not
        // pull the average down — only Monday and Tuesday count.
        val repository = FakeCaloriesActivitiesRepository(
            dailyBurned = mapOf(monday to 600, tuesday to 900, monday.plusDays(2) to 500),
        )
        val summary = runBlocking { useCase(entries, repository).periodSummary(NutritionPeriod.WEEK, 0) }

        assertEquals((600 + 900) / 2, summary.averages.avgBurned)
    }

    @Test
    fun `summary total burned comes from the whole-range total, not the daily breakdown`() {
        // The daily breakdown alone sums to 1_500; the fake's whole-range total is deliberately a
        // different number, so a passing assertion here proves which one summary.totalBurned uses.
        val repository = FakeCaloriesActivitiesRepository(
            wholeRangeTotal = 1_550,
            dailyBurned = mapOf(monday to 600, tuesday to 900),
        )
        val summary = runBlocking { useCase(activitiesRepository = repository).periodSummary(NutritionPeriod.WEEK, 0) }

        assertEquals(1_550, summary.summary.totalBurned)
    }

    @Test
    fun `best and worst day only compare days Health Connect actually reported`() {
        val entries = listOf(
            entry(monday, calories = 500),
            entry(tuesday, calories = 700),
        )
        // Only Monday and Tuesday have a reported burned figure; every other day of the week is
        // absent from the map, not zero.
        val repository = FakeCaloriesActivitiesRepository(dailyBurned = mapOf(monday to 600, tuesday to 900))
        val summary = runBlocking { useCase(entries, repository).periodSummary(NutritionPeriod.WEEK, 0) }

        // Monday: 500 - 600 = -100. Tuesday: 700 - 900 = -200.
        // Without the exclusion, every untracked day would read as a 0 balance and outrank Monday
        // as "worst" — asserting Monday here is what proves the exclusion actually happened.
        assertEquals(tuesday, summary.summary.bestDay)
        assertEquals(-200, summary.summary.bestDayBalance)
        assertEquals(monday, summary.summary.worstDay)
        assertEquals(-100, summary.summary.worstDayBalance)
    }

    @Test
    fun `calories chart keeps every weekday's slot, zero for days nothing was logged`() {
        val entries = listOf(entry(monday, calories = 500), entry(tuesday, calories = 700))
        val repository = FakeCaloriesActivitiesRepository(dailyBurned = mapOf(monday to 600, tuesday to 900))
        val chart = runBlocking { useCase(entries, repository).periodSummary(NutritionPeriod.WEEK, 0) }.caloriesChart

        // All 7 weekday bars stay in the chart so its positions don't shift; a (0, 0) point renders
        // as an empty bar (the chart skips drawing a zero-height one) rather than being dropped.
        assertEquals(7, chart.size)
        assertEquals(listOf(500, 600), chart[0].values)
        assertEquals(listOf(700, 900), chart[1].values)
        assertEquals(listOf(0, 0), chart[2].values)
    }

    @Test
    fun `macro chart keeps every weekday's slot, and never reads from Health Connect`() {
        val entries = listOf(
            entry(monday, calories = 100, protein = 20.0),
            entry(tuesday, calories = 100, protein = 10.0),
        )
        val foodUseCase = FoodUseCase(FoodRepository(FakeFoodEntryDao(entries)))
        val useCase = CaloriesUseCase(foodUseCase, ThrowingActivitiesRepository(), FakeSettingsRepository())

        val chart = runBlocking { useCase.macroChartForPeriod(MacroType.PROTEIN, NutritionPeriod.WEEK, 0) }

        assertEquals(7, chart.size)
        assertEquals(listOf(20), chart[0].values)
        assertEquals(listOf(10), chart[1].values)
        assertEquals(listOf(0), chart[2].values)
    }

    @Test
    fun `month buckets keep all 5 slots, each averaged over its own tracked days only`() {
        val firstOfMonth = LocalDate.now().withDayOfMonth(1)
        // Only the very first day of the month has anything logged; every other day, in every
        // bucket, is untracked.
        val entries = listOf(entry(firstOfMonth, calories = 600))
        val chart = runBlocking { useCase(entries).periodSummary(NutritionPeriod.MONTH, 0) }.caloriesChart

        // All 5 buckets stay in the chart. The first bucket's one tracked day sets its average —
        // undiluted by the rest of that bucket's untracked days — and the other 4 buckets are zero.
        assertEquals(5, chart.size)
        assertEquals(600, chart[0].values[0])
        assertEquals(listOf(0, 0), chart[1].values)
    }

    @Test
    fun `a period with nothing recorded reports zeroes and no best or worst day`() {
        val summary = runBlocking { useCase().periodSummary(NutritionPeriod.WEEK, 0) }

        assertEquals(0, summary.averages.avgConsumed)
        assertEquals(0, summary.averages.avgBurned)
        assertEquals(0, summary.summary.totalConsumed)
        assertEquals(0, summary.summary.totalBurned)
        assertNull(summary.summary.bestDay)
        assertNull(summary.summary.worstDay)
        // All 7 weekday slots are still there, just every one of them zero.
        assertEquals(7, summary.caloriesChart.size)
        assertTrue(summary.caloriesChart.all { it.values == listOf(0, 0) })
    }

    private fun entry(
        date: LocalDate,
        calories: Int,
        protein: Double = 0.0,
        carbs: Double = 0.0,
        fat: Double = 0.0,
    ) = FoodEntryEntity(
        id = "$date-$calories",
        date = date.toString(),
        mealType = MealType.BREAKFAST.name,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        name = "",
    )
}

/** Stands in for the Health Connect-backed repository with configurable, distinguishable data. */
private class FakeCaloriesActivitiesRepository(
    private val wholeRangeTotal: Int = 0,
    private val dailyBurned: Map<LocalDate, Int> = emptyMap(),
) : ActivitiesRepository {

    override suspend fun activityTotals(range: DateRange): RecordedActivity =
        RecordedActivity.Empty.copy(caloriesBurned = wholeRangeTotal)

    override suspend fun dailyCaloriesBurned(range: DateRange): Map<LocalDate, Int> =
        dailyBurned.filterKeys { range.contains(it) }
}

/** Fails the test if the macro chart path ever calls through to Health Connect. */
private class ThrowingActivitiesRepository : ActivitiesRepository {
    override suspend fun activityTotals(range: DateRange): RecordedActivity =
        error("macroChartForPeriod must not read Health Connect")

    override suspend fun dailyCaloriesBurned(range: DateRange): Map<LocalDate, Int> =
        error("macroChartForPeriod must not read Health Connect")
}
