package inc.dimasic.taskcal.ui.screens.statistic.nutrition

/** Period nutrition totals with the best and worst days already formatted for display. */
data class NutritionSummaryUi(
    val totalConsumed: Int,
    val totalBurned: Int,
    val avgBalance: Int,
    val bestDayLabel: String,
    val worstDayLabel: String,
) {
    companion object {
        val Empty = NutritionSummaryUi(0, 0, 0, "-", "-")
    }
}
