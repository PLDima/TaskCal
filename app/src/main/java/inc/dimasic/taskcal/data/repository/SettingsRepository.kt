package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.domain.model.Gender
import inc.dimasic.taskcal.domain.model.UnitSystem
import inc.dimasic.taskcal.domain.model.UserProfile
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/**
 * The user's profile and goals. What a goal means for a given period, and how a figure measures
 * against it, are domain decisions and live in the use cases instead.
 */
interface SettingsRepository {

    /** The current profile, re-emitted whenever any field changes. */
    val userProfile: Flow<UserProfile>

    /** The units measurements are shown in; metric until changed. */
    val unitSystem: Flow<UnitSystem>

    suspend fun updateUnitSystem(unitSystem: UnitSystem)

    /** Replaces the personal details as one; a null clears that detail. */
    suspend fun updateDetails(name: String, gender: Gender?, dateOfBirth: LocalDate?, heightCm: Int?)

    suspend fun updateCaloriesGoal(calories: Int)

    suspend fun updateMacroGoals(protein: Double, carbs: Double, fat: Double)

    suspend fun updateWeightGoal(weightGoal: Double)

    suspend fun updateStepsGoal(stepsGoal: Int)
}
