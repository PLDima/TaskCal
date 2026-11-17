package inc.dimasic.taskcal.data.remote.model

import inc.dimasic.taskcal.domain.model.FoodEntry

/** Conversion between [FoodEntry] and its wire form [FoodEntryDto]. */

fun FoodEntry.toDto() = FoodEntryDto(
    id = id,
    date = date,
    mealType = mealType,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    name = name,
)

fun FoodEntryDto.toDomain() = FoodEntry(
    id = requireNotNull(id) { "The server returned a food entry without an id" },
    date = date,
    mealType = mealType,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    name = name,
)