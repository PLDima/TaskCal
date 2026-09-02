package inc.dimasic.taskcal.domain.usecase.model

/**
 * Headline task figures for a period. [completed] counts everything done, late or not, which is
 * why it can exceed the completed figure in [TaskStatusBreakdown].
 */
data class TasksSummary(
    val available: Int,
    val completed: Int,
    val onTimeRatePercent: Int,
) {
    companion object {
        val Empty = TasksSummary(available = 0, completed = 0, onTimeRatePercent = 0)
    }
}
