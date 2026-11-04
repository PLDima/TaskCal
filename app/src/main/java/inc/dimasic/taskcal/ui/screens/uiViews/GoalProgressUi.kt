package inc.dimasic.taskcal.ui.screens.uiViews

/**
 * One value measured against its goal, as a ring or bar renders it. The measuring is done once,
 * in the ViewModel, through the domain's goal rules — the composable only draws what's here.
 */
data class GoalProgressUi(
    val value: Int,
    val goal: Int,
    /** How much of the bar to fill, 0 to 1. */
    val fraction: Float,
    /** May exceed 100 once the goal is passed. */
    val percent: Int,
    val isOver: Boolean,
    /** Pre-worded, e.g. "935 left" or "120 kcal over". */
    val statusLabel: String,
)
