package inc.dimasic.taskcal.domain.usecase.model

import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.TaskState
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * One occurrence of a task, flattened for a task list. A repeating task has no stored instances,
 * so [taskId] plus [occurrenceDate] is what identifies the row.
 */
data class TaskListItem(
    val taskId: String,
    /** Which occurrence this row stands for; null for a task with no date. */
    val occurrenceDate: LocalDateTime?,
    val title: String,
    val description: String,
    val priority: Priority,
    val state: TaskState,
) {
    val date: LocalDate? get() = occurrenceDate?.toLocalDate()
}
