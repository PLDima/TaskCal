package inc.dimasic.taskcal.domain.usecase.model

import inc.dimasic.taskcal.domain.model.Gender
import java.time.LocalDate
import java.time.Period

/** Who the user is, as the profile-details screen edits it. Every field is optional until set. */
data class ProfileDetails(
    val name: String,
    val gender: Gender?,
    val dateOfBirth: LocalDate?,
    val heightCm: Int?,
) {
    /** Whole years since [dateOfBirth]; null when it isn't set or lies in the future. */
    val age: Int? get() = dateOfBirth?.let { Period.between(it, LocalDate.now()).years.takeIf { years -> years >= 0 } }

    companion object {
        val Empty = ProfileDetails(name = "", gender = null, dateOfBirth = null, heightCm = null)
    }
}
