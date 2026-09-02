package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.repository.SettingsRepository
import inc.dimasic.taskcal.domain.usecase.model.ProfileGoals
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
