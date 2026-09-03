package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.domain.model.ActivitiesPeriod
import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.NutritionPeriod
import inc.dimasic.taskcal.domain.model.TasksPeriod
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Turning a period and an offset into the dates it covers.
 *
 * Which dates "this week" or "last month" mean is a domain decision, so every screen resolves it
 * through here rather than each repository keeping its own copy. The period enums differ per
 * feature, but they are all built from the same three primitives below.
 */

/** The week [offset] weeks from the current one, Monday through Sunday. */
internal fun weekRangeAt(offset: Int): DateRange {
    val monday = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .plusWeeks(offset.toLong())
    return DateRange(start = monday, end = monday.plusDays(6))
}

/** The month [offset] months from the current one, first day through last. */
internal fun monthRangeAt(offset: Int): DateRange {
    val firstOfMonth = LocalDate.now()
        .with(TemporalAdjusters.firstDayOfMonth())
        .plusMonths(offset.toLong())
    return DateRange(start = firstOfMonth, end = firstOfMonth.with(TemporalAdjusters.lastDayOfMonth()))
}

/** The single day [offset] days from today. */
internal fun dayRangeAt(offset: Int): DateRange {
    val day = LocalDate.now().plusDays(offset.toLong())
    return DateRange(start = day, end = day)
}

internal fun ActivitiesPeriod.dateRangeAt(offset: Int): DateRange = when (this) {
    ActivitiesPeriod.DAY -> dayRangeAt(offset)
    ActivitiesPeriod.WEEK -> weekRangeAt(offset)
}

internal fun NutritionPeriod.dateRangeAt(offset: Int): DateRange = when (this) {
    NutritionPeriod.WEEK -> weekRangeAt(offset)
    NutritionPeriod.MONTH -> monthRangeAt(offset)
}

/** Null for [TasksPeriod.ALL_TIME], which has no bounds. */
internal fun TasksPeriod.dateRangeAt(offset: Int): DateRange? = when (this) {
    TasksPeriod.WEEK -> weekRangeAt(offset)
    TasksPeriod.MONTH -> monthRangeAt(offset)
    TasksPeriod.ALL_TIME -> null
}
