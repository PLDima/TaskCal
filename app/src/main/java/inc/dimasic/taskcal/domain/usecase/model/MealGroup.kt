package inc.dimasic.taskcal.domain.usecase.model

import inc.dimasic.taskcal.domain.model.FoodEntry
import inc.dimasic.taskcal.domain.model.MealType

/** A day's entries for one meal. Which meals exist, and in what order, is decided in the domain. */
data class MealGroup(
    val mealType: MealType,
    val entries: List<FoodEntry>,
)
