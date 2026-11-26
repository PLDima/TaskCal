package inc.dimasic.taskcal.data.remote.model

import com.squareup.moshi.JsonClass
import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Wire form of a task. The server fills in the same defaults the app uses for everything but
 * [title] and [priority], and [id] may be left out of a request because it is part of the URL.
 */
@JsonClass(generateAdapter = true)
data class TaskDto(
    val id: String? = null,
    val title: String,
    val description: String = "",
    val priority: Priority,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val repeatDayOfWeek: DayOfWeek? = null,
    val repeatDayOfMonth: Int? = null,
    val assignedDateTime: LocalDateTime? = null,
    val hasDueTime: Boolean = false,
    val isCompleted: Boolean = false,
)