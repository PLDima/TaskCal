package inc.dimasic.taskcal.domain.usecase.profile

import inc.dimasic.taskcal.domain.model.Gender
import inc.dimasic.taskcal.domain.model.UnitSystem
import inc.dimasic.taskcal.domain.usecase.model.ProfileDetails
import inc.dimasic.taskcal.domain.usecase.model.ProfileGoals
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface ProfileUseCase {
    val unitSystem: Flow<UnitSystem>

    val details: Flow<ProfileDetails>

    val goals: Flow<ProfileGoals>

    suspend fun updateUnitSystem(unitSystem: UnitSystem)

    suspend fun updateDetails(name: String, gender: Gender?, dateOfBirth: LocalDate?, heightCm: Int?)

    suspend fun updateCaloriesGoal(calories: Int)

    suspend fun updateStepsGoal(steps: Int)

    suspend fun updateMacroGoals(protein: Double, carbs: Double, fat: Double)

    suspend fun updateWeightGoal(weightGoal: Double)
}
