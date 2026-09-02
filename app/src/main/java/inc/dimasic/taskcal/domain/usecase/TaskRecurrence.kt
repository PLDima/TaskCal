package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.model.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

/**
 * Expansion of a [Task]'s recurrence rule into the individual occurrences it stands for.
 *
 * Repeating tasks store no instances, so every screen that needs them derives them from here.
 * A task with no [Task.assignedDateTime] has nothing to expand, and no occurrence is ever
 * produced before that anchor — it doubles as the start of the series.
 */

/** Every occurrence falling inside [range], in chronological order. */
fun Task.occurrencesIn(range: DateRange): List<LocalDateTime> {
    val anchor = assignedDateTime ?: return emptyList()
    val anchorDate = anchor.toLocalDate()
    if (anchorDate > range.end) return emptyList()

    val time = anchor.toLocalTime()
    val from = maxOf(range.start, anchorDate)

    return when (recurrenceType) {
        RecurrenceType.NONE -> if (anchorDate in range) listOf(anchor) else emptyList()

        RecurrenceType.DAILY -> from.datesUntilInclusive(range.end).map { it.atTime(time) }

        RecurrenceType.WEEKLY -> {
            val weekday = repeatDayOfWeek ?: anchor.dayOfWeek
            from.datesUntilInclusive(range.end)
                .filter { it.dayOfWeek == weekday }
                .map { it.atTime(time) }
        }

        RecurrenceType.MONTHLY -> {
            val day = repeatDayOfMonth ?: anchor.dayOfMonth
            generateSequence(from.withDayOfMonth(1)) { it.plusMonths(1) }
                .takeWhile { it <= range.end }
                .map { month -> month.clampedToDay(day) }
                .filter { it >= from && it <= range.end }
                .map { it.atTime(time) }
                .toList()
        }
    }
}

/**
 * The closest occurrence on or after [date], or null if there is none. List screens show a
 * repeating task once, at this date, rather than projecting it out to an arbitrary horizon.
 */
fun Task.nextOccurrenceFrom(date: LocalDate): LocalDateTime? {
    val anchor = assignedDateTime ?: return null
    val anchorDate = anchor.toLocalDate()
    val time = anchor.toLocalTime()
    val from = maxOf(date, anchorDate)

    return when (recurrenceType) {
        RecurrenceType.NONE -> anchor.takeIf { anchorDate >= date }

        RecurrenceType.DAILY -> from.atTime(time)

        RecurrenceType.WEEKLY -> {
            val weekday = repeatDayOfWeek ?: anchor.dayOfWeek
            from.with(TemporalAdjusters.nextOrSame(weekday)).atTime(time)
        }

        RecurrenceType.MONTHLY -> {
            val day = repeatDayOfMonth ?: anchor.dayOfMonth
            val thisMonth = from.clampedToDay(day)
            val occurrence = if (thisMonth >= from) thisMonth else from.plusMonths(1).clampedToDay(day)
            occurrence.atTime(time)
        }
    }
}

/**
 * This month's [day], pulled back to the last day when the month is too short — so a task
 * anchored on the 31st still lands in February instead of disappearing.
 */
private fun LocalDate.clampedToDay(day: Int): LocalDate =
    withDayOfMonth(day.coerceIn(1, lengthOfMonth()))

private fun LocalDate.datesUntilInclusive(end: LocalDate): List<LocalDate> =
    generateSequence(this) { it.plusDays(1) }.takeWhile { it <= end }.toList()
