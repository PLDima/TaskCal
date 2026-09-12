package inc.dimasic.taskcal.ui.screens.food

/** One meal's block on the Food screen, with its heading already worded. */
data class MealSectionUi(
    val title: String,
    val entries: List<FoodEntryUi>,
)