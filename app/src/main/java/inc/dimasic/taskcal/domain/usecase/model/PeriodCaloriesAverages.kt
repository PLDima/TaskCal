package inc.dimasic.taskcal.domain.usecase.model

/** Per-day averages across a period. [avgBalance] is [avgConsumed] minus [avgBurned]. */
data class PeriodCaloriesAverages(
    val avgConsumed: Int,
    val avgBurned: Int,
    val avgBalance: Int,
) {
    companion object {
        val Empty = PeriodCaloriesAverages(0, 0, 0)
    }
}
