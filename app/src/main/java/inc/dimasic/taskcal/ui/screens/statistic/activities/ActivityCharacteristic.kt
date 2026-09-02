package inc.dimasic.taskcal.ui.screens.statistic.activities

/**
 * One row of the activity characteristics list: value done this period, optionally with a
 * "% of daily goal" figure ([goalPercent]) and a progress bar ([fraction]). Either is null when
 * the characteristic has no goal to track against (e.g. average heart rate).
 */
data class ActivityCharacteristic(
    val title: String,
    val valueLabel: String,
    val goalPercent: Int?,
    val fraction: Float?,
)
