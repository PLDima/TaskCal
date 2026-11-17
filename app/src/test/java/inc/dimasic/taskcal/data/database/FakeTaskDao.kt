package inc.dimasic.taskcal.data.database

import inc.dimasic.taskcal.data.local.database.TaskDao
import inc.dimasic.taskcal.data.local.database.TaskEntity
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [inc.dimasic.taskcal.data.local.database.TaskDao], so the real [inc.dimasic.taskcal.data.repository.TasksRepository] can run without Room. */
internal class FakeTaskDao(initial: List<TaskEntity> = emptyList()) : TaskDao {

    private val tasks = MutableStateFlow(initial)

    /** Whatever has been written, as a test would read it back. */
    val stored: List<TaskEntity> get() = tasks.value

    override fun observeAll() = tasks
    override suspend fun findById(id: String) = tasks.value.find { it.id == id }
    override suspend fun upsert(task: TaskEntity) {
        tasks.value = tasks.value.filterNot { it.id == task.id } + task
    }
    override suspend fun setCompleted(id: String, completed: Boolean) = error("not needed for this test")
    override suspend fun delete(id: String) = error("not needed for this test")
}
