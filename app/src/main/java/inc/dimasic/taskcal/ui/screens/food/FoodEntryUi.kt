package inc.dimasic.taskcal.ui.screens.food

import inc.dimasic.taskcal.domain.model.MealType

/**
 * One logged item as the Food screen shows it: what it was, and its macros already written out.
 *
 * [id] and the raw figures are carried through untouched because they are what the row's own
 * actions need — the id identifies the row to delete, and the numbers prefill the edit dialog,
 * which cannot read them back out of [macrosLabel]. [mealType] stays a domain value for the same
 * reason: the dialog selects from it, and a parallel UI enum would be the same four cases under
 * another name.
 */
data class FoodEntryUi(
    val id: String,
    val name: String,
    /** Pre-formatted, e.g. "420 kcal · 28P 45C 14F". */
    val macrosLabel: String,
    val mealType: MealType,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
)
