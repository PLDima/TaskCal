package inc.dimasic.taskcal.domain.usecase.model

/**
 * How a period's occurrences partition: the three counts always add up to the total. An
 * occurrence finished after its deadline counts as [missed], not [completed].
 */
data class TaskStatusBreakdown(
    val completed: Int,
    val missed: Int,
    val pending: Int,
) {
    companion object {
        val Empty = TaskStatusBreakdown(completed = 0, missed = 0, pending = 0)
    }
}
