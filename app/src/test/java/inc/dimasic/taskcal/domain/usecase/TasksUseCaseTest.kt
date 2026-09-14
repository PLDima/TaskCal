package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.database.FakeTaskCompletionDao
import inc.dimasic.taskcal.data.database.FakeTaskDao
import inc.dimasic.taskcal.data.database.toDomain
import inc.dimasic.taskcal.data.database.toEntity
import inc.dimasic.taskcal.data.repository.TasksRepository
import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.model.Task
import inc.dimasic.taskcal.domain.model.TaskCompletion
import inc.dimasic.taskcal.domain.model.TasksPeriod
import inc.dimasic.taskcal.domain.usecase.model.PeriodTaskOccurrences
import inc.dimasic.taskcal.firstValue
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The tasks-statistics rules: how a period's occurrences are counted, how they partition into
 * completed / missed / pending, and how the on-time rate is bucketed for each period's chart.
 *
 * A deadline is a calendar day, so "overdue" is anchored to yesterday and "still pending" to today,
 * and a late completion is one that lands the day after. The partition tests read the all-time
 * period, which is the one guaranteed to hold both of those days whatever the date; the week and
 * month tests anchor to the start of the current week or month instead.
 */
class TasksUseCaseTest {

    private val today = LocalDate.now()
    private val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private val mondayStart = monday.atTime(9, 0)
    private val yesterday = today.minusDays(1).atTime(9, 0)
    private val todayAtNine = today.atTime(9, 0)

    private fun useCase(tasks: List<Task>, completions: List<TaskCompletion> = emptyList()) =
        TasksUseCase(
            TasksRepository(
                FakeTaskDao(tasks.map { it.toEntity() }),
                FakeTaskCompletionDao(completions.map { it.toEntity() }),
            ),
        )

    private fun TasksUseCase.occurrences(period: TasksPeriod, offset: Int = 0): PeriodTaskOccurrences =
        occurrencesForPeriod(flowOf(period), flowOf(offset)).firstValue()

    @Test
    fun `occurrences partition into completed, missed and pending, and always add up`() {
        val onTime = oneOff("on-time", yesterday, isCompleted = true)
        val late = weekly("late", yesterday)
        val overdue = oneOff("overdue", yesterday)
        val upcoming = oneOff("upcoming", todayAtNine)
        val useCase = useCase(
            tasks = listOf(onTime, late, overdue, upcoming),
            // Finished the day after its deadline: done, but not on time.
            completions = listOf(completion(late, yesterday, completedAt = yesterday.plusDays(1))),
        )

        val occurrences = useCase.occurrences(TasksPeriod.ALL_TIME)
        val status = useCase.taskStatusOf(occurrences)

        assertEquals(1, status.completed)
        assertEquals(2, status.missed)
        assertEquals(1, status.pending)
        assertEquals(occurrences.occurrences.size, status.completed + status.missed + status.pending)
    }

    @Test
    fun `summary counts late completions as completed, but not as on time`() {
        val onTime = oneOff("on-time", yesterday, isCompleted = true)
        val late = weekly("late", yesterday)
        val overdue = oneOff("overdue", yesterday)
        val useCase = useCase(
            tasks = listOf(onTime, late, overdue),
            completions = listOf(completion(late, yesterday, completedAt = yesterday.plusDays(1))),
        )

        val summary = useCase.summaryOf(useCase.occurrences(TasksPeriod.ALL_TIME))

        assertEquals(3, summary.available)
        // Both finished tasks count here — the breakdown is where the late one moves to missed.
        assertEquals(2, summary.completed)
        // One of the two completions was on time.
        assertEquals(50, summary.onTimeRatePercent)
    }

    @Test
    fun `on-time rate is zero, not undefined, when nothing was completed`() {
        val useCase = useCase(tasks = listOf(oneOff("overdue", yesterday)))

        assertEquals(0, useCase.summaryOf(useCase.occurrences(TasksPeriod.ALL_TIME)).onTimeRatePercent)
    }

