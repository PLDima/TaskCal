package inc.dimasic.taskcal.domain.usecase.model

/** What was eaten on a single day, against the goals from the user's profile. */
data class CaloriesSummary(
    val caloriesUsed: Int,
    val caloriesGoal: Int,
    val caloriesBurned: Int,
    val proteinUsed: Double,
    val proteinGoal: Double,
    val carbsUsed: Double,
    val carbsGoal: Double,
    val fatUsed: Double,
    val fatGoal: Double,
) {
    companion object {
        val Empty = CaloriesSummary(0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    }
}
