package inc.dimasic.taskcal.repository

import inc.dimasic.taskcal.model.ui.DateRange
import inc.dimasic.taskcal.model.domain.Priority
import inc.dimasic.taskcal.model.domain.RecurrenceType
import inc.dimasic.taskcal.model.domain.Status
import inc.dimasic.taskcal.model.domain.Task
import inc.dimasic.taskcal.model.domain.TaskOccurrence
import inc.dimasic.taskcal.model.ui.TasksPeriod
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
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

/** Projection of a [TaskOccurrence] that's all the tasks-statistics calculations need. */
data class TaskOccurrenceStat(
    val date: LocalDate?,
    val scheduledTime: LocalTime?,
    val status: Status,
    val completedAt: LocalDateTime?,
)

data class PeriodTaskOccurrences(
    /** Null for [TasksPeriod.ALL_TIME], which has no bounds. */
    val dateRange: DateRange?,
    val occurrences: List<TaskOccurrenceStat>,
) {
    companion object {
        val Empty = PeriodTaskOccurrences(dateRange = null, occurrences = emptyList())
    }
}

private const val ORIGINAL_TASK_COUNT = 12

private data class MockOccurrenceSpec(
    val date: LocalDate,
    val status: Status,
    val scheduledTime: LocalTime? = null,
    /** Moment the occurrence was completed; null while [status] isn't DONE. */
    val completedAt: LocalDateTime? = null,
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
                .filter { it.scheduledDate == null || it.scheduledDate >= today }
                .sortedWith(compareBy(nullsLast()) { it.scheduledDate })
                .mapNotNull { occurrence -> occurrence.toTaskListItem(tasks) }
        }

    fun taskListItemsForDate(date: Flow<LocalDate>): Flow<List<TaskListItem>> =
        combine(tasks, occurrences, date) { tasks, occurrences, selectedDate ->
            occurrences
                .filter { it.scheduledDate == selectedDate }
                .mapNotNull { occurrence -> occurrence.toTaskListItem(tasks) }
        }

    fun occurrencesForPeriod(period: Flow<TasksPeriod>, offset: Flow<Int>): Flow<PeriodTaskOccurrences> =
        combine(occurrences, period, offset) { allOccurrences, selectedPeriod, selectedOffset ->
            val range = dateRangeFor(selectedPeriod, selectedOffset)
            val filtered = if (range == null) {
                allOccurrences
            } else {
                allOccurrences.filter { it.scheduledDate != null && range.contains(it.scheduledDate) }
            }
            PeriodTaskOccurrences(
                dateRange = range,
                occurrences = filtered.map {
                    TaskOccurrenceStat(
                        date = it.scheduledDate,
                        scheduledTime = it.scheduledTime,
                        status = it.status,
                        completedAt = it.completedAt,
                    )
                },
            )
        }

    private fun dateRangeFor(period: TasksPeriod, offset: Int): DateRange? {
        val today = LocalDate.now()
        return when (period) {
            TasksPeriod.WEEK -> {
                val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(offset.toLong())
                DateRange(start = monday, end = monday.plusDays(6))
            }
            TasksPeriod.MONTH -> {
                val firstOfMonth = today.with(TemporalAdjusters.firstDayOfMonth()).plusMonths(offset.toLong())
                DateRange(start = firstOfMonth, end = firstOfMonth.with(TemporalAdjusters.lastDayOfMonth()))
            }
            TasksPeriod.ALL_TIME -> null
        }
    }

    fun addTask(
        title: String,
        description: String,
        priority: Priority,
        recurrenceType: RecurrenceType,
        scheduledDate: LocalDate?,
    ) {
        val task = Task(
            title = title,
            description = description,
            priority = priority,
            recurrenceType = recurrenceType,
        )
        tasks.value = tasks.value + task
        occurrences.value = occurrences.value + TaskOccurrence(
            taskId = task.id,
            scheduledDate = scheduledDate,
            scheduledTime = null,
            status = Status.TODO,
        )
    }

    fun toggleOccurrenceStatus(occurrenceId: String) {
        occurrences.value = occurrences.value.map { occurrence ->
            if (occurrence.id != occurrenceId) {
                occurrence
            } else if (occurrence.status == Status.DONE) {
                occurrence.copy(status = Status.TODO, completedAt = null)
            } else {
                occurrence.copy(status = Status.DONE, completedAt = LocalDateTime.now())
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
            date = scheduledDate,
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
        // Additional tasks scheduled this week
        Task(
            title = "Prepare sprint demo",
            description = "Put together slides for Friday's demo",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Water the plants",
            description = "Office plants need watering twice a week",
            priority = Priority.LOW,
            recurrenceType = RecurrenceType.DAILY,
        ),
        Task(
            title = "Call mom",
            description = "Catch up before the weekend",
            priority = Priority.MEDIUM,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Submit timesheet",
            description = "Log hours for this week",
            priority = Priority.MEDIUM,
            recurrenceType = RecurrenceType.WEEKLY,
        ),
        Task(
            title = "Backup laptop",
            description = "Run the weekly backup before the weekend",
            priority = Priority.LOW,
            recurrenceType = RecurrenceType.NONE,
        ),
        // Expired tasks that ended up completed late
        Task(
            title = "Renew passport",
            description = "Appointment slot closes soon",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Submit expense report",
            description = "March travel expenses",
            priority = Priority.MEDIUM,
            recurrenceType = RecurrenceType.NONE,
        ),
        Task(
            title = "Pay utility bill",
            description = "Electricity bill for last month",
            priority = Priority.MEDIUM,
            recurrenceType = RecurrenceType.MONTHLY,
        ),
        Task(
            title = "Return library books",
            description = "Avoid the late fee next time",
            priority = Priority.LOW,
            recurrenceType = RecurrenceType.NONE,
        ),
    )

    private fun mockOccurrences(tasks: List<Task>): List<TaskOccurrence> {
        val today = LocalDate.now()
        val originalOccurrences = tasks.take(ORIGINAL_TASK_COUNT).mapIndexed { index, task ->
            val status = when (index % 4) {
                0 -> Status.DONE
                1 -> Status.IN_PROGRESS
                else -> Status.TODO
            }
            val date = if (index % 6 == 5) null else today.plusDays((index % 5).toLong())
            // On-time when index is a multiple of 8, otherwise completed a few days late.
            val completedAt = if (status == Status.DONE) {
                val completedDate = if (index % 8 == 0) date ?: today else (date ?: today).plusDays(3)
                completedDate.atTime(12, 0)
            } else {
                null
            }
            TaskOccurrence(taskId = task.id, scheduledDate = date, scheduledTime = null, status = status, completedAt = completedAt)
        }

        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val extraSpecs = listOf(
            // More tasks scheduled this week
            MockOccurrenceSpec(
                date = monday.plusDays(1),
                status = Status.DONE,
                scheduledTime = LocalTime.of(17, 0),
                completedAt = monday.plusDays(1).atTime(15, 30), // before the 5pm deadline, on time
            ),
            MockOccurrenceSpec(date = monday.plusDays(2), status = Status.TODO, scheduledTime = LocalTime.of(17, 0)), // Wed
            MockOccurrenceSpec(date = monday.plusDays(3), status = Status.IN_PROGRESS, scheduledTime = LocalTime.of(12, 0)), // Thu
            MockOccurrenceSpec(
                date = monday.plusDays(4),
                status = Status.DONE,
                scheduledTime = LocalTime.of(18, 0),
                completedAt = monday.plusDays(4).atTime(16, 45), // before the 6pm deadline, on time
            ),
            MockOccurrenceSpec(date = monday.plusDays(5), status = Status.TODO), // Sat
            // Expired tasks: most were never finished, one was completed well after its deadline
            MockOccurrenceSpec(date = monday.minusDays(1), status = Status.TODO, scheduledTime = LocalTime.of(9, 0)),
            MockOccurrenceSpec(
                date = monday.minusDays(1),
                status = Status.DONE,
                scheduledTime = LocalTime.of(9, 0),
                completedAt = monday.minusDays(1).plusDays(5).atTime(10, 0), // 5 days after the 9am deadline
            ),
            MockOccurrenceSpec(date = monday.minusDays(2), status = Status.TODO, scheduledTime = LocalTime.of(14, 0)),
            MockOccurrenceSpec(date = monday.minusDays(3), status = Status.TODO),
        )
        val extraOccurrences = tasks.drop(ORIGINAL_TASK_COUNT).zip(extraSpecs) { task, spec ->
            TaskOccurrence(
                taskId = task.id,
                scheduledDate = spec.date,
                scheduledTime = spec.scheduledTime,
                status = spec.status,
                completedAt = spec.completedAt,
            )
        }

        return originalOccurrences + extraOccurrences
    }
}
