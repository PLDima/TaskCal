package inc.dimasic.taskcal.domain.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * A receipt that one occurrence of a repeating [Task] was completed.
 *
 * Only repeating tasks get these; a non-repeating task records its state in [Task.isCompleted].
 * An expanded occurrence with no matching completion is still open — and once its
 * [occurrenceDate] has passed, missed. That's what makes missed repeats derivable instead of
 * having to be stored.
 */
data class TaskCompletion(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    /** The occurrence this completes, matching an expanded date of the owning task. */
    val occurrenceDate: LocalDateTime,
    /** When it was actually completed; later than [occurrenceDate] means it was done late. */
    val completedAt: LocalDateTime,
)
