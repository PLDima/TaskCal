package inc.dimasic.taskcal.ui.screens

import inc.dimasic.taskcal.domain.usecase.model.CaloriesSummary

/**
 * Everything the Food screen shows for one day, gathered into a single value so it can be rendered
 * once all of its reads have finished rather than piecemeal.
 *
 * [calories] is passed through as-is: it is already field-for-field what the screen renders.
 */
data class FoodDayUi(
    val calories: CaloriesSummary,
    val mealSections: List<MealSectionUi>,
)
