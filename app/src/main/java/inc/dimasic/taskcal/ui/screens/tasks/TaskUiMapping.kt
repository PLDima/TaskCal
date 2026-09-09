package inc.dimasic.taskcal.ui.screens.tasks

import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.TaskState
import inc.dimasic.taskcal.domain.usecase.model.TaskListItem
import inc.dimasic.taskcal.domain.usecase.model.TaskPriorityGroup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val dateLabelFormatter = DateTimeFormatter.ofPattern("MMM d")

/** Shared mapping from the task domain models onto what the Tasks and Today screens render. */

internal fun TaskListItem.toUi() = TaskListItemUi(
    taskId = taskId,
    occurrenceDate = occurrenceDate,
    title = title,
    description = description,
    dateLabel = date.toRelativeLabel(),
    isDone = state == TaskState.DONE,
    priority = priority,
    recurrenceType = recurrenceType,
)

internal fun TaskPriorityGroup.toUi() = TaskSectionUi(
    title = priority.sectionTitle,
    tasks = tasks.map { it.toUi() },
)

private val Priority.sectionTitle: String
    get() = "$label priority"

/** Dates close to today read better named than dated. */
private fun LocalDate?.toRelativeLabel(): String {
    if (this == null) return "Not specified"
    return when (ChronoUnit.DAYS.between(LocalDate.now(), this)) {
        0L -> "Today"
        1L -> "Tomorrow"
        else -> format(dateLabelFormatter)
    }
}
