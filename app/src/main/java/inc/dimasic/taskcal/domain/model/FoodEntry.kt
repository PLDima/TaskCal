package inc.dimasic.taskcal.domain.model

import java.time.LocalDate
import java.util.UUID

data class FoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDate,
    val mealType: MealType,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val note: String,
)

enum class MealType(val label: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack"),
}
