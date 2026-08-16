package inc.dimasic.taskcal.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class TaskOccurrence(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val date: LocalDate,
    val scheduledTime: LocalTime?,
    val status: Status,
)

enum class Status(val label: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    DONE("Done"),
}
