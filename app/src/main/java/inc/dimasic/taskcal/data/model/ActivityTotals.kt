package inc.dimasic.taskcal.data.model

/** Activity figures summed over a period, each alongside the goal for that same period. */
data class ActivityTotals(
    val caloriesBurned: Int,
    val caloriesGoal: Int,
    val steps: Int,
    val stepsGoal: Int,
    val distanceMeters: Int,
    val distanceGoalMeters: Int,
    /** Total active minutes over the period; no goal. */
    val activeMinutes: Int,
    /** Mean of the per-day averages over the period; no goal. */
    val avgHeartRate: Int,
) {
    companion object {
        val Empty = ActivityTotals(
            caloriesBurned = 0,
            caloriesGoal = 0,
            steps = 0,
            stepsGoal = 0,
            distanceMeters = 0,
            distanceGoalMeters = 0,
            activeMinutes = 0,
            avgHeartRate = 0,
        )
    }
}
