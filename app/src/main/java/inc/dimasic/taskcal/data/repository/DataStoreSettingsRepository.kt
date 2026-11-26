package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.local.datastore.PreferencesDataStoreManager
import inc.dimasic.taskcal.data.local.datastore.UserProfileKeys
import inc.dimasic.taskcal.data.local.datastore.toUnitSystem
import inc.dimasic.taskcal.data.local.datastore.toUserProfile
import inc.dimasic.taskcal.domain.model.Gender
import inc.dimasic.taskcal.domain.model.UnitSystem
import inc.dimasic.taskcal.domain.model.UserProfile
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [SettingsRepository] backed by DataStore Preferences, one key per goal. */
@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val preferences: PreferencesDataStoreManager,
) : SettingsRepository {

    override val userProfile: Flow<UserProfile> = preferences.preferences.map { it.toUserProfile() }

    override val unitSystem: Flow<UnitSystem> = preferences.preferences.map { it.toUnitSystem() }

    override suspend fun updateUnitSystem(unitSystem: UnitSystem) {
        preferences.set(UserProfileKeys.UNIT_SYSTEM, unitSystem.name)
    }

    override suspend fun updateDetails(name: String, gender: Gender?, dateOfBirth: LocalDate?, heightCm: Int?) {
        preferences.edit {
            it[UserProfileKeys.NAME] = name
            if (gender != null) it[UserProfileKeys.GENDER] = gender.name else it.remove(UserProfileKeys.GENDER)
            if (dateOfBirth != null) it[UserProfileKeys.DATE_OF_BIRTH] = dateOfBirth.toString() else it.remove(UserProfileKeys.DATE_OF_BIRTH)
            if (heightCm != null) it[UserProfileKeys.HEIGHT_CM] = heightCm else it.remove(UserProfileKeys.HEIGHT_CM)
        }
    }

    override suspend fun updateCaloriesGoal(calories: Int) {
        preferences.set(UserProfileKeys.CALORIES_GOAL, calories)
    }

    override suspend fun updateMacroGoals(protein: Double, carbs: Double, fat: Double) {
        preferences.edit {
            it[UserProfileKeys.PROTEIN_GOAL] = protein
            it[UserProfileKeys.CARBS_GOAL] = carbs
            it[UserProfileKeys.FAT_GOAL] = fat
        }
    }

    override suspend fun updateWeightGoal(weightGoal: Double) {
        preferences.set(UserProfileKeys.WEIGHT_GOAL, weightGoal)
    }

    override suspend fun updateStepsGoal(stepsGoal: Int) {
        preferences.set(UserProfileKeys.STEPS_GOAL, stepsGoal)
    }
}
