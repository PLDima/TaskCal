package inc.dimasic.taskcal.ui.screens

/** One meal's block on the Food screen, with its heading and its "add" action already worded. */
data class MealSectionUi(
    val title: String,
    val addLabel: String,
    val entries: List<FoodEntryUi>,
)
