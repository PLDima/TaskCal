package inc.dimasic.taskcal.ui.screens.profile

import inc.dimasic.taskcal.domain.model.Gender
import java.time.LocalDate

/** What the details form starts from, with the height already in the chosen unit. */
data class ProfileDetailsFormUi(
    val name: String,
    val gender: Gender?,
    val dateOfBirth: LocalDate?,
    val height: Int?,
    val heightUnit: String,
)
