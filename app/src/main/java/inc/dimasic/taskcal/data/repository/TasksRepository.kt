package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.database.TaskCompletionDao
import inc.dimasic.taskcal.data.database.TaskDao
import inc.dimasic.taskcal.data.database.toDomain
import inc.dimasic.taskcal.data.database.toEntity
import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.model.Task
import inc.dimasic.taskcal.domain.model.TaskCompletion
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed store of tasks and the completion receipts of their repeating occurrences. Deriving
 * occurrences from a task's recurrence rule is business logic and lives in
 * [inc.dimasic.taskcal.domain.usecase.TasksUseCase] instead.
 */
@Singleton
class TasksRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val taskCompletionDao: TaskCompletionDao,
) {

    val tasks: Flow<List<Task>> = taskDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    val completions: Flow<List<TaskCompletion>> =
        taskCompletionDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun addTask(
        title: String,
        description: String,
        priority: Priority,
        recurrenceType: RecurrenceType,
        assignedDateTime: LocalDateTime?,
    ) {
        val task = Task(
            title = title,
            description = description,
            priority = priority,
            recurrenceType = recurrenceType,
            repeatDayOfWeek = assignedDateTime?.dayOfWeek.takeIf { recurrenceType == RecurrenceType.WEEKLY },
            repeatDayOfMonth = assignedDateTime?.dayOfMonth.takeIf { recurrenceType == RecurrenceType.MONTHLY },
            assignedDateTime = assignedDateTime,
        )
        taskDao.upsert(task.toEntity())
    }

    /** Removes a task for good. Its completion receipts are cascaded away by the foreign key. */
    suspend fun deleteTask(taskId: String) {
        taskDao.delete(taskId)
    }

    /**
     * Marks one occurrence done or not done. A repeating task gains or loses a [TaskCompletion];
     * anything else just flips [Task.isCompleted], since it has only the one instance.
     */
    suspend fun setCompleted(taskId: String, occurrenceDate: LocalDateTime?, completed: Boolean) {
        val task = taskDao.findById(taskId)?.toDomain() ?: return

        when {
            !task.isRepeating || occurrenceDate == null ->
                taskDao.setCompleted(taskId, completed)

            completed ->
                taskCompletionDao.insert(
                    TaskCompletion(
                        taskId = taskId,
                        occurrenceDate = occurrenceDate,
                        completedAt = LocalDateTime.now(),
                    ).toEntity(),
                )

            else ->
                taskCompletionDao.delete(taskId, occurrenceDate.toString())
        }
    }
}
