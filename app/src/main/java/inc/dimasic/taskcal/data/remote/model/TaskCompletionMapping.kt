package inc.dimasic.taskcal.data.remote.model

import inc.dimasic.taskcal.domain.model.TaskCompletion

/** Conversion between [TaskCompletion] and its wire form [TaskCompletionDto]. */

fun TaskCompletion.toDto() = TaskCompletionDto(
    id = id,
    taskId = taskId,
    occurrenceDate = occurrenceDate,
    completedAt = completedAt,
)

fun TaskCompletionDto.toDomain() = TaskCompletion(
    id = requireNotNull(id) { "The server returned a completion without an id" },
    taskId = requireNotNull(taskId) { "The server returned a completion without a task id" },
    occurrenceDate = occurrenceDate,
    completedAt = completedAt,
)