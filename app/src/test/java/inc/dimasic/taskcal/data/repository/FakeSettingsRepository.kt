package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.domain.model.Gender
import inc.dimasic.taskcal.domain.model.UnitSystem
import inc.dimasic.taskcal.domain.model.UserProfile
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [SettingsRepository], starting from [UserProfile.Default] unless told otherwise. */
internal class FakeSettingsRepository(initial: UserProfile = UserProfile.Default) : SettingsRepository {

    private val profile = MutableStateFlow(initial)

    override val userProfile: Flow<UserProfile> = profile

    private val units = MutableStateFlow(UnitSystem.METRIC)

    override val unitSystem: Flow<UnitSystem> = units

    override suspend fun updateUnitSystem(unitSystem: UnitSystem) {
        units.value = unitSystem
    }

    override suspend fun updateDetails(name: String, gender: Gender?, dateOfBirth: LocalDate?, heightCm: Int?) {
        profile.value = profile.value.copy(name = name, gender = gender, dateOfBirth = dateOfBirth, heightCm = heightCm)
    }

    override suspend fun updateCaloriesGoal(calories: Int) {
        profile.value = profile.value.copy(nutritionGoal = profile.value.nutritionGoal.copy(calories = calories))
    }

    override suspend fun updateMacroGoals(protein: Double, carbs: Double, fat: Double) {
        profile.value = profile.value.copy(
            nutritionGoal = profile.value.nutritionGoal.copy(protein = protein, carbs = carbs, fat = fat),
        )
    }

    override suspend fun updateWeightGoal(weightGoal: Double) {
        profile.value = profile.value.copy(weightGoal = weightGoal)
    }

    override suspend fun updateStepsGoal(stepsGoal: Int) {
        profile.value = profile.value.copy(stepsGoal = stepsGoal)
    }
}
