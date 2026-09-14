package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.repository.SettingsRepository
import inc.dimasic.taskcal.domain.model.Gender
import inc.dimasic.taskcal.domain.model.UnitSystem
import inc.dimasic.taskcal.domain.usecase.model.ProfileDetails
import inc.dimasic.taskcal.domain.usecase.model.ProfileGoals
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {

    val unitSystem: Flow<UnitSystem> = settingsRepository.unitSystem

    suspend fun updateUnitSystem(unitSystem: UnitSystem) {
        settingsRepository.updateUnitSystem(unitSystem)
    }

    val details: Flow<ProfileDetails> = settingsRepository.userProfile.map { profile ->
        ProfileDetails(
            name = profile.name,
            gender = profile.gender,
            dateOfBirth = profile.dateOfBirth,
            heightCm = profile.heightCm,
        )
    }

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

    suspend fun updateDetails(name: String, gender: Gender?, dateOfBirth: LocalDate?, heightCm: Int?) {
        settingsRepository.updateDetails(name.trim(), gender, dateOfBirth, heightCm)
    }

    suspend fun updateCaloriesGoal(calories: Int) {
        settingsRepository.updateCaloriesGoal(calories)
    }

    suspend fun updateStepsGoal(steps: Int) {
        settingsRepository.updateStepsGoal(steps)
    }

    suspend fun updateMacroGoals(protein: Double, carbs: Double, fat: Double) {
        settingsRepository.updateMacroGoals(protein, carbs, fat)
    }

    suspend fun updateWeightGoal(weightGoal: Double) {
        settingsRepository.updateWeightGoal(weightGoal)
    }
}
