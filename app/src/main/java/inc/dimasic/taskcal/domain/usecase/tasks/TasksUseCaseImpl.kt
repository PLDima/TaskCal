package inc.dimasic.taskcal.domain.usecase.tasks

import inc.dimasic.taskcal.data.repository.TasksRepository
import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.model.Task
import inc.dimasic.taskcal.domain.model.TaskCompletion
import inc.dimasic.taskcal.domain.model.TaskState
import inc.dimasic.taskcal.domain.model.TasksPeriod
import inc.dimasic.taskcal.domain.usecase.dateRangeAt
import inc.dimasic.taskcal.domain.usecase.model.ChartBucket
import inc.dimasic.taskcal.domain.usecase.model.ChartPoint
import inc.dimasic.taskcal.domain.usecase.model.PeriodTaskOccurrences
import inc.dimasic.taskcal.domain.usecase.model.TaskListItem
import inc.dimasic.taskcal.domain.usecase.model.TaskOccurrenceStat
import inc.dimasic.taskcal.domain.usecase.model.TaskPriorityGroup
import inc.dimasic.taskcal.domain.usecase.model.TaskStatusBreakdown
import inc.dimasic.taskcal.domain.usecase.model.TasksSummary
import inc.dimasic.taskcal.domain.usecase.monthChartBuckets
import inc.dimasic.taskcal.domain.usecase.nextOccurrenceFrom
import inc.dimasic.taskcal.domain.usecase.occurrencesIn
import inc.dimasic.taskcal.domain.usecase.weekChartBuckets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private const val ALL_TIME_CHART_MONTHS = 6
private val monthLabelFormatter = DateTimeFormatter.ofPattern("MMM")

/** Most urgent first, which is the order tasks are grouped and listed in. */
private val PRIORITY_ORDER = listOf(Priority.HIGH, Priority.MEDIUM, Priority.LOW)

/**
 * The time of day a task is stamped with when the user didn't choose one. It is a placeholder,
 * not a deadline — such a task is due by the end of its date — and [Task.hasDueTime] is what
 * tells [isPast] which way to read it.
 */
private val DEFAULT_TASK_TIME: LocalTime = LocalTime.of(9, 0)

class TasksUseCaseImpl @Inject constructor(
    private val tasksRepository: TasksRepository,
) : TasksUseCase {

    /**
     * Occurrences falling on [date], most important first. Tasks with no date have no deadline,
     * so they always apply. Completed occurrences are dropped: the Today list shows only what is
     * still outstanding.
     */
    override fun tasksForDate(date: Flow<LocalDate>): Flow<List<TaskListItem>> =
        combine(tasksRepository.tasks, tasksRepository.completions, date) { tasks, completions, selectedDate ->
            val now = LocalDateTime.now()
            tasks.mapNotNull { task ->
                if (task.assignedDateTime == null) {
                    task.toListItem(occurrence = null, completions = completions, now = now)
                } else {
                    task.occurrencesIn(DateRange(selectedDate, selectedDate))
                        .firstOrNull()
                        ?.let { task.toListItem(it, completions, now) }
                }
            }.filterNot { it.state == TaskState.DONE }.byImportance()
        }

    /**
     * One row per task, closest first. A repeating task is represented only by its next
     * occurrence rather than projected out to some arbitrary horizon, and undated tasks sort last.
     */
    override fun allTasks(): Flow<List<TaskListItem>> =
        combine(tasksRepository.tasks, tasksRepository.completions) { tasks, completions ->
            val today = LocalDate.now()
            val now = LocalDateTime.now()
            tasks
                .mapNotNull { task ->
                    if (task.assignedDateTime == null) {
                        task.toListItem(occurrence = null, completions = completions, now = now)
                    } else {
                        task.nextOccurrenceFrom(today)?.let { task.toListItem(it, completions, now) }
                    }
                }
                .sortedWith(compareBy(nullsLast()) { it.occurrenceDate })
        }

    override fun occurrencesForPeriod(period: Flow<TasksPeriod>, offset: Flow<Int>): Flow<PeriodTaskOccurrences> =
        combine(
            tasksRepository.tasks,
            tasksRepository.completions,
            period,
            offset,
        ) { tasks, completions, selectedPeriod, selectedOffset ->
            val range = selectedPeriod.dateRangeAt(selectedOffset)
            val expansionRange = range ?: allTimeRange()
            val now = LocalDateTime.now()

            val stats = tasks.flatMap { task ->
                when {
                    // Undated tasks have no deadline, so they only make sense in an unbounded period.
                    task.assignedDateTime == null ->
                        if (range == null) listOf(task.toStat(null, completions, now)) else emptyList()
                    else -> task.occurrencesIn(expansionRange).map { task.toStat(it, completions, now) }
                }
            }
            PeriodTaskOccurrences(dateRange = range, occurrences = stats)
        }

    /**
     * Pure derivations of an already-fetched [PeriodTaskOccurrences], for a screen that keeps one
     * shared, stateful subscription to [occurrencesForPeriod] and maps this off it, rather than
     * each stat re-running the underlying [combine] and Room queries on its own.
     */
    override fun summaryOf(occurrences: PeriodTaskOccurrences): TasksSummary = occurrences.toSummary()

    override fun taskStatusOf(occurrences: PeriodTaskOccurrences): TaskStatusBreakdown = occurrences.toTaskStatusBreakdown()

    override fun onTimeRateChartOf(occurrences: PeriodTaskOccurrences, period: TasksPeriod): List<ChartPoint> =
        when (period) {
            TasksPeriod.WEEK -> weekChartBuckets(occurrences)
            TasksPeriod.MONTH -> monthChartBuckets(occurrences)
            TasksPeriod.ALL_TIME -> allTimeChartBuckets(occurrences)
        }

    override suspend fun setTaskCompleted(taskId: String, occurrenceDate: LocalDateTime?, completed: Boolean) {
        tasksRepository.setCompleted(taskId, occurrenceDate, completed)
    }

    /**
     * Deletes the whole task. For a repeating task that removes every occurrence, since they are
     * all expanded from the one stored task.
     */
    override suspend fun deleteTask(taskId: String) {
        tasksRepository.deleteTask(taskId)
    }

    override suspend fun task(taskId: String): Task? = tasksRepository.task(taskId)

    /**
     * A repeating task expands from its start date, so it always needs one: asking for a repeat
     * with no date starts the series today rather than creating a task that never comes due. A
     * weekly or monthly task also remembers which weekday or day-of-month it lands on, taken from
     * that start date. A [dueTime] only means something alongside a date.
     */
    override suspend fun addTask(
        title: String,
        description: String,
        priority: Priority,
        recurrenceType: RecurrenceType,
        assignedDate: LocalDate?,
        dueTime: LocalTime?,
    ) {
        tasksRepository.saveTask(
            Task(title = title, description = description, priority = priority, recurrenceType = recurrenceType)
                .scheduled(recurrenceType, assignedDate, dueTime),
        )
    }

    /**
     * Rewrites every editable field of an existing task, under the same rules as [addTask]. Its
     * identity and, for a one-off, its done state are kept; a repeating task's completion receipts
     * stay attached to whichever occurrence dates still expand.
     */
    override suspend fun updateTask(
        taskId: String,
        title: String,
        description: String,
        priority: Priority,
        recurrenceType: RecurrenceType,
        assignedDate: LocalDate?,
        dueTime: LocalTime?,
    ) {
        val existing = tasksRepository.task(taskId) ?: return
        tasksRepository.saveTask(
            existing.copy(title = title, description = description, priority = priority, recurrenceType = recurrenceType)
                .scheduled(recurrenceType, assignedDate, dueTime),
        )
    }
}

