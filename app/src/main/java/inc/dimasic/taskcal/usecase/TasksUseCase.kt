package inc.dimasic.taskcal.usecase

import inc.dimasic.taskcal.model.domain.Priority
import inc.dimasic.taskcal.model.domain.RecurrenceType
import inc.dimasic.taskcal.model.domain.Task
import inc.dimasic.taskcal.model.domain.TaskCompletion
import inc.dimasic.taskcal.model.domain.TaskState
import inc.dimasic.taskcal.model.ui.DateRange
import inc.dimasic.taskcal.model.ui.TasksPeriod
import inc.dimasic.taskcal.repository.TasksRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlin.math.ceil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private val WEEKDAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private const val MONTH_CHART_BUCKETS = 5
private const val ALL_TIME_CHART_MONTHS = 6
private val monthLabelFormatter = DateTimeFormatter.ofPattern("MMM")

/**
 * One occurrence of a [Task], flattened for a task list. A repeating task has no stored
 * instances, so [taskId] plus [occurrenceDate] is what identifies the row.
 */
data class TaskListItem(
    val taskId: String,
    /** Which occurrence this row stands for; null for a task with no date. */
    val occurrenceDate: LocalDateTime?,
    val title: String,
    val description: String,
    val priority: Priority,
    val state: TaskState,
) {
    val date: LocalDate? get() = occurrenceDate?.toLocalDate()
}

/** Projection of one occurrence that's all the tasks-statistics calculations need. */
data class TaskOccurrenceStat(
    /** Null for a task with no date, which therefore has no deadline. */
    val occurrenceDate: LocalDateTime?,
    val state: TaskState,
    val completedAt: LocalDateTime?,
) {
    val date: LocalDate? get() = occurrenceDate?.toLocalDate()
}

data class PeriodTaskOccurrences(
    /** Null for [TasksPeriod.ALL_TIME], which has no bounds. */
    val dateRange: DateRange?,
    val occurrences: List<TaskOccurrenceStat>,
) {
    companion object {
        val Empty = PeriodTaskOccurrences(dateRange = null, occurrences = emptyList())
    }
}

data class TasksSummary(
    val available: Int,
    val completed: Int,
    val onTimeRatePercent: Int,
) {
    companion object {
        val Empty = TasksSummary(available = 0, completed = 0, onTimeRatePercent = 0)
    }
}

data class TasksChartPoint(
    val label: String,
    val onTimeRatePercent: Int,
)

data class TaskStatusBreakdown(
    val completed: Int,
    val missed: Int,
    val pending: Int,
) {
    companion object {
        val Empty = TaskStatusBreakdown(completed = 0, missed = 0, pending = 0)
    }
}

class TasksUseCase @Inject constructor(
    private val tasksRepository: TasksRepository,
) {

    /** Occurrences falling on [date]. Tasks with no date have no deadline, so they always apply. */
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
            }
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
            val range = dateRangeFor(selectedPeriod, selectedOffset)
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

    fun onTimeRateChartForPeriod(period: Flow<TasksPeriod>, offset: Flow<Int>): Flow<List<TasksChartPoint>> =
        combine(occurrencesForPeriod(period, offset), period) { periodOccurrences, selectedPeriod ->
            when (selectedPeriod) {
                TasksPeriod.WEEK -> weekChartBuckets(periodOccurrences)
                TasksPeriod.MONTH -> monthChartBuckets(periodOccurrences)
                TasksPeriod.ALL_TIME -> allTimeChartBuckets(periodOccurrences)
            }
        }

    fun setTaskCompleted(taskId: String, occurrenceDate: LocalDateTime?, completed: Boolean) {
        tasksRepository.setCompleted(taskId, occurrenceDate, completed)
    }

    fun addTask(
        title: String,
        description: String,
        priority: Priority,
        recurrenceType: RecurrenceType,
        assignedDateTime: LocalDateTime?,
    ) = tasksRepository.addTask(title, description, priority, recurrenceType, assignedDateTime)
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

private fun weekChartBuckets(periodOccurrences: PeriodTaskOccurrences): List<TasksChartPoint> {
    val range = periodOccurrences.dateRange ?: return emptyList()
    val occurrencesByDate = periodOccurrences.occurrences.groupBy { it.date }
    return WEEKDAY_LABELS.mapIndexed { index, label ->
        val date = range.start.plusDays(index.toLong())
        TasksChartPoint(label = label, onTimeRatePercent = occurrencesByDate[date].orEmpty().onTimeRatePercent())
    }
}

private fun monthChartBuckets(periodOccurrences: PeriodTaskOccurrences): List<TasksChartPoint> {
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
        TasksChartPoint(label = "W${bucketIndex + 1}", onTimeRatePercent = bucketOccurrences.onTimeRatePercent())
    }
}

private fun allTimeChartBuckets(periodOccurrences: PeriodTaskOccurrences): List<TasksChartPoint> {
    val firstMonth = LocalDate.now().withDayOfMonth(1).minusMonths((ALL_TIME_CHART_MONTHS - 1).toLong())
    return (0 until ALL_TIME_CHART_MONTHS).map { index ->
        val monthStart = firstMonth.plusMonths(index.toLong())
        val monthEnd = monthStart.plusMonths(1).minusDays(1)
        val monthOccurrences = periodOccurrences.occurrences.filter { stat ->
            val date = stat.date ?: return@filter false
            !date.isBefore(monthStart) && !date.isAfter(monthEnd)
        }
        TasksChartPoint(label = monthStart.format(monthLabelFormatter), onTimeRatePercent = monthOccurrences.onTimeRatePercent())
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
