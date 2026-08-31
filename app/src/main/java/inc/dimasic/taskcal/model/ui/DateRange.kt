package inc.dimasic.taskcal.model.ui

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DateRange(val start: LocalDate, val end: LocalDate) {

    val dayCount: Int get() = ChronoUnit.DAYS.between(start, end).toInt() + 1

    operator fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)
}