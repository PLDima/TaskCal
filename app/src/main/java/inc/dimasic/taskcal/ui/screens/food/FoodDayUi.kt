package inc.dimasic.taskcal.ui.screens.food

import inc.dimasic.taskcal.ui.screens.uiViews.MacroProgressUi

/**
 * Everything the Food screen shows for one day, gathered into a single value so it can be rendered
 * once all of its reads have finished rather than piecemeal.
 */
data class FoodDayUi(
    val caloriesUsed: Int,
    val caloriesGoal: Int,
    val macros: List<MacroProgressUi>,
    val mealSections: List<MealSectionUi>,
)
