package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.domain.model.ActivitiesPeriod
import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.NutritionPeriod
import inc.dimasic.taskcal.domain.model.TasksPeriod
import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Which dates a period and offset resolve to. Every statistics screen goes through these three
 * primitives, so they are asserted on their shape — where a range starts and ends, and how an
 * offset moves it — relative to today rather than against fixed dates.
 */
class PeriodRangesTest {

    private val today = LocalDate.now()

    @Test
    fun `week runs Monday to Sunday and covers today`() {
        val week = weekRangeAt(0)

        assertEquals(DayOfWeek.MONDAY, week.start.dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, week.end.dayOfWeek)
        assertEquals(7, week.dayCount)
        assertTrue(today in week)
    }

    @Test
    fun `week offset moves by whole weeks in either direction`() {
        val thisWeek = weekRangeAt(0)

        assertEquals(thisWeek.start.minusWeeks(1), weekRangeAt(-1).start)
        assertEquals(thisWeek.end.minusWeeks(1), weekRangeAt(-1).end)
        assertEquals(thisWeek.start.plusWeeks(2), weekRangeAt(2).start)
    }

    @Test
    fun `month runs first day to last and covers today`() {
        val month = monthRangeAt(0)

        assertEquals(1, month.start.dayOfMonth)
        assertEquals(month.start.lengthOfMonth(), month.end.dayOfMonth)
        assertEquals(month.start.lengthOfMonth(), month.dayCount)
        assertTrue(today in month)
    }

    @Test
    fun `month offset moves by calendar months, each keeping its own length`() {
        val thisMonth = monthRangeAt(0)
        val lastMonth = monthRangeAt(-1)

        assertEquals(thisMonth.start.minusMonths(1), lastMonth.start)
        // The end is the last day of *that* month, not this month's end shifted back.
        assertEquals(lastMonth.start.lengthOfMonth(), lastMonth.end.dayOfMonth)
        assertEquals(thisMonth.start.plusMonths(1), monthRangeAt(1).start)
    }

    @Test
    fun `day is a single date, offset in days`() {
        assertEquals(today, dayRangeAt(0).start)
        assertEquals(today, dayRangeAt(0).end)
        assertEquals(1, dayRangeAt(0).dayCount)
        assertEquals(today.minusDays(1), dayRangeAt(-1).start)
        assertEquals(today.plusDays(3), dayRangeAt(3).end)
    }

    @Test
    fun `each feature's period enum resolves through the shared primitives`() {
        assertEquals(dayRangeAt(0), ActivitiesPeriod.DAY.dateRangeAt(0))
        assertEquals(weekRangeAt(-1), ActivitiesPeriod.WEEK.dateRangeAt(-1))
        assertEquals(weekRangeAt(0), NutritionPeriod.WEEK.dateRangeAt(0))
        assertEquals(monthRangeAt(1), NutritionPeriod.MONTH.dateRangeAt(1))
        assertEquals(weekRangeAt(0), TasksPeriod.WEEK.dateRangeAt(0))
        assertEquals(monthRangeAt(-2), TasksPeriod.MONTH.dateRangeAt(-2))
    }

    @Test
    fun `all time has no bounds`() {
        assertNull(TasksPeriod.ALL_TIME.dateRangeAt(0))
        assertNull(TasksPeriod.ALL_TIME.dateRangeAt(-5))
    }

    @Test
    fun `week chart buckets are the seven days, Monday first, each its own column`() {
        val buckets = weekRangeAt(0).weekChartBuckets()

        assertEquals(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"), buckets.map { it.label })
        assertTrue(buckets.all { it.range.dayCount == 1 })
        assertEquals(weekRangeAt(0).dates(), buckets.map { it.range.start })
    }

    @Test
    fun `month chart buckets are five contiguous runs that together cover the whole month`() {
        val month = monthRangeAt(0)
        val buckets = month.monthChartBuckets()

        assertEquals(listOf("W1", "W2", "W3", "W4", "W5"), buckets.map { it.label })
        assertEquals(month.start, buckets.first().range.start)
        assertEquals(month.end, buckets.last().range.end)
        buckets.zipWithNext { earlier, later ->
            assertEquals(earlier.range.end.plusDays(1), later.range.start)
        }
        assertEquals(month.dayCount, buckets.sumOf { it.range.dayCount })
    }

    @Test
    fun `month chart buckets are equal-sized apart from the last, which absorbs the remainder`() {
        val thirtyOne = monthRangeAt(0).let { DateRange(it.start, it.start.plusDays(30)) }
        val sizes = thirtyOne.monthChartBuckets().map { it.range.dayCount }

        assertEquals(listOf(7, 7, 7, 7, 3), sizes)
    }
}
