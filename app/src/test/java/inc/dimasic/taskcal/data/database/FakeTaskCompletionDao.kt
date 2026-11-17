package inc.dimasic.taskcal.data.database

import inc.dimasic.taskcal.data.local.database.TaskCompletionDao
import inc.dimasic.taskcal.data.local.database.TaskCompletionEntity
import kotlinx.coroutines.flow.flowOf

internal class FakeTaskCompletionDao(private val completions: List<TaskCompletionEntity>) :
    TaskCompletionDao {
    override fun observeAll() = flowOf(completions)
    override suspend fun insert(completion: TaskCompletionEntity) = error("not needed for this test")
    override suspend fun delete(taskId: String, occurrenceDate: String) = error("not needed for this test")
}
