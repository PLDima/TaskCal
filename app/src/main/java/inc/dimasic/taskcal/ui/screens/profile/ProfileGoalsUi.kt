package inc.dimasic.taskcal.ui.screens.profile

/**
 * The goals as the Profile rows and dialogs show them. The weight goal is the one measured in a
 * unit the user can change, so it comes both worded and as the number to put in the input field.
 */
data class ProfileGoalsUi(
    val caloriesGoal: Int,
    val stepsGoal: Int,
    val proteinGoal: Double,
    val carbsGoal: Double,
    val fatGoal: Double,
    /** e.g. "68.0 kg" or "149.9 lb". */
    val weightGoalLabel: String,
    /** The goal in the chosen unit, for the dialog to start from. */
    val weightGoalInput: Double,
    val weightUnit: String,
)