/** Applies the scheduling rules described on [TasksUseCase.addTask] to a task's other fields. */
private fun Task.scheduled(recurrenceType: RecurrenceType, assignedDate: LocalDate?, dueTime: LocalTime?): Task {
    val startDate = assignedDate
        ?: LocalDate.now().takeIf { recurrenceType != RecurrenceType.NONE }
    val assignedDateTime = startDate?.atTime(dueTime ?: DEFAULT_TASK_TIME)
    return copy(
        recurrenceType = recurrenceType,
        repeatDayOfWeek = assignedDateTime?.dayOfWeek.takeIf { recurrenceType == RecurrenceType.WEEKLY },
        repeatDayOfMonth = assignedDateTime?.dayOfMonth.takeIf { recurrenceType == RecurrenceType.MONTHLY },
        assignedDateTime = assignedDateTime,
        hasDueTime = startDate != null && dueTime != null,
    )
}

/**
 * Repeating tasks expand forever, so "all time" is bounded to the span the chart actually draws.
 */
private fun allTimeRange(): DateRange {
    val today = LocalDate.now()
    return DateRange(
        start = today.withDayOfMonth(1).minusMonths((ALL_TIME_CHART_MONTHS - 1).toLong()),
        end = today,
    )
}

/** Grouped under their priority, most urgent group first. Priorities with no tasks are left out. */
fun List<TaskListItem>.groupedByPriority(): List<TaskPriorityGroup> {
    val byPriority = groupBy { it.priority }
    return PRIORITY_ORDER.mapNotNull { priority ->
        byPriority[priority]?.let { TaskPriorityGroup(priority = priority, tasks = it) }
    }
}

/** Most important first; tasks of equal priority keep the order they arrived in. */
private fun List<TaskListItem>.byImportance(): List<TaskListItem> =
    sortedByDescending { it.priority.ordinal }

private fun Task.toListItem(
    occurrence: LocalDateTime?,
    completions: List<TaskCompletion>,
    now: LocalDateTime,
) = TaskListItem(
    taskId = id,
    occurrenceDate = occurrence,
    hasDueTime = hasDueTime,
    title = title,
    description = description,
    priority = priority,
    state = stateOf(isDoneAt(occurrence, completions), occurrence, hasDueTime, now),
    recurrenceType = recurrenceType,
)

private fun Task.toStat(
    occurrence: LocalDateTime?,
    completions: List<TaskCompletion>,
    now: LocalDateTime,
) = TaskOccurrenceStat(
    occurrenceDate = occurrence,
    hasDueTime = hasDueTime,
    state = stateOf(isDoneAt(occurrence, completions), occurrence, hasDueTime, now),
    completedAt = completedAtFor(occurrence, completions),
)

