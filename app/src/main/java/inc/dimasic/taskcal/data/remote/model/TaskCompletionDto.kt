package inc.dimasic.taskcal.data.remote.model

import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

/** Wire form of a completion receipt. [id] and [taskId] may be left out of a request: both are in the URL. */
@JsonClass(generateAdapter = true)
data class TaskCompletionDto(
    val id: String? = null,
    val taskId: String? = null,
    val occurrenceDate: LocalDateTime,
    val completedAt: LocalDateTime,
)