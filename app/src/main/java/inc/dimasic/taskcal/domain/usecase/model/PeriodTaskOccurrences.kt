package inc.dimasic.taskcal.domain.usecase.model

import inc.dimasic.taskcal.domain.model.DateRange

/** Every occurrence expanded across a period, with the period's bounds. */
data class PeriodTaskOccurrences(
    /** Null for the all-time period, which has no bounds. */
    val dateRange: DateRange?,
    val occurrences: List<TaskOccurrenceStat>,
) {
    companion object {
        val Empty = PeriodTaskOccurrences(dateRange = null, occurrences = emptyList())
    }
}
