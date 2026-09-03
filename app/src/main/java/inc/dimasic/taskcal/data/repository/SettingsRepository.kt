package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.domain.model.Gender
import inc.dimasic.taskcal.domain.model.NutritionGoal
import inc.dimasic.taskcal.domain.model.UserProfile
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Singleton
class SettingsRepository @Inject constructor() {

    private val profile = MutableStateFlow(mockUserProfile())

    val userProfile: Flow<UserProfile> = profile

    fun updateCaloriesGoal(calories: Int) {
        profile.value = profile.value.copy(nutritionGoal = profile.value.nutritionGoal.copy(calories = calories))
    }

    fun updateMacroGoals(protein: Double, carbs: Double, fat: Double) {
        profile.value = profile.value.copy(
            nutritionGoal = profile.value.nutritionGoal.copy(protein = protein, carbs = carbs, fat = fat),
        )
    }

    fun updateWeightGoal(weightGoal: Double) {
        profile.value = profile.value.copy(weightGoal = weightGoal)
    }

    fun updateStepsGoal(stepsGoal: Int) {
        profile.value = profile.value.copy(stepsGoal = stepsGoal)
    }

    private fun mockUserProfile(): UserProfile = UserProfile(
        name = "Alex",
        email = "alex@example.com",
        gender = Gender.OTHER,
        dateOfBirth = LocalDate.of(1995, 5, 20),
        height = 175.0,
        weight = 70.0,
        weightGoal = 68.0,
        stepsGoal = 10_000,
        nutritionGoal = NutritionGoal(
            calories = 3000,
            protein = 140.0,
            carbs = 250.0,
            fat = 70.0,
        ),
    )
}
