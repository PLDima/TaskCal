package inc.dimasic.taskcal.usecase

import inc.dimasic.taskcal.model.FoodEntry
import inc.dimasic.taskcal.model.Gender
import inc.dimasic.taskcal.model.MealType
import inc.dimasic.taskcal.model.NutritionGoal
import inc.dimasic.taskcal.model.UserProfile
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

private const val MOCK_CALORIES_BURNED = 2800

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

class CaloriesUseCase @Inject constructor() {

    private val foodEntries = MutableStateFlow(mockFoodEntries())
    private val userProfile = MutableStateFlow(mockUserProfile())

    fun caloriesForDate(date: Flow<LocalDate>): Flow<CaloriesSummary> =
        combine(foodEntries, userProfile, date) { entries, profile, selectedDate ->
            val entriesForDate = entries.filter { it.date == selectedDate }
            val goal = profile.nutritionGoal
            CaloriesSummary(
                caloriesUsed = entriesForDate.sumOf { it.calories },
                caloriesGoal = goal.calories,
                caloriesBurned = MOCK_CALORIES_BURNED,
                proteinUsed = entriesForDate.sumOf { it.protein },
                proteinGoal = goal.protein,
                carbsUsed = entriesForDate.sumOf { it.carbs },
                carbsGoal = goal.carbs,
                fatUsed = entriesForDate.sumOf { it.fat },
                fatGoal = goal.fat,
            )
        }

    private fun mockUserProfile(): UserProfile = UserProfile(
        name = "Alex",
        email = "alex@example.com",
        gender = Gender.OTHER,
        dateOfBirth = LocalDate.of(1995, 5, 20),
        height = 175.0,
        weight = 70.0,
        nutritionGoal = NutritionGoal(
            calories = 3000,
            protein = 140.0,
            carbs = 250.0,
            fat = 70.0,
        ),
    )

    private fun mockFoodEntries(): List<FoodEntry> {
        val today = LocalDate.now()
        return listOf(
            FoodEntry(
                date = today,
                mealType = MealType.BREAKFAST,
                calories = 420,
                protein = 28.0,
                carbs = 5.0,
                fat = 14.0,
                note = "Oatmeal with eggs",
            ),
            FoodEntry(
                date = today,
                mealType = MealType.LUNCH,
                calories = 680,
                protein = 5.0,
                carbs = 70.0,
                fat = 20.0,
                note = "Chicken bowl",
            ),
            FoodEntry(
                date = today,
                mealType = MealType.SNACK,
                calories = 210,
                protein = 2.0,
                carbs = 20.0,
                fat = 8.0,
                note = "Protein bar",
            ),
        )
    }
}
