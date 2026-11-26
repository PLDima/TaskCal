package inc.dimasic.taskcal.data.remote.model

import inc.dimasic.taskcal.domain.model.Task

/** Conversion between [Task] and its wire form [TaskDto]. */

fun Task.toDto() = TaskDto(
    id = id,
    title = title,
    description = description,
    priority = priority,
    recurrenceType = recurrenceType,
    repeatDayOfWeek = repeatDayOfWeek,
    repeatDayOfMonth = repeatDayOfMonth,
    assignedDateTime = assignedDateTime,
    hasDueTime = hasDueTime,
    isCompleted = isCompleted,
)

fun TaskDto.toDomain() = Task(
    id = requireNotNull(id) { "The server returned a task without an id" },
    title = title,
    description = description,
    priority = priority,
    recurrenceType = recurrenceType,
    repeatDayOfWeek = repeatDayOfWeek,
    repeatDayOfMonth = repeatDayOfMonth,
    assignedDateTime = assignedDateTime,
    hasDueTime = hasDueTime,
    isCompleted = isCompleted,
)