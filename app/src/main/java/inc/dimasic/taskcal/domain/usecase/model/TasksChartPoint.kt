package inc.dimasic.taskcal.domain.usecase.model

/** One bar of the on-time-rate chart, as a percentage between 0 and 100. */
data class TasksChartPoint(
    val label: String,
    val onTimeRatePercent: Int,
)
