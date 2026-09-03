package inc.dimasic.taskcal.domain.usecase.model

/**
 * One labelled column of a chart.
 *
 * [values] holds one figure per series being drawn, in the order the chart declares its series.
 * What each figure means is named there, alongside its colour, rather than being repeated in a
 * separate point type per chart.
 */
data class ChartPoint(
    val label: String,
    val values: List<Int>,
)
