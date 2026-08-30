package inc.dimasic.taskcal.model

import java.time.LocalDate
import java.util.UUID

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val gender: Gender,
    val dateOfBirth: LocalDate,
    val height: Double,
    val weight: Double,
    val weightGoal: Double,
    val nutritionGoal: NutritionGoal,
)

enum class Gender(val label: String) {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other"),
}

data class NutritionGoal(
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
)
