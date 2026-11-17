package inc.dimasic.taskcal.data.remote.model

import inc.dimasic.taskcal.domain.model.NutritionGoal
import inc.dimasic.taskcal.domain.model.UserProfile

/** Conversion between [UserProfile] and its wire form [UserProfileDto]. */

fun UserProfile.toDto() = UserProfileDto(
    name = name,
    gender = gender,
    dateOfBirth = dateOfBirth,
    heightCm = heightCm,
    stepsGoal = stepsGoal,
    distanceGoalMeters = distanceGoalMeters,
    caloriesBurnedGoal = caloriesBurnedGoal,
    weightGoal = weightGoal,
    nutritionGoal = NutritionGoalDto(
        calories = nutritionGoal.calories,
        protein = nutritionGoal.protein,
        carbs = nutritionGoal.carbs,
        fat = nutritionGoal.fat,
    ),
)

fun UserProfileDto.toDomain() = UserProfile(
    name = name,
    gender = gender,
    dateOfBirth = dateOfBirth,
    heightCm = heightCm,
    stepsGoal = stepsGoal,
    distanceGoalMeters = distanceGoalMeters,
    caloriesBurnedGoal = caloriesBurnedGoal,
    weightGoal = weightGoal,
    nutritionGoal = NutritionGoal(
        calories = nutritionGoal.calories,
        protein = nutritionGoal.protein,
        carbs = nutritionGoal.carbs,
        fat = nutritionGoal.fat,
    ),
)