package inc.dimasic.taskcal.domain.usecase.profile

import inc.dimasic.taskcal.data.repository.SettingsRepository
import inc.dimasic.taskcal.domain.model.Gender
import inc.dimasic.taskcal.domain.model.UnitSystem
import inc.dimasic.taskcal.domain.usecase.model.ProfileDetails
import inc.dimasic.taskcal.domain.usecase.model.ProfileGoals
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileUseCaseImpl @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ProfileUseCase {

    override val unitSystem: Flow<UnitSystem> = settingsRepository.unitSystem

    override suspend fun updateUnitSystem(unitSystem: UnitSystem) {
        settingsRepository.updateUnitSystem(unitSystem)
    }

    override val details: Flow<ProfileDetails> = settingsRepository.userProfile.map { profile ->
        ProfileDetails(
            name = profile.name,
            gender = profile.gender,
            dateOfBirth = profile.dateOfBirth,
            heightCm = profile.heightCm,
        )
    }

    override val goals: Flow<ProfileGoals> = settingsRepository.userProfile.map { profile ->
        ProfileGoals(
            caloriesGoal = profile.nutritionGoal.calories,
            stepsGoal = profile.stepsGoal,
            proteinGoal = profile.nutritionGoal.protein,
            carbsGoal = profile.nutritionGoal.carbs,
            fatGoal = profile.nutritionGoal.fat,
            weightGoalKg = profile.weightGoal,
        )
    }

    override suspend fun updateDetails(name: String, gender: Gender?, dateOfBirth: LocalDate?, heightCm: Int?) {
        settingsRepository.updateDetails(name.trim(), gender, dateOfBirth, heightCm)
    }

    override suspend fun updateCaloriesGoal(calories: Int) {
        settingsRepository.updateCaloriesGoal(calories)
    }

    override suspend fun updateStepsGoal(steps: Int) {
        settingsRepository.updateStepsGoal(steps)
    }

    override suspend fun updateMacroGoals(protein: Double, carbs: Double, fat: Double) {
        settingsRepository.updateMacroGoals(protein, carbs, fat)
    }

    override suspend fun updateWeightGoal(weightGoal: Double) {
        settingsRepository.updateWeightGoal(weightGoal)
    }
}
