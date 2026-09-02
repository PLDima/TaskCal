package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.model.Task
import inc.dimasic.taskcal.domain.model.TaskCompletion
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stores tasks and the completion receipts of their repeating occurrences. Deriving occurrences
 * from a task's recurrence rule is business logic and lives in
 * [inc.dimasic.taskcal.domain.usecase.TasksUseCase] instead.
 */
@Singleton
class TasksRepository @Inject constructor() {

    private val _tasks = MutableStateFlow(mockTasks())
    private val _completions = MutableStateFlow(mockCompletions(_tasks.value))

    val tasks: Flow<List<Task>> = _tasks.asStateFlow()
    val completions: Flow<List<TaskCompletion>> = _completions.asStateFlow()

    fun addTask(
        title: String,
        description: String,
        priority: Priority,
        recurrenceType: RecurrenceType,
        assignedDateTime: LocalDateTime?,
    ) {
        _tasks.value = _tasks.value + Task(
            title = title,
            description = description,
            priority = priority,
            recurrenceType = recurrenceType,
            repeatDayOfWeek = assignedDateTime?.dayOfWeek.takeIf { recurrenceType == RecurrenceType.WEEKLY },
            repeatDayOfMonth = assignedDateTime?.dayOfMonth.takeIf { recurrenceType == RecurrenceType.MONTHLY },
            assignedDateTime = assignedDateTime,
        )
    }

    /**
     * Marks one occurrence done or not done. A repeating task gains or loses a [TaskCompletion];
     * anything else just flips [Task.isCompleted], since it has only the one instance.
     */
    fun setCompleted(taskId: String, occurrenceDate: LocalDateTime?, completed: Boolean) {
        val task = _tasks.value.find { it.id == taskId } ?: return

        if (!task.isRepeating || occurrenceDate == null) {
            _tasks.value = _tasks.value.map {
                if (it.id == taskId) it.copy(isCompleted = completed) else it
            }
            return
        }

        _completions.value = if (completed) {
            _completions.value + TaskCompletion(
                taskId = taskId,
                occurrenceDate = occurrenceDate,
                completedAt = LocalDateTime.now(),
            )
        } else {
            _completions.value.filterNot { it.taskId == taskId && it.occurrenceDate == occurrenceDate }
        }
    }

    private fun mockTasks(): List<Task> {
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return listOf(
            Task(
                title = "Work shift",
                description = "Survive with fraps",
                priority = Priority.HIGH,
                recurrenceType = RecurrenceType.DAILY,
                assignedDateTime = monday.minusWeeks(3).atTime(9, 0),
            ),
            Task(
                title = "Clean up inbox",
                description = "Archive old threads",
                priority = Priority.LOW,
                recurrenceType = RecurrenceType.DAILY,
                assignedDateTime = monday.minusWeeks(2).atTime(18, 0),
            ),
            Task(
                title = "Submit timesheet",
                description = "Log hours for this week",
                priority = Priority.MEDIUM,
                recurrenceType = RecurrenceType.WEEKLY,
                repeatDayOfWeek = DayOfWeek.FRIDAY,
                assignedDateTime = monday.minusWeeks(4).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY)).atTime(17, 0),
            ),
            Task(
                title = "Buy groceries",
                description = "Milk, eggs, bread, coffee",
                priority = Priority.LOW,
                recurrenceType = RecurrenceType.WEEKLY,
                repeatDayOfWeek = DayOfWeek.SATURDAY,
                assignedDateTime = monday.minusWeeks(2).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).atTime(11, 0),
            ),
            Task(
                title = "Pay utility bill",
                description = "Electricity bill for last month",
                priority = Priority.MEDIUM,
                recurrenceType = RecurrenceType.MONTHLY,
                repeatDayOfMonth = 28,
                assignedDateTime = today.minusMonths(4).withDayOfMonth(28).atTime(12, 0),
            ),
            // One-off tasks, still upcoming
            Task(
                title = "Prepare sprint demo",
                description = "Put together slides for Friday's demo",
                priority = Priority.HIGH,
                recurrenceType = RecurrenceType.NONE,
                assignedDateTime = monday.plusDays(4).atTime(14, 0),
            ),
            Task(
                title = "Review PR #482",
                description = "Check the new auth flow changes",
                priority = Priority.HIGH,
                recurrenceType = RecurrenceType.NONE,
                assignedDateTime = today.plusDays(1).atTime(11, 0),
            ),
            Task(
                title = "Call mom",
                description = "Catch up before the weekend",
                priority = Priority.MEDIUM,
                recurrenceType = RecurrenceType.NONE,
                assignedDateTime = today.plusDays(2).atTime(19, 0),
            ),
            Task(
                title = "Schedule dentist appointment",
                description = "Call clinic before Friday",
                priority = Priority.MEDIUM,
                recurrenceType = RecurrenceType.NONE,
                assignedDateTime = today.atTime(15, 30),
            ),
            // One-off that came and went without being done
            Task(
                title = "Renew passport",
                description = "Appointment slot closes soon",
                priority = Priority.HIGH,
                recurrenceType = RecurrenceType.NONE,
                assignedDateTime = monday.minusDays(2).atTime(9, 0),
            ),
            // General tasks with no date: never overdue, always last in the list
            Task(
                title = "Backup laptop",
                description = "Run the weekly backup before the weekend",
                priority = Priority.LOW,
                recurrenceType = RecurrenceType.NONE,
            ),
            Task(
                title = "Return library books",
                description = "Avoid the late fee next time",
                priority = Priority.LOW,
                recurrenceType = RecurrenceType.NONE,
                isCompleted = true,
            ),
        )
    }

    /**
     * Completion history for the repeating fixtures, so the statistics screens have something to
     * chart. "Clean up inbox" is deliberately completed late every time, which is what keeps the
     * "done, but still missed" path visible.
     */
    private fun mockCompletions(tasks: List<Task>): List<TaskCompletion> {
        val today = LocalDate.now()

        fun completionsFor(title: String, days: List<LocalDate>, lateBy: Long = 0): List<TaskCompletion> {
            val task = tasks.find { it.title == title } ?: return emptyList()
            val time = task.assignedDateTime?.toLocalTime() ?: return emptyList()
            return days.map { date ->
                val occurrence = date.atTime(time)
                TaskCompletion(
                    taskId = task.id,
                    occurrenceDate = occurrence,
                    completedAt = occurrence.plusDays(lateBy).minusMinutes(if (lateBy == 0L) 45 else 0),
                )
            }
        }

        val pastFortnight = (1..14L).map { today.minusDays(it) }
        return completionsFor("Work shift", pastFortnight.filter { it.dayOfWeek != DayOfWeek.SUNDAY }) +
            completionsFor("Clean up inbox", pastFortnight.filter { it.dayOfWeek.value % 2 == 0 }, lateBy = 2) +
            completionsFor("Submit timesheet", (1..4L).map { today.minusWeeks(it).with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY)) }) +
            completionsFor("Buy groceries", (1..2L).map { today.minusWeeks(it).with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)) }) +
            completionsFor("Pay utility bill", (1..3L).map { today.minusMonths(it).withDayOfMonth(28) })
    }
}
