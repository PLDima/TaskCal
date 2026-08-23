package inc.dimasic.taskcal.repository

import inc.dimasic.taskcal.model.Priority
import inc.dimasic.taskcal.model.RecurrenceType
import inc.dimasic.taskcal.model.Status
import inc.dimasic.taskcal.model.Task
import inc.dimasic.taskcal.model.TaskOccurrence
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Projection of a [TaskOccurrence] joined with the fields of its owning [Task] that a task list
 * needs to render (name, priority), rather than exposing the raw entities to callers.
 */
data class TaskListItem(
    val occurrenceId: String,
    val title: String,
    val description: String,
    val priority: Priority,
    val status: Status,
    /** Null means a general task with no specific scheduled date. */
    val date: LocalDate?,
)

@Singleton
class TasksRepository @Inject constructor() {

    private val tasks = MutableStateFlow(mockTasks())
    private val occurrences = MutableStateFlow(mockOccurrences(tasks.value))

    /** Upcoming and general (undated) occurrences, closest to today first, general tasks last. */
    val taskListItems: Flow<List<TaskListItem>> =
        combine(tasks, occurrences) { tasks, occurrences ->
            val today = LocalDate.now()
            occurrences
                .filter { it.date == null || it.date >= today }
                .sortedWith(compareBy(nullsLast()) { it.date })
                .mapNotNull { occurrence -> occurrence.toTaskListItem(tasks) }
        }

    fun taskListItemsForDate(date: Flow<LocalDate>): Flow<List<TaskListItem>> =
        combine(tasks, occurrences, date) { tasks, occurrences, selectedDate ->
            occurrences
                .filter { it.date == selectedDate }
                .mapNotNull { occurrence -> occurrence.toTaskListItem(tasks) }
        }

    fun toggleOccurrenceStatus(occurrenceId: String) {
        occurrences.value = occurrences.value.map { occurrence ->
            if (occurrence.id != occurrenceId) {
                occurrence
            } else {
                occurrence.copy(status = if (occurrence.status == Status.DONE) Status.TODO else Status.DONE)
            }
        }
    }

    private fun TaskOccurrence.toTaskListItem(tasks: List<Task>): TaskListItem? {
        val task = tasks.find { it.id == taskId } ?: return null
        return TaskListItem(
            occurrenceId = id,
            title = task.title,
            description = task.description,
            priority = task.priority,
            status = status,
            date = date,
        )
    }

    private fun mockTasks(): List<Task> = listOf(
        Task(
            title = "Finish quarterly report",
            description = "Compile Q2 numbers and send to finance",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Finish quarterly report",
            description = "Compile Q2 numbers and send to finance",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Finish quarterly report",
            description = "Compile Q2 numbers and send to finance",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Finish quarterly report",
            description = "Compile Q2 numbers and send to finance",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Finish quarterly report",
            description = "Compile Q2 numbers and send to finance",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Finish quarterly report",
            description = "Compile Q2 numbers and send to finance",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.NONE,
        ),
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
        return tasks.mapIndexed { index, task ->
            val status = when (index % 4) {
                0 -> Status.DONE
                1 -> Status.IN_PROGRESS
                else -> Status.TODO
            }
            val date = if (index % 6 == 5) null else today.plusDays((index % 5).toLong())
            TaskOccurrence(taskId = task.id, date = date, scheduledTime = null, status = status)
        }
    }
}
