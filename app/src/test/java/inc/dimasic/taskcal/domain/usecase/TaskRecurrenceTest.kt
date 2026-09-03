package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.model.Task
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// 2026-01-05 is a Monday, which makes the weekday expectations below easy to read.
private val MONDAY = LocalDate.of(2026, 1, 5)

private fun task(
    recurrenceType: RecurrenceType,
    assignedDateTime: LocalDateTime?,
    repeatDayOfWeek: DayOfWeek? = null,
    repeatDayOfMonth: Int? = null,
) = Task(
    title = "Task",
    description = "",
    priority = Priority.MEDIUM,
    recurrenceType = recurrenceType,
    repeatDayOfWeek = repeatDayOfWeek,
    repeatDayOfMonth = repeatDayOfMonth,
    assignedDateTime = assignedDateTime,
)

private fun range(start: LocalDate, end: LocalDate) = DateRange(start, end)

class TaskRecurrenceTest {

    @Test
    fun `undated task has no occurrences`() {
        val task = task(RecurrenceType.DAILY, assignedDateTime = null)

        assertTrue(task.occurrencesIn(range(MONDAY, MONDAY.plusDays(7))).isEmpty())
        assertNull(task.nextOccurrenceFrom(MONDAY))
    }

    @Test
    fun `one-off task yields its date only when the range covers it`() {
        val task = task(RecurrenceType.NONE, MONDAY.atTime(9, 0))

        assertEquals(listOf(MONDAY.atTime(9, 0)), task.occurrencesIn(range(MONDAY, MONDAY.plusDays(3))))
        assertTrue(task.occurrencesIn(range(MONDAY.plusDays(1), MONDAY.plusDays(3))).isEmpty())
    }

    @Test
    fun `daily task expands across the whole range at the anchor time`() {
        val task = task(RecurrenceType.DAILY, MONDAY.atTime(9, 0))

        val occurrences = task.occurrencesIn(range(MONDAY, MONDAY.plusDays(3)))

        assertEquals(
            listOf(
                MONDAY.atTime(9, 0),
                MONDAY.plusDays(1).atTime(9, 0),
                MONDAY.plusDays(2).atTime(9, 0),
                MONDAY.plusDays(3).atTime(9, 0),
            ),
            occurrences,
        )
    }

    @Test
    fun `expansion never starts before the anchor`() {
        val task = task(RecurrenceType.DAILY, MONDAY.plusDays(2).atTime(9, 0))

        val occurrences = task.occurrencesIn(range(MONDAY, MONDAY.plusDays(3)))

        assertEquals(
            listOf(MONDAY.plusDays(2).atTime(9, 0), MONDAY.plusDays(3).atTime(9, 0)),
            occurrences,
        )
    }

    @Test
    fun `weekly task lands on its configured weekday`() {
        val task = task(RecurrenceType.WEEKLY, MONDAY.atTime(17, 0), repeatDayOfWeek = DayOfWeek.FRIDAY)

        val occurrences = task.occurrencesIn(range(MONDAY, MONDAY.plusDays(15)))

        // Fridays are the 9th and the 16th.
        assertEquals(
            listOf(MONDAY.plusDays(4).atTime(17, 0), MONDAY.plusDays(11).atTime(17, 0)),
            occurrences,
        )
    }

    @Test
    fun `weekly task falls back to the anchor weekday`() {
        val task = task(RecurrenceType.WEEKLY, MONDAY.atTime(17, 0))

        val occurrences = task.occurrencesIn(range(MONDAY, MONDAY.plusDays(15)))

        assertEquals(
            listOf(
                MONDAY.atTime(17, 0),
                MONDAY.plusDays(7).atTime(17, 0),
                MONDAY.plusDays(14).atTime(17, 0),
            ),
            occurrences,
        )
    }

    @Test
    fun `monthly task on the 31st clamps to shorter months`() {
        val anchor = LocalDate.of(2026, 1, 31).atTime(8, 0)
        val task = task(RecurrenceType.MONTHLY, anchor, repeatDayOfMonth = 31)

        val occurrences = task.occurrencesIn(range(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 30)))

        assertEquals(
            listOf(
                LocalDate.of(2026, 1, 31).atTime(8, 0),
                LocalDate.of(2026, 2, 28).atTime(8, 0),
                LocalDate.of(2026, 3, 31).atTime(8, 0),
                LocalDate.of(2026, 4, 30).atTime(8, 0),
            ),
            occurrences,
        )
    }

    @Test
    fun `next occurrence of a daily task is the day asked for`() {
        val task = task(RecurrenceType.DAILY, MONDAY.atTime(9, 0))

        assertEquals(MONDAY.plusDays(5).atTime(9, 0), task.nextOccurrenceFrom(MONDAY.plusDays(5)))
    }

    @Test
    fun `next occurrence is never earlier than the anchor`() {
        val task = task(RecurrenceType.DAILY, MONDAY.plusDays(10).atTime(9, 0))

        assertEquals(MONDAY.plusDays(10).atTime(9, 0), task.nextOccurrenceFrom(MONDAY))
    }

    @Test
    fun `next occurrence of a weekly task is the closest matching weekday`() {
        val task = task(RecurrenceType.WEEKLY, MONDAY.atTime(17, 0), repeatDayOfWeek = DayOfWeek.FRIDAY)

        // From Wednesday the 7th, the next Friday is the 9th.
        assertEquals(MONDAY.plusDays(4).atTime(17, 0), task.nextOccurrenceFrom(MONDAY.plusDays(2)))
    }

    @Test
    fun `next occurrence of a monthly task rolls into the following month`() {
        val anchor = LocalDate.of(2026, 1, 31).atTime(8, 0)
        val task = task(RecurrenceType.MONTHLY, anchor, repeatDayOfMonth = 31)

        assertEquals(
            LocalDate.of(2026, 2, 28).atTime(8, 0),
            task.nextOccurrenceFrom(LocalDate.of(2026, 2, 1)),
        )
    }

    @Test
    fun `a one-off task that has already passed has no next occurrence`() {
        val task = task(RecurrenceType.NONE, MONDAY.atTime(9, 0))

        assertNull(task.nextOccurrenceFrom(MONDAY.plusDays(1)))
        assertEquals(MONDAY.atTime(9, 0), task.nextOccurrenceFrom(MONDAY))
    }
}
