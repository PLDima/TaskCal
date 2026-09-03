package inc.dimasic.taskcal.ui.screens

import inc.dimasic.taskcal.domain.model.Priority
import java.time.LocalDateTime

/**
 * One task row as the screens show it: when it's due already worded, and its state flattened to
 * the single question the row asks — is it ticked.
 *
 * [taskId] and [occurrenceDate] are carried through untouched because they are what identifies
 * the occurrence when the checkbox is tapped. [priority] stays a domain value: it selects the
 * colour of the dot, and a parallel UI enum would be the same three cases under another name.
 */
data class TaskListItemUi(
    val taskId: String,
    val occurrenceDate: LocalDateTime?,
    val title: String,
    val description: String,
    /** Pre-worded, e.g. "Today", "Tomorrow", "Sep 12" or "Not specified". */
    val dateLabel: String,
    val isDone: Boolean,
    val priority: Priority,
)
