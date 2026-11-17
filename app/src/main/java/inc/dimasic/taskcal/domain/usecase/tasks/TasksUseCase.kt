package inc.dimasic.taskcal.domain.usecase.tasks

import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.model.Task
import inc.dimasic.taskcal.domain.model.TasksPeriod
import inc.dimasic.taskcal.domain.usecase.model.ChartPoint
import inc.dimasic.taskcal.domain.usecase.model.PeriodTaskOccurrences
import inc.dimasic.taskcal.domain.usecase.model.TaskListItem
import inc.dimasic.taskcal.domain.usecase.model.TaskStatusBreakdown
import inc.dimasic.taskcal.domain.usecase.model.TasksSummary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow

interface TasksUseCase {
    fun tasksForDate(date: Flow<LocalDate>): Flow<List<TaskListItem>>

    fun allTasks(): Flow<List<TaskListItem>>

    fun occurrencesForPeriod(period: Flow<TasksPeriod>, offset: Flow<Int>): Flow<PeriodTaskOccurrences>

    fun summaryOf(occurrences: PeriodTaskOccurrences): TasksSummary

    fun taskStatusOf(occurrences: PeriodTaskOccurrences): TaskStatusBreakdown

    fun onTimeRateChartOf(occurrences: PeriodTaskOccurrences, period: TasksPeriod): List<ChartPoint>

    suspend fun setTaskCompleted(taskId: String, occurrenceDate: LocalDateTime?, completed: Boolean)

    suspend fun deleteTask(taskId: String)

    suspend fun task(taskId: String): Task?

    suspend fun addTask(
        title: String,
        description: String,
        priority: Priority,
        recurrenceType: RecurrenceType,
        assignedDate: LocalDate?,
        dueTime: LocalTime? = null,
    )

    suspend fun updateTask(
        taskId: String,
        title: String,
        description: String,
        priority: Priority,
        recurrenceType: RecurrenceType,
        assignedDate: LocalDate?,
        dueTime: LocalTime?,
    )
}
