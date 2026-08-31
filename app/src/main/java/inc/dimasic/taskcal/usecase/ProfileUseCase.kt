package inc.dimasic.taskcal.usecase

import inc.dimasic.taskcal.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

class ProfileUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {

    val goals: Flow<ProfileGoals> = settingsRepository.userProfile.map { profile ->
        ProfileGoals(
            caloriesGoal = profile.nutritionGoal.calories,
            stepsGoal = profile.stepsGoal,
            proteinGoal = profile.nutritionGoal.protein,
            carbsGoal = profile.nutritionGoal.carbs,
            fatGoal = profile.nutritionGoal.fat,
            weightGoalKg = profile.weightGoal,
        )
    }

    fun updateCaloriesGoal(calories: Int) {
        settingsRepository.updateCaloriesGoal(calories)
    }

    fun updateStepsGoal(steps: Int) {
        settingsRepository.updateStepsGoal(steps)
    }

    fun updateMacroGoals(protein: Double, carbs: Double, fat: Double) {
        settingsRepository.updateMacroGoals(protein, carbs, fat)
    }

    fun updateWeightGoal(weightGoal: Double) {
        settingsRepository.updateWeightGoal(weightGoal)
    }
}
