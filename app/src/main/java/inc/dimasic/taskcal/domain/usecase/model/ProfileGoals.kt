package inc.dimasic.taskcal.domain.usecase.model

/** The goals a user has set, flattened out of their profile for the Profile screen. */
data class ProfileGoals(
    val caloriesGoal: Int,
    val stepsGoal: Int,
    val proteinGoal: Double,
    val carbsGoal: Double,
    val fatGoal: Double,
    val weightGoalKg: Double,
) {
    companion object {
        val Empty = ProfileGoals(
            caloriesGoal = 0,
            stepsGoal = 0,
            proteinGoal = 0.0,
            carbsGoal = 0.0,
            fatGoal = 0.0,
            weightGoalKg = 0.0,
        )
    }
}
