package inc.dimasic.taskcal.domain.usecase.model

import inc.dimasic.taskcal.domain.model.Priority

/** Tasks sharing a priority. Which priorities exist, and in what order, is decided in the domain. */
data class TaskPriorityGroup(
    val priority: Priority,
    val tasks: List<TaskListItem>,
)
