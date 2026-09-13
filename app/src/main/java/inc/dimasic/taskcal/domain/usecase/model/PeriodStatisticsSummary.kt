package inc.dimasic.taskcal.domain.usecase.model

import inc.dimasic.taskcal.domain.model.DateRange
import java.time.LocalDate

/**
 * Everything the Nutrition statistics screen's main content needs for one period, loaded together.
 * The macro breakdown (Protein/Fat/Carbs) is deliberately not part of this: it loads on demand,
 * per macro, only once the user asks to see it.
 */
data class PeriodStatisticsSummary(
    val dateRange: DateRange,
    val averages: PeriodCaloriesAverages,
    val summary: PeriodCaloriesSummary,
    val caloriesChart: List<ChartPoint>,
) {
    companion object {
        val Empty = PeriodStatisticsSummary(
            dateRange = DateRange(LocalDate.now(), LocalDate.now()),
            averages = PeriodCaloriesAverages.Empty,
            summary = PeriodCaloriesSummary.Empty,
            caloriesChart = emptyList(),
        )
    }
}
