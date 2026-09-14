package inc.dimasic.taskcal.ui.screens.statistic.nutrition

import inc.dimasic.taskcal.domain.usecase.model.ChartPoint
import inc.dimasic.taskcal.domain.usecase.model.PeriodCaloriesAverages

/**
 * The Nutrition statistics content for one period. [averages] and [caloriesChart] pass through
 * as-is — they are already field-for-field what the screen renders.
 */
data class NutritionPeriodUi(
    val dateRangeLabel: String,
    val averages: PeriodCaloriesAverages,
    val summary: NutritionSummaryUi,
    val caloriesChart: List<ChartPoint>,
)
