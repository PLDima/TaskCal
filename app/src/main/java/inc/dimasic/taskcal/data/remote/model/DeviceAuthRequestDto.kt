package inc.dimasic.taskcal.data.remote.model

import com.squareup.moshi.JsonClass

/** Body of `POST auth/device`: registers the device token, or finds the user it already belongs to. */
@JsonClass(generateAdapter = true)
data class DeviceAuthRequestDto(
    val deviceToken: String,
)