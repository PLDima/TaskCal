package inc.dimasic.taskcal.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class TaskOccurrence(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    /** Null means a general task with no specific scheduled date. */
    val date: LocalDate?,
    val scheduledTime: LocalTime?,
    val status: Status,
    /** Moment the occurrence was marked [Status.DONE], null while it isn't. */
    val completedAt: LocalDateTime? = null,
)

enum class Status(val label: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    DONE("Done"),
}
