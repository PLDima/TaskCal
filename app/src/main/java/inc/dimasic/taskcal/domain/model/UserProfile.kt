package inc.dimasic.taskcal.domain.model

import java.time.LocalDate

/**
 * Who the user is and the goals they have set for themselves. The personal details are all
 * optional — a fresh install knows nothing about the user until they fill the details screen in.
 *
 * [distanceGoalMeters] and [caloriesBurnedGoal] are not set by the user: the Activities
 * statistics measure against them, but the Profile screen doesn't offer them, so they always
 * read as their [Default] values.
 */
data class UserProfile(
    val name: String,
    val gender: Gender?,
    val dateOfBirth: LocalDate?,
    val heightCm: Int?,
    val stepsGoal: Int,
    val distanceGoalMeters: Int,
    /** Total calories burned per day, basal included — the figure Health Connect reports. */
    val caloriesBurnedGoal: Int,
    val weightGoal: Double,
    val nutritionGoal: NutritionGoal,
) {
    companion object {
        /** What a fresh install starts from, before the user has set anything. */
        val Default = UserProfile(
            name = "",
            gender = null,
            dateOfBirth = null,
            heightCm = null,
            stepsGoal = 10_000,
            distanceGoalMeters = 5_000,
            caloriesBurnedGoal = 2_500,
            weightGoal = 68.0,
            nutritionGoal = NutritionGoal(
                calories = 3000,
                protein = 140.0,
                carbs = 250.0,
                fat = 70.0,
            ),
        )
    }
}
