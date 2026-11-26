package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.model.RecordedWeight
import inc.dimasic.taskcal.data.repository.FakeSettingsRepository
import inc.dimasic.taskcal.data.repository.WeightRepository
import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.UserProfile
import inc.dimasic.taskcal.domain.usecase.weight.WeightUseCaseImpl
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Weight against the goal: how far there is to go, and how it moved across a period. The change
 * is read from the period's first and last weigh-ins, so it needs at least two of them.
 */
class WeightUseCaseTest {

    private val today = LocalDate.now()
    private val week = weekRangeAt(0)

    private fun useCase(vararg samples: RecordedWeight) =
        WeightUseCaseImpl(FakeWeightRepository(samples.toList()), FakeSettingsRepository())

    @Test
    fun `the latest weigh-in is measured against the goal`() {
        val goal = UserProfile.Default.weightGoal
        val summary = runBlocking { useCase(weight(today.minusDays(1), goal + 4.4)).summary() }

        assertEquals(goal + 4.4, summary.currentKg!!, 0.0001)
        assertEquals(today.minusDays(1), summary.measuredOn)
        assertEquals(goal, summary.goalKg, 0.0)
        assertEquals(4.4, summary.toGoalKg!!, 0.0001)
    }

    @Test
    fun `being under the goal reads as a negative distance to it`() {
        val goal = UserProfile.Default.weightGoal
        val summary = runBlocking { useCase(weight(today, goal - 0.6)).summary() }

        assertEquals(-0.6, summary.toGoalKg!!, 0.0001)
    }

    @Test
    fun `nothing weighed in means no current weight and nothing to measure`() {
        val summary = runBlocking { useCase().summary(changeOver = week) }

        assertNull(summary.currentKg)
        assertNull(summary.measuredOn)
        assertNull(summary.toGoalKg)
        assertNull(summary.changeKg)
        assertEquals(UserProfile.Default.weightGoal, summary.goalKg, 0.0)
    }

    @Test
    fun `change over a period is its last weigh-in minus its first`() {
        val summary = runBlocking {
            useCase(
                weight(week.start, 73.2),
                weight(week.start.plusDays(2), 73.0),
                weight(week.end, 72.4),
                // Outside the period, so it takes no part in the change.
                weight(week.end.plusDays(3), 70.0),
            ).summary(changeOver = week)
        }

        assertEquals(-0.8, summary.changeKg!!, 0.0001)
    }

    @Test
    fun `a single weigh-in in the period gives no change, and no period asked means none either`() {
        val useCase = useCase(weight(week.start, 73.2))

        assertNull(runBlocking { useCase.summary(changeOver = week) }.changeKg)
        assertNull(runBlocking { useCase.summary() }.changeKg)
    }

    private fun weight(date: LocalDate, kilograms: Double) = RecordedWeight(date = date, kilograms = kilograms)
}

/** In-memory weigh-ins; the latest is simply the most recent by date. */
private class FakeWeightRepository(private val samples: List<RecordedWeight>) : WeightRepository {
    override suspend fun latestWeight(): RecordedWeight? = samples.maxByOrNull { it.date }
    override suspend fun weightHistory(range: DateRange): List<RecordedWeight> =
        samples.filter { it.date in range }.sortedBy { it.date }
}
