package inc.dimasic.taskcal.data.model

import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.FoodEntry
import java.time.LocalDate

/** The food entries stored within a date range, as read back from the repository. */
data class PeriodFoodEntries(
    val dateRange: DateRange,
    val entries: List<FoodEntry>,
) {
    companion object {
        val Empty = PeriodFoodEntries(DateRange(LocalDate.now(), LocalDate.now()), emptyList())
    }
}
