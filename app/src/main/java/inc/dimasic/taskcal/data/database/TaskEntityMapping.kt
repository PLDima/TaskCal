package inc.dimasic.taskcal.data.database

import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.model.Task
import inc.dimasic.taskcal.domain.model.TaskCompletion
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Conversion between the domain task models and their stored [TaskEntity] / [TaskCompletionEntity]
 * rows. Pure functions, so they can be round-trip tested without Room.
 */

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    priority = priority.name,
    recurrenceType = recurrenceType.name,
    repeatDayOfWeek = repeatDayOfWeek?.value,
    repeatDayOfMonth = repeatDayOfMonth,
    assignedDateTime = assignedDateTime?.toString(),
    isCompleted = isCompleted,
)

fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    description = description,
    priority = enumValueOf<Priority>(priority),
    recurrenceType = enumValueOf<RecurrenceType>(recurrenceType),
    repeatDayOfWeek = repeatDayOfWeek?.let(DayOfWeek::of),
    repeatDayOfMonth = repeatDayOfMonth,
    assignedDateTime = assignedDateTime?.let(LocalDateTime::parse),
    isCompleted = isCompleted,
)

fun TaskCompletion.toEntity() = TaskCompletionEntity(
    id = id,
    taskId = taskId,
    occurrenceDate = occurrenceDate.toString(),
    completedAt = completedAt.toString(),
)

fun TaskCompletionEntity.toDomain() = TaskCompletion(
    id = id,
    taskId = taskId,
    occurrenceDate = LocalDateTime.parse(occurrenceDate),
    completedAt = LocalDateTime.parse(completedAt),
)
