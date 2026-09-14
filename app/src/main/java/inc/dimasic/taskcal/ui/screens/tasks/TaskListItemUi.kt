package inc.dimasic.taskcal.ui.screens.tasks

import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import java.time.LocalDateTime

/**
 * One task row as the screens show it: when it's due already worded, and its state flattened to
 * the single question the row asks — is it ticked.
 *
 * [taskId] and [occurrenceDate] are carried through untouched because they are what identifies
 * the occurrence when the checkbox is tapped. [priority] and [recurrenceType] stay domain values:
 * the row selects behaviour straight off them (the priority dot's colour, whether the delete
 * dialog warns about every occurrence), and parallel UI enums would just be the same cases under
 * another name.
 */
data class TaskListItemUi(
    val taskId: String,
    val occurrenceDate: LocalDateTime?,
    val title: String,
    val description: String,
    /** Pre-worded, e.g. "Today", "Tomorrow · 14:30", "Sep 12" or "Not specified". */
    val dateLabel: String,
    val isDone: Boolean,
    val priority: Priority,
    val recurrenceType: RecurrenceType,
)
