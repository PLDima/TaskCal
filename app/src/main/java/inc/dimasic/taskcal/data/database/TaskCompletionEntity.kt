package inc.dimasic.taskcal.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * How a [inc.dimasic.taskcal.domain.model.TaskCompletion] is stored. Deleting a task cascades to
 * its completion receipts.
 *
 * [occurrenceDate] and [completedAt] are ISO-8601 `LocalDateTime` strings.
 */
@Entity(
    tableName = "task_completions",
    indices = [Index("taskId")],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TaskCompletionEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val occurrenceDate: String,
    val completedAt: String,
)
