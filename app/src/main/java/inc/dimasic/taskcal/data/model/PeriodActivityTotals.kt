package inc.dimasic.taskcal.data.model

import inc.dimasic.taskcal.domain.model.DateRange
import java.time.LocalDate

/** [ActivityTotals] together with the period they were summed over. */
data class PeriodActivityTotals(
    val dateRange: DateRange,
    val totals: ActivityTotals,
) {
    companion object {
        val Empty = PeriodActivityTotals(
            dateRange = DateRange(LocalDate.now(), LocalDate.now()),
            totals = ActivityTotals.Empty,
        )
    }
}
