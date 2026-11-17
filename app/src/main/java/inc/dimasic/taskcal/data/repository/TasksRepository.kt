package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.local.database.TaskCompletionDao
import inc.dimasic.taskcal.data.local.database.TaskDao
import inc.dimasic.taskcal.data.local.database.toDomain
import inc.dimasic.taskcal.data.local.database.toEntity
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

    suspend fun task(taskId: String): Task? = taskDao.findById(taskId)?.toDomain()

    /** Writes the task, replacing whatever is stored under the same [Task.id]. */
    suspend fun saveTask(task: Task) {
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
