package inc.dimasic.taskcal.model.domain

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.UUID

/**
 * A task and everything about it: what it is, when it happens, how it repeats, and — for
 * non-repeating tasks — whether it's done.
 *
 * Repeating tasks don't store their individual instances. Those are expanded from
 * [recurrenceType] on demand, and completing one writes a [TaskCompletion] instead of touching
 * [isCompleted].
 */
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val priority: Priority,
    val recurrenceType: RecurrenceType,
    /** Which weekday a [RecurrenceType.WEEKLY] task lands on; falls back to [assignedDateTime]. */
    val repeatDayOfWeek: DayOfWeek? = null,
    /** Which day a [RecurrenceType.MONTHLY] task lands on; falls back to [assignedDateTime]. */
    val repeatDayOfMonth: Int? = null,
    /**
     * When the task is due. Null means a general task with no date, which can never be overdue.
     * For a repeating task this is also the start of the series: no occurrence is ever expanded
     * before it.
     */
    val assignedDateTime: LocalDateTime? = null,
    /** Only meaningful for non-repeating tasks; repeating ones use [TaskCompletion]. */
    val isCompleted: Boolean = false,
) {
    val isRepeating: Boolean get() = recurrenceType != RecurrenceType.NONE
}

enum class Priority(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
}

enum class RecurrenceType(val label: String) {
    NONE("None"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
}

/** Display state of a single task occurrence, derived rather than stored. */
enum class TaskState(val label: String) {
    PENDING("Pending"),
    DONE("Done"),
    MISSED("Missed"),
}
