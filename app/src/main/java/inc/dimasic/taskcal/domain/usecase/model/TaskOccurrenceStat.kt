package inc.dimasic.taskcal.domain.usecase.model

import inc.dimasic.taskcal.domain.model.TaskState
import java.time.LocalDate
import java.time.LocalDateTime

/** Projection of one occurrence that's all the tasks-statistics calculations need. */
data class TaskOccurrenceStat(
    /** Null for a task with no date, which therefore has no deadline. */
    val occurrenceDate: LocalDateTime?,
    /** Whether [occurrenceDate]'s time of day is a real deadline or a placeholder — see [inc.dimasic.taskcal.domain.model.Task.hasDueTime]. */
    val hasDueTime: Boolean,
    val state: TaskState,
    val completedAt: LocalDateTime?,
) {
    val date: LocalDate? get() = occurrenceDate?.toLocalDate()
}
