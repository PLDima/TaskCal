package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.repository.TasksRepository
import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.model.Task
import inc.dimasic.taskcal.domain.model.TaskCompletion
import inc.dimasic.taskcal.domain.model.TaskState
import inc.dimasic.taskcal.domain.model.TasksPeriod
import inc.dimasic.taskcal.domain.usecase.model.PeriodTaskOccurrences
import inc.dimasic.taskcal.domain.usecase.model.TaskListItem
import inc.dimasic.taskcal.domain.usecase.model.TaskOccurrenceStat
import inc.dimasic.taskcal.domain.usecase.model.TaskPriorityGroup
import inc.dimasic.taskcal.domain.usecase.model.TaskStatusBreakdown
import inc.dimasic.taskcal.domain.usecase.model.ChartPoint
import inc.dimasic.taskcal.domain.usecase.model.TasksSummary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.ceil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private val WEEKDAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private const val MONTH_CHART_BUCKETS = 5
private const val ALL_TIME_CHART_MONTHS = 6
private val monthLabelFormatter = DateTimeFormatter.ofPattern("MMM")

/** Most urgent first, which is the order tasks are grouped and listed in. */
private val PRIORITY_ORDER = listOf(Priority.HIGH, Priority.MEDIUM, Priority.LOW)

/** Nothing collects a time of day yet, so a task is due at this hour of its date. */
private val DEFAULT_TASK_TIME: LocalTime = LocalTime.of(9, 0)

class TasksUseCase @Inject constructor(
    private val tasksRepository: TasksRepository,
) {

    /**
     * Occurrences falling on [date], most important first. Tasks with no date have no deadline,
     * so they always apply. Completed occurrences are dropped: the Today list shows only what is
     * still outstanding.
     */
    fun tasksForDate(date: Flow<LocalDate>): Flow<List<TaskListItem>> =
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
    fun allTasks(): Flow<List<TaskListItem>> =
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

    fun occurrencesForPeriod(period: Flow<TasksPeriod>, offset: Flow<Int>): Flow<PeriodTaskOccurrences> =
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

    fun summaryForPeriod(period: Flow<TasksPeriod>, offset: Flow<Int>): Flow<TasksSummary> =
        occurrencesForPeriod(period, offset).map { it.toSummary() }

    fun taskStatusForPeriod(period: Flow<TasksPeriod>, offset: Flow<Int>): Flow<TaskStatusBreakdown> =
        occurrencesForPeriod(period, offset).map { it.toTaskStatusBreakdown() }

    fun onTimeRateChartForPeriod(period: Flow<TasksPeriod>, offset: Flow<Int>): Flow<List<ChartPoint>> =
        combine(occurrencesForPeriod(period, offset), period) { periodOccurrences, selectedPeriod ->
            when (selectedPeriod) {
                TasksPeriod.WEEK -> weekChartBuckets(periodOccurrences)
                TasksPeriod.MONTH -> monthChartBuckets(periodOccurrences)
                TasksPeriod.ALL_TIME -> allTimeChartBuckets(periodOccurrences)
            }
        }

    suspend fun setTaskCompleted(taskId: String, occurrenceDate: LocalDateTime?, completed: Boolean) {
        tasksRepository.setCompleted(taskId, occurrenceDate, completed)
    }

    /**
     * Deletes the whole task. For a repeating task that removes every occurrence, since they are
     * all expanded from the one stored task.
     */
    suspend fun deleteTask(taskId: String) {
        tasksRepository.deleteTask(taskId)
    }

    /**
     * A repeating task expands from its start date, so it always needs one: asking for a repeat
     * with no date starts the series today rather than creating a task that never comes due.
     */
    suspend fun addTask(
        title: String,
        description: String,
        priority: Priority,
        recurrenceType: RecurrenceType,
        assignedDate: LocalDate?,
    ) {
        val startDate = assignedDate
            ?: LocalDate.now().takeIf { recurrenceType != RecurrenceType.NONE }
        tasksRepository.addTask(
            title = title,
            description = description,
            priority = priority,
            recurrenceType = recurrenceType,
            assignedDateTime = startDate?.atTime(DEFAULT_TASK_TIME),
        )
    }
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
    title = title,
    description = description,
    priority = priority,
    state = stateOf(isDoneAt(occurrence, completions), occurrence, now),
    recurrenceType = recurrenceType,
)

private fun Task.toStat(
    occurrence: LocalDateTime?,
    completions: List<TaskCompletion>,
    now: LocalDateTime,
) = TaskOccurrenceStat(
    occurrenceDate = occurrence,
    state = stateOf(isDoneAt(occurrence, completions), occurrence, now),
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

private fun stateOf(isDone: Boolean, occurrence: LocalDateTime?, now: LocalDateTime): TaskState = when {
    isDone -> TaskState.DONE
    occurrence != null && occurrence.isBefore(now) -> TaskState.MISSED
    else -> TaskState.PENDING
}

private fun PeriodTaskOccurrences.toSummary(): TasksSummary = TasksSummary(
    available = occurrences.size,
    completed = occurrences.count { it.state == TaskState.DONE },
    onTimeRatePercent = occurrences.onTimeRatePercent(),
)

private fun weekChartBuckets(periodOccurrences: PeriodTaskOccurrences): List<ChartPoint> {
    val range = periodOccurrences.dateRange ?: return emptyList()
    val occurrencesByDate = periodOccurrences.occurrences.groupBy { it.date }
    return WEEKDAY_LABELS.mapIndexed { index, label ->
        val date = range.start.plusDays(index.toLong())
        ChartPoint(label = label, values = listOf(occurrencesByDate[date].orEmpty().onTimeRatePercent()))
    }
}

private fun monthChartBuckets(periodOccurrences: PeriodTaskOccurrences): List<ChartPoint> {
    val range = periodOccurrences.dateRange ?: return emptyList()
    val totalDays = range.dayCount
    val bucketSize = ceil(totalDays / MONTH_CHART_BUCKETS.toDouble()).toInt().coerceAtLeast(1)
    return (0 until MONTH_CHART_BUCKETS).map { bucketIndex ->
        val bucketStart = range.start.plusDays((bucketIndex * bucketSize).toLong())
        val bucketEnd = minOf(bucketStart.plusDays((bucketSize - 1).toLong()), range.end)
        val bucketOccurrences = periodOccurrences.occurrences.filter { stat ->
            val date = stat.date ?: return@filter false
            !date.isBefore(bucketStart) && !date.isAfter(bucketEnd)
        }
        ChartPoint(label = "W${bucketIndex + 1}", values = listOf(bucketOccurrences.onTimeRatePercent()))
    }
}

private fun allTimeChartBuckets(periodOccurrences: PeriodTaskOccurrences): List<ChartPoint> {
    val firstMonth = LocalDate.now().withDayOfMonth(1).minusMonths((ALL_TIME_CHART_MONTHS - 1).toLong())
    return (0 until ALL_TIME_CHART_MONTHS).map { index ->
        val monthStart = firstMonth.plusMonths(index.toLong())
        val monthEnd = monthStart.plusMonths(1).minusDays(1)
        val monthOccurrences = periodOccurrences.occurrences.filter { stat ->
            val date = stat.date ?: return@filter false
            !date.isBefore(monthStart) && !date.isAfter(monthEnd)
        }
        ChartPoint(label = monthStart.format(monthLabelFormatter), values = listOf(monthOccurrences.onTimeRatePercent()))
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
    return !completedAt.isAfter(dueDateTime)
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
    return dueDateTime.isBefore(now)
}