private fun Task.isDoneAt(occurrence: LocalDateTime?, completions: List<TaskCompletion>): Boolean =
    if (isRepeating && occurrence != null) {
        completions.any { it.taskId == id && it.occurrenceDate == occurrence }
    } else {
        isCompleted
    }

private fun Task.completedAtFor(occurrence: LocalDateTime?, completions: List<TaskCompletion>): LocalDateTime? =
    if (isRepeating && occurrence != null) {
        completions.find { it.taskId == id && it.occurrenceDate == occurrence }?.completedAt
    } else {
        // A non-repeating task records only that it's done, never when, so it counts as on time.
        occurrence.takeIf { isCompleted }
    }

private fun stateOf(isDone: Boolean, occurrence: LocalDateTime?, hasDueTime: Boolean, now: LocalDateTime): TaskState =
    when {
        isDone -> TaskState.DONE
        occurrence != null && isPast(occurrence, hasDueTime, now) -> TaskState.MISSED
        else -> TaskState.PENDING
    }

/**
 * Whether [deadline] had already passed at [moment]. A deadline with a chosen time is read to the
 * minute; one without is the whole day — see [DEFAULT_TASK_TIME].
 */
private fun isPast(deadline: LocalDateTime, hasDueTime: Boolean, moment: LocalDateTime): Boolean =
    if (hasDueTime) deadline.isBefore(moment) else deadline.toLocalDate().isBefore(moment.toLocalDate())

private fun PeriodTaskOccurrences.toSummary(): TasksSummary = TasksSummary(
    available = occurrences.size,
    completed = occurrences.count { it.state == TaskState.DONE },
    onTimeRatePercent = occurrences.onTimeRatePercent(),
)

private fun weekChartBuckets(periodOccurrences: PeriodTaskOccurrences): List<ChartPoint> {
    val range = periodOccurrences.dateRange ?: return emptyList()
    return range.weekChartBuckets().map { bucket -> periodOccurrences.chartPointIn(bucket) }
}

private fun monthChartBuckets(periodOccurrences: PeriodTaskOccurrences): List<ChartPoint> {
    val range = periodOccurrences.dateRange ?: return emptyList()
    return range.monthChartBuckets().map { bucket -> periodOccurrences.chartPointIn(bucket) }
}

/** The on-time rate of the occurrences falling inside [bucket]; undated ones fall in none. */
private fun PeriodTaskOccurrences.chartPointIn(bucket: ChartBucket): ChartPoint {
    val bucketOccurrences = occurrences.filter { stat -> stat.date?.let { it in bucket.range } ?: false }
    return ChartPoint(label = bucket.label, values = listOf(bucketOccurrences.onTimeRatePercent()))
}

/** One column per month across [allTimeRange], oldest first. */
private fun allTimeChartBuckets(periodOccurrences: PeriodTaskOccurrences): List<ChartPoint> {
    val firstMonth = allTimeRange().start
    return (0 until ALL_TIME_CHART_MONTHS).map { index ->
        val monthStart = firstMonth.plusMonths(index.toLong())
        val bucket = ChartBucket(
            label = monthStart.format(monthLabelFormatter),
            range = DateRange(monthStart, monthStart.plusMonths(1).minusDays(1)),
        )
        periodOccurrences.chartPointIn(bucket)
    }
}

private fun List<TaskOccurrenceStat>.onTimeRatePercent(): Int {
    val completed = filter { it.state == TaskState.DONE }
    if (completed.isEmpty()) return 0
    val onTime = completed.count { it.isOnTime() }
    return (onTime * 100) / completed.size
}

/** A task with no [TaskOccurrenceStat.occurrenceDate] has no deadline, so it's always on time. */
private fun TaskOccurrenceStat.isOnTime(): Boolean {
    val dueDateTime = occurrenceDate ?: return true
    val completedAt = completedAt ?: return false
    return !isPast(dueDateTime, hasDueTime, completedAt)
}

private fun PeriodTaskOccurrences.toTaskStatusBreakdown(): TaskStatusBreakdown {
    val now = LocalDateTime.now()
    val missed = occurrences.count { it.isMissed(now) }
    val completed = occurrences.count { it.state == TaskState.DONE && !it.isMissed(now) }
    val pending = occurrences.size - completed - missed
    return TaskStatusBreakdown(completed = completed, missed = missed, pending = pending)
}

/**
 * Missed covers occurrences still open past their deadline, as well as ones that did get done,
 * but only after their deadline had already passed.
 */
private fun TaskOccurrenceStat.isMissed(now: LocalDateTime): Boolean =
    if (state == TaskState.DONE) !isOnTime() else isOverdue(now)

/** A task with no [TaskOccurrenceStat.occurrenceDate] has no deadline, so it's never overdue. */
private fun TaskOccurrenceStat.isOverdue(now: LocalDateTime): Boolean {
    val dueDateTime = occurrenceDate ?: return false
    return isPast(dueDateTime, hasDueTime, now)
}
