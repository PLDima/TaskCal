package inc.dimasic.taskcal.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * How a [inc.dimasic.taskcal.domain.model.Task] is stored. Room-only; the domain model carries
 * no persistence annotations. See [TaskEntityMapping] for the conversion.
 *
 * [assignedDateTime] is an ISO-8601 `LocalDateTime` string; [priority] and [recurrenceType] are
 * enum names.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priority: String,
    val recurrenceType: String,
    val repeatDayOfWeek: Int?,
    val repeatDayOfMonth: Int?,
    val assignedDateTime: String?,
    val isCompleted: Boolean,
)
