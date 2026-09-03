package inc.dimasic.taskcal.ui.screens

/** One logged item as the Food screen shows it: what it was, and its macros already written out. */
data class FoodEntryUi(
    val name: String,
    /** Pre-formatted, e.g. "420 kcal · 28P 45C 14F". */
    val macrosLabel: String,
)
