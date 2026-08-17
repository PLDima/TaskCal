package inc.dimasic.taskcal.usecase

import inc.dimasic.taskcal.model.Priority
import inc.dimasic.taskcal.model.RecurrenceType
import inc.dimasic.taskcal.model.Status
import inc.dimasic.taskcal.model.Task
import inc.dimasic.taskcal.model.TaskOccurrence
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

data class TaskListItem(
    val occurrenceId: String,
    val title: String,
    val description: String,
    val priority: Priority,
    val status: Status,
)

class TasksUseCase @Inject constructor() {

    private val tasks = MutableStateFlow(mockTasks())
    private val occurrences = MutableStateFlow(mockOccurrences(tasks.value))

    fun tasksForDate(date: Flow<LocalDate>): Flow<List<TaskListItem>> =
        combine(tasks, occurrences, date) { tasks, occurrences, selectedDate ->
            occurrences
                .filter { it.date == selectedDate }
                .mapNotNull { occurrence -> occurrence.toTaskListItem(tasks) }
        }

    fun allTasks(): Flow<List<TaskListItem>> =
        combine(tasks, occurrences) { tasks, occurrences ->
            occurrences.mapNotNull { occurrence -> occurrence.toTaskListItem(tasks) }
        }

    private fun TaskOccurrence.toTaskListItem(tasks: List<Task>): TaskListItem? {
        val task = tasks.find { it.id == taskId } ?: return null
        return TaskListItem(
            occurrenceId = id,
            title = task.title,
            description = task.description,
            priority = task.priority,
            status = status,
        )
    }

    fun toggleTaskCompletion(occurrenceId: String) {
        occurrences.value = occurrences.value.map { occurrence ->
            if (occurrence.id != occurrenceId) {
                occurrence
            } else {
                occurrence.copy(status = if (occurrence.status == Status.DONE) Status.TODO else Status.DONE)
            }
        }
    }

    private fun mockTasks(): List<Task> = listOf(
        Task(
            title = "Finish quarterly report",
            description = "Compile Q2 numbers and send to finance",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Review PR #482",
            description = "Check the new auth flow changes",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Clean up inbox",
            description = "Archive old threads",
            priority = Priority.LOW,
            recurrenceType = RecurrenceType.DAILY,
        ),
        Task(
            title = "Schedule dentist appointment",
            description = "Call clinic before Friday",
            priority = Priority.MEDIUM,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Buy groceries",
            description = "Milk, eggs, bread, coffee",
            priority = Priority.LOW,
            recurrenceType = RecurrenceType.WEEKLY,
        ),
        Task(
            title = "Work shift",
            description = "Survive with fraps",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.DAILY,
        ),
    )

    private fun mockOccurrences(tasks: List<Task>): List<TaskOccurrence> {
        val today = LocalDate.now()
        return listOf(
            TaskOccurrence(taskId = tasks[0].id, date = today, scheduledTime = null, status = Status.TODO),
            TaskOccurrence(taskId = tasks[1].id, date = today, scheduledTime = null, status = Status.IN_PROGRESS),
            TaskOccurrence(taskId = tasks[2].id, date = today, scheduledTime = null, status = Status.DONE),
            TaskOccurrence(taskId = tasks[3].id, date = today.plusDays(1), scheduledTime = null, status = Status.TODO),
            TaskOccurrence(taskId = tasks[4].id, date = today, scheduledTime = null, status = Status.TODO),
            TaskOccurrence(taskId = tasks[5].id, date = today, scheduledTime = null, status = Status.TODO),
        )
    }
}
