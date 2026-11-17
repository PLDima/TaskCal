package inc.dimasic.taskcal.data.remote.model

import com.squareup.moshi.JsonClass
import inc.dimasic.taskcal.domain.model.Gender
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    val name: String,
    val gender: Gender? = null,
    val dateOfBirth: LocalDate? = null,
    val heightCm: Int? = null,
    val stepsGoal: Int,
    val distanceGoalMeters: Int,
    val caloriesBurnedGoal: Int,
    val weightGoal: Double,
    val nutritionGoal: NutritionGoalDto,
)