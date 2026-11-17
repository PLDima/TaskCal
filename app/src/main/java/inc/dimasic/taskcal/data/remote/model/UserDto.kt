package inc.dimasic.taskcal.data.remote.model

import com.squareup.moshi.JsonClass

/** The signed-in user, as returned by `POST auth/device` and `GET me`. */
@JsonClass(generateAdapter = true)
data class UserDto(
    val userId: String,
)