package inc.dimasic.taskcal.data.database

import inc.dimasic.taskcal.domain.model.FoodEntry
import inc.dimasic.taskcal.domain.model.MealType
import java.time.LocalDate

/**
 * Conversion between [FoodEntry] and its stored [FoodEntryEntity] row. Pure functions, so they can
 * be round-trip tested without Room.
 */

fun FoodEntry.toEntity() = FoodEntryEntity(
    id = id,
    date = date.toString(),
    mealType = mealType.name,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    note = note,
)

fun FoodEntryEntity.toDomain() = FoodEntry(
    id = id,
    date = LocalDate.parse(date),
    mealType = enumValueOf<MealType>(mealType),
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    note = note,
)
