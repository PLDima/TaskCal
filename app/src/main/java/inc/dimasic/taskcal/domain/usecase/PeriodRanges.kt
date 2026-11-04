package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.domain.model.ActivitiesPeriod
import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.NutritionPeriod
import inc.dimasic.taskcal.domain.model.TasksPeriod
import inc.dimasic.taskcal.domain.usecase.model.ChartBucket
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.ceil

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

private val WEEKDAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private const val MONTH_CHART_BUCKETS = 5

/**
 * A week's chart columns: one per day, Monday through Sunday. Every day keeps its slot whether or
 * not anything happened on it, so the chart's positions never shift.
 */
internal fun DateRange.weekChartBuckets(): List<ChartBucket> =
    WEEKDAY_LABELS.mapIndexed { index, label ->
        val date = start.plusDays(index.toLong())
        ChartBucket(label = label, range = DateRange(date, date))
    }

/**
 * A month's chart columns: [MONTH_CHART_BUCKETS] consecutive, equal-sized runs of days, the last
 * one cut short at the month's end. A 30-day month splits 6/6/6/6/6, a 31-day one 7/7/7/7/3.
 */
internal fun DateRange.monthChartBuckets(): List<ChartBucket> {
    val bucketSize = ceil(dayCount / MONTH_CHART_BUCKETS.toDouble()).toInt().coerceAtLeast(1)
    return (0 until MONTH_CHART_BUCKETS).map { index ->
        val bucketStart = start.plusDays((index * bucketSize).toLong())
        val bucketEnd = minOf(bucketStart.plusDays((bucketSize - 1).toLong()), end)
        ChartBucket(label = "W${index + 1}", range = DateRange(bucketStart, bucketEnd))
    }
}