    @Test
    fun `an undated task has no deadline, so it is always on time and never overdue`() {
        val undated = undated("undated", isCompleted = true)
        val useCase = useCase(tasks = listOf(undated))

        val occurrences = useCase.occurrences(TasksPeriod.ALL_TIME)

        assertEquals(100, useCase.summaryOf(occurrences).onTimeRatePercent)
        assertEquals(1, useCase.taskStatusOf(occurrences).completed)
        assertEquals(0, useCase.taskStatusOf(occurrences).missed)
    }

    @Test
    fun `undated tasks only appear in the unbounded all-time period`() {
        val useCase = useCase(tasks = listOf(oneOff("dated", todayAtNine), undated("undated")))

        assertEquals(1, useCase.occurrences(TasksPeriod.WEEK).occurrences.size)
        assertEquals(1, useCase.occurrences(TasksPeriod.MONTH).occurrences.size)
        assertEquals(2, useCase.occurrences(TasksPeriod.ALL_TIME).occurrences.size)
    }

    @Test
    fun `a repeating task contributes one occurrence per expansion inside the period`() {
        val useCase = useCase(tasks = listOf(daily("daily", mondayStart)))

        val occurrences = useCase.occurrences(TasksPeriod.WEEK)

        assertEquals(7, occurrences.occurrences.size)
        assertEquals(weekRangeAt(0), occurrences.dateRange)
    }

