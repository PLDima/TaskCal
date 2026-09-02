package inc.dimasic.taskcal.domain.usecase.model

/** One bar of the calories chart: eaten versus burned for the bucket labelled [label]. */
data class CaloriesChartPoint(
    val label: String,
    val consumed: Int,
    val burned: Int,
)
