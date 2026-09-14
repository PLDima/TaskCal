package inc.dimasic.taskcal.ui.screens.tasks

import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import java.time.LocalDate
import java.time.LocalTime

/** What the task editor's fields hold — a new task's defaults, or an existing task's values. */
data class TaskFormUi(
    val title: String,
    val description: String,
    val priority: Priority,
    val recurrenceType: RecurrenceType,
    val date: LocalDate?,
    /** Null when no time of day was chosen; only meaningful alongside [date]. */
    val time: LocalTime?,
) {
    companion object {
        val Empty = TaskFormUi(
            title = "",
            description = "",
            priority = Priority.MEDIUM,
            recurrenceType = RecurrenceType.NONE,
            date = null,
            time = null,
        )
    }
}
