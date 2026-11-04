package inc.dimasic.taskcal.ui.screens.uiViews

/** The Today screen's calories card: two rings and the macro breakdown beneath them. */
data class CaloriesSectionUi(
    /** Consumed against the intake goal. */
    val goal: GoalProgressUi,
    /** Consumed against what activity burned. */
    val activity: GoalProgressUi,
    val macros: List<MacroProgressUi>,
)
