package inc.dimasic.taskcal.data.local.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import inc.dimasic.taskcal.domain.model.Gender
import inc.dimasic.taskcal.domain.model.NutritionGoal
import inc.dimasic.taskcal.domain.model.UnitSystem
import inc.dimasic.taskcal.domain.model.UserProfile
import java.time.LocalDate

/**
 * How a [UserProfile] is stored: one preference per field, so each can be updated on its own.
 * A field that has never been set reads as its [UserProfile.Default] value. [GENDER] holds the
 * enum name and [DATE_OF_BIRTH] an ISO-8601 date.
 */
internal object UserProfileKeys {
    val NAME = stringPreferencesKey("name")
    val GENDER = stringPreferencesKey("gender")
    val DATE_OF_BIRTH = stringPreferencesKey("date_of_birth")
    val HEIGHT_CM = intPreferencesKey("height_cm")
    val UNIT_SYSTEM = stringPreferencesKey("unit_system")
    val STEPS_GOAL = intPreferencesKey("steps_goal")
    val WEIGHT_GOAL = doublePreferencesKey("weight_goal")
    val CALORIES_GOAL = intPreferencesKey("calories_goal")
    val PROTEIN_GOAL = doublePreferencesKey("protein_goal")
    val CARBS_GOAL = doublePreferencesKey("carbs_goal")
    val FAT_GOAL = doublePreferencesKey("fat_goal")
}

internal fun Preferences.toUserProfile(): UserProfile {
    val default = UserProfile.Default
    return UserProfile(
        name = this[UserProfileKeys.NAME] ?: default.name,
        gender = this[UserProfileKeys.GENDER]?.let { Gender.valueOf(it) },
        dateOfBirth = this[UserProfileKeys.DATE_OF_BIRTH]?.let { LocalDate.parse(it) },
        heightCm = this[UserProfileKeys.HEIGHT_CM],
        stepsGoal = this[UserProfileKeys.STEPS_GOAL] ?: default.stepsGoal,
        distanceGoalMeters = default.distanceGoalMeters,
        caloriesBurnedGoal = default.caloriesBurnedGoal,
        weightGoal = this[UserProfileKeys.WEIGHT_GOAL] ?: default.weightGoal,
        nutritionGoal = NutritionGoal(
            calories = this[UserProfileKeys.CALORIES_GOAL] ?: default.nutritionGoal.calories,
            protein = this[UserProfileKeys.PROTEIN_GOAL] ?: default.nutritionGoal.protein,
            carbs = this[UserProfileKeys.CARBS_GOAL] ?: default.nutritionGoal.carbs,
            fat = this[UserProfileKeys.FAT_GOAL] ?: default.nutritionGoal.fat,
        ),
    )
}

/** Metric until the user says otherwise. */
internal fun Preferences.toUnitSystem(): UnitSystem =
    this[UserProfileKeys.UNIT_SYSTEM]?.let { UnitSystem.valueOf(it) } ?: UnitSystem.METRIC
