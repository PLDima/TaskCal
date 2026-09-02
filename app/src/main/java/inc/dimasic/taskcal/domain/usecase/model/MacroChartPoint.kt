package inc.dimasic.taskcal.domain.usecase.model

/** One bar of a single-macro chart, in grams. */
data class MacroChartPoint(
    val label: String,
    val value: Int,
)
