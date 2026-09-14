package inc.dimasic.taskcal.ui.screens.uiViews

/** One macro's column in the breakdown row: amount already worded, progress already measured. */
data class MacroProgressUi(
    val label: String,
    /** Pre-worded, e.g. "20/140g". */
    val amountLabel: String,
    val fraction: Float,
    val percent: Int,
)
