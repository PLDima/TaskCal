package inc.dimasic.taskcal.ui.screens.profile

/** The card at the top of Profile: the user's name, and what little is known about them, worded. */
data class ProfileCardUi(
    val name: String,
    /** e.g. "31 years · 175 cm", or a prompt to fill the details in when nothing is set. */
    val subtitle: String,
)
