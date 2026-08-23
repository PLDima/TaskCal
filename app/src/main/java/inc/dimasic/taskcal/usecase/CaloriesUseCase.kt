package inc.dimasic.taskcal.usecase

import inc.dimasic.taskcal.model.Gender
import inc.dimasic.taskcal.model.NutritionGoal
import inc.dimasic.taskcal.model.UserProfile
import inc.dimasic.taskcal.repository.FoodRepository
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

class CaloriesUseCase @Inject constructor(
    private val foodRepository: FoodRepository,
) {

    private val userProfile = MutableStateFlow(mockUserProfile())

    fun caloriesForDate(date: Flow<LocalDate>): Flow<CaloriesSummary> =
        combine(foodRepository.foodEntriesForDate(date), userProfile) { entriesForDate, profile ->
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
}
