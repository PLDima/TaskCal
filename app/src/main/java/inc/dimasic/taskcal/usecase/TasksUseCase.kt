package inc.dimasic.taskcal.usecase

import inc.dimasic.taskcal.repository.TaskListItem
import inc.dimasic.taskcal.repository.TasksRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class TasksUseCase @Inject constructor(
    private val tasksRepository: TasksRepository,
) {

    fun tasksForDate(date: Flow<LocalDate>): Flow<List<TaskListItem>> =
        tasksRepository.taskListItemsForDate(date)

    fun allTasks(): Flow<List<TaskListItem>> = tasksRepository.taskListItems

    fun toggleTaskCompletion(occurrenceId: String) {
        tasksRepository.toggleOccurrenceStatus(occurrenceId)
    }
}
