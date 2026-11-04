package inc.dimasic.taskcal.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DateRange(val start: LocalDate, val end: LocalDate) {

    val dayCount: Int get() = ChronoUnit.DAYS.between(start, end).toInt() + 1

    operator fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)

    /** Every date from [start] through [end], in order. */
    fun dates(): List<LocalDate> = (0 until dayCount).map { start.plusDays(it.toLong()) }
}