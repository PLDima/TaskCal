package inc.dimasic.taskcal.domain.usecase.model

import java.time.LocalDate

/**
 * Totals across a period, plus the days at each end of the calorie balance. Best is the largest
 * deficit, worst the largest surplus, so [bestDayBalance] is never above [worstDayBalance].
 */
data class PeriodCaloriesSummary(
    val totalConsumed: Int,
    val totalBurned: Int,
    val avgBalance: Int,
    val bestDay: LocalDate?,
    val bestDayBalance: Int,
    val worstDay: LocalDate?,
    val worstDayBalance: Int,
) {
    companion object {
        val Empty = PeriodCaloriesSummary(0, 0, 0, null, 0, null, 0)
    }
}
