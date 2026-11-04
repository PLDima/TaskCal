package inc.dimasic.taskcal.domain.usecase.model

import java.time.LocalDate

/**
 * Where the user's weight stands against their goal. [currentKg] is null when nothing has been
 * weighed in; [changeKg] is null unless the period asked about holds at least two weigh-ins.
 */
data class WeightSummary(
    val currentKg: Double?,
    val measuredOn: LocalDate?,
    val goalKg: Double,
    /** Last weigh-in of the period minus the first: negative means weight was lost. */
    val changeKg: Double?,
) {
    /** How far above the goal the user still is; negative once they are under it. */
    val toGoalKg: Double? get() = currentKg?.let { it - goalKg }
}