    @Test
    fun `week chart keeps every weekday's slot with that day's on-time rate`() {
        val daily = daily("daily", mondayStart)
        val useCase = useCase(
            tasks = listOf(daily),
            completions = listOf(
                completion(daily, mondayStart, completedAt = mondayStart),
                completion(daily, mondayStart.plusDays(1), completedAt = mondayStart.plusDays(2)),
            ),
        )

        val chart = useCase.onTimeRateChartOf(useCase.occurrences(TasksPeriod.WEEK), TasksPeriod.WEEK)

        assertEquals(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"), chart.map { it.label })
        // Monday: done that day. Tuesday: done on Wednesday. The rest: nothing completed, so zero.
        assertEquals(listOf(100, 0, 0, 0, 0, 0, 0), chart.map { it.values.single() })
    }

    @Test
    fun `month chart splits the month into five buckets`() {
        val firstOfMonth = today.withDayOfMonth(1).atTime(0, 0)
        val daily = daily("daily", firstOfMonth)
        val useCase = useCase(
            tasks = listOf(daily),
            completions = listOf(completion(daily, firstOfMonth, completedAt = firstOfMonth)),
        )

        val chart = useCase.onTimeRateChartOf(useCase.occurrences(TasksPeriod.MONTH), TasksPeriod.MONTH)

        assertEquals(listOf("W1", "W2", "W3", "W4", "W5"), chart.map { it.label })
        assertEquals(100, chart[0].values.single())
        assertTrue(chart.drop(1).all { it.values.single() == 0 })
    }

    @Test
    fun `all-time chart covers the last six months, oldest first`() {
        val firstOfMonth = today.withDayOfMonth(1).atTime(0, 0)
        val useCase = useCase(tasks = listOf(oneOff("this-month", firstOfMonth, isCompleted = true)))

        val chart = useCase.onTimeRateChartOf(useCase.occurrences(TasksPeriod.ALL_TIME), TasksPeriod.ALL_TIME)

        val monthLabel = DateTimeFormatter.ofPattern("MMM")
        val expectedLabels = (5 downTo 0).map { firstOfMonth.minusMonths(it.toLong()).format(monthLabel) }
        assertEquals(expectedLabels, chart.map { it.label })
        assertEquals(100, chart.last().values.single())
        assertTrue(chart.dropLast(1).all { it.values.single() == 0 })
    }

    @Test
    fun `bounded charts are empty for the unbounded period`() {
        val useCase = useCase(tasks = emptyList())

        assertTrue(useCase.onTimeRateChartOf(PeriodTaskOccurrences.Empty, TasksPeriod.WEEK).isEmpty())
        assertTrue(useCase.onTimeRateChartOf(PeriodTaskOccurrences.Empty, TasksPeriod.MONTH).isEmpty())
    }

    /**
     * Nothing collects a time of day yet, so every task carries a placeholder hour. A deadline is
     * the whole day: ticking a repeating task later that same day must not read as late.
     */
    @Test
    fun `a repeating task completed later the same day is on time`() {
        // Weekly, so Monday's is the week's only occurrence: a daily task would also carry every
        // weekday already passed as missed, and the count would depend on the day the test runs.
        val weekly = weekly("weekly", mondayStart)
        val useCase = useCase(
            tasks = listOf(weekly),
            completions = listOf(completion(weekly, mondayStart, completedAt = mondayStart.plusHours(1))),
        )

        val occurrences = useCase.occurrences(TasksPeriod.WEEK)

        assertEquals(1, occurrences.occurrences.size)
        assertEquals(100, useCase.summaryOf(occurrences).onTimeRatePercent)
        assertEquals(1, useCase.taskStatusOf(occurrences).completed)
        assertEquals(0, useCase.taskStatusOf(occurrences).missed)
    }

    @Test
    fun `a task due today is still pending, whatever its placeholder hour`() {
        val useCase = useCase(tasks = listOf(oneOff("today", today.atTime(0, 0))))

        val status = useCase.taskStatusOf(useCase.occurrences(TasksPeriod.ALL_TIME))

        assertEquals(1, status.pending)
        assertEquals(0, status.missed)
    }

    @Test
    fun `a weekly or monthly task remembers which day it lands on, from its start date`() {
        val dao = FakeTaskDao()
        val useCase = TasksUseCase(TasksRepository(dao, FakeTaskCompletionDao(emptyList())))

        runBlocking {
            useCase.addTask("weekly", "", Priority.LOW, RecurrenceType.WEEKLY, monday)
            useCase.addTask("monthly", "", Priority.LOW, RecurrenceType.MONTHLY, monday)
            useCase.addTask("daily", "", Priority.LOW, RecurrenceType.DAILY, monday)
        }

        val saved = dao.stored.associate { it.title to it.toDomain() }
        assertEquals(DayOfWeek.MONDAY, saved.getValue("weekly").repeatDayOfWeek)
        assertEquals(null, saved.getValue("weekly").repeatDayOfMonth)
        assertEquals(monday.dayOfMonth, saved.getValue("monthly").repeatDayOfMonth)
        assertEquals(null, saved.getValue("monthly").repeatDayOfWeek)
        assertEquals(null, saved.getValue("daily").repeatDayOfWeek)
        assertEquals(null, saved.getValue("daily").repeatDayOfMonth)
    }

    @Test
    fun `a repeat asked for without a date starts today, a one-off stays undated`() {
        val dao = FakeTaskDao()
        val useCase = TasksUseCase(TasksRepository(dao, FakeTaskCompletionDao(emptyList())))

        runBlocking {
            useCase.addTask("repeat", "", Priority.LOW, RecurrenceType.DAILY, assignedDate = null)
            useCase.addTask("one-off", "", Priority.LOW, RecurrenceType.NONE, assignedDate = null)
        }

        val saved = dao.stored.associate { it.title to it.toDomain() }
        assertEquals(today, saved.getValue("repeat").assignedDateTime?.toLocalDate())
        assertEquals(null, saved.getValue("one-off").assignedDateTime)
    }

    @Test
    fun `a chosen due time is a real deadline, a placeholder hour is not`() {
        val timed = task("timed", RecurrenceType.WEEKLY, mondayStart, hasDueTime = true)
        val untimed = task("untimed", RecurrenceType.WEEKLY, mondayStart)
        val useCase = useCase(
            tasks = listOf(timed, untimed),
            // Both ticked an hour after the 09:00 stamp on the same day.
            completions = listOf(
                completion(timed, mondayStart, completedAt = mondayStart.plusHours(1)),
                completion(untimed, mondayStart, completedAt = mondayStart.plusHours(1)),
            ),
        )

        val status = useCase.taskStatusOf(useCase.occurrences(TasksPeriod.WEEK))

        // The timed one was due at 09:00 sharp and is late; the other was due by end of day.
        assertEquals(1, status.completed)
        assertEquals(1, status.missed)
    }

    @Test
    fun `adding with a time stamps it and marks it chosen, without one uses the placeholder`() {
        val dao = FakeTaskDao()
        val useCase = TasksUseCase(TasksRepository(dao, FakeTaskCompletionDao(emptyList())))

        runBlocking {
            useCase.addTask("timed", "", Priority.LOW, RecurrenceType.NONE, today, dueTime = LocalTime.of(14, 30))
            useCase.addTask("untimed", "", Priority.LOW, RecurrenceType.NONE, today)
        }

        val saved = dao.stored.associate { it.title to it.toDomain() }
        assertEquals(today.atTime(14, 30), saved.getValue("timed").assignedDateTime)
        assertTrue(saved.getValue("timed").hasDueTime)
        assertEquals(today, saved.getValue("untimed").assignedDateTime?.toLocalDate())
        assertTrue(!saved.getValue("untimed").hasDueTime)
    }

    @Test
    fun `updating rewrites the fields and reschedules, but keeps identity and done state`() {
        val original = oneOff("original", yesterday, isCompleted = true)
        val dao = FakeTaskDao(listOf(original.toEntity()))
        val useCase = TasksUseCase(TasksRepository(dao, FakeTaskCompletionDao(emptyList())))

        runBlocking {
            useCase.updateTask(
                taskId = original.id,
                title = "renamed",
                description = "with notes",
                priority = Priority.HIGH,
                recurrenceType = RecurrenceType.WEEKLY,
                assignedDate = monday,
                dueTime = null,
            )
        }

        val updated = dao.stored.single().toDomain()
        assertEquals(original.id, updated.id)
        assertEquals("renamed", updated.title)
        assertEquals("with notes", updated.description)
        assertEquals(Priority.HIGH, updated.priority)
        assertEquals(RecurrenceType.WEEKLY, updated.recurrenceType)
        assertEquals(DayOfWeek.MONDAY, updated.repeatDayOfWeek)
        assertEquals(monday, updated.assignedDateTime?.toLocalDate())
        assertTrue(updated.isCompleted)
    }

    private fun task(
        id: String,
        recurrenceType: RecurrenceType,
        assignedDateTime: LocalDateTime?,
        isCompleted: Boolean = false,
        hasDueTime: Boolean = false,
    ) = Task(
        id = id,
        title = id,
        description = "",
        priority = Priority.MEDIUM,
        recurrenceType = recurrenceType,
        repeatDayOfWeek = assignedDateTime?.dayOfWeek.takeIf { recurrenceType == RecurrenceType.WEEKLY },
        assignedDateTime = assignedDateTime,
        hasDueTime = hasDueTime,
        isCompleted = isCompleted,
    )

    private fun oneOff(id: String, at: LocalDateTime, isCompleted: Boolean = false) =
        task(id, RecurrenceType.NONE, at, isCompleted)

    private fun undated(id: String, isCompleted: Boolean = false) =
        task(id, RecurrenceType.NONE, assignedDateTime = null, isCompleted = isCompleted)

    private fun daily(id: String, from: LocalDateTime) = task(id, RecurrenceType.DAILY, from)

    /** Lands once a week on [from]'s weekday, so inside the current week it has exactly one occurrence. */
    private fun weekly(id: String, from: LocalDateTime) = task(id, RecurrenceType.WEEKLY, from)

    private fun completion(task: Task, occurrence: LocalDateTime, completedAt: LocalDateTime) =
        TaskCompletion(taskId = task.id, occurrenceDate = occurrence, completedAt = completedAt)
}
