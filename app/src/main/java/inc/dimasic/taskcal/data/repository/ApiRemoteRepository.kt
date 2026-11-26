package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.remote.DeviceTokenStore
import inc.dimasic.taskcal.data.remote.RemoteException
import inc.dimasic.taskcal.data.remote.TaskCalApi
import inc.dimasic.taskcal.data.remote.model.DeviceAuthRequestDto
import inc.dimasic.taskcal.data.remote.model.ProblemDto
import inc.dimasic.taskcal.data.remote.model.toDomain
import inc.dimasic.taskcal.data.remote.model.toDto
import inc.dimasic.taskcal.data.remote.toRemoteException
import inc.dimasic.taskcal.domain.model.FoodEntry
import inc.dimasic.taskcal.domain.model.Task
import inc.dimasic.taskcal.domain.model.TaskCompletion
import inc.dimasic.taskcal.domain.model.UserProfile
import com.squareup.moshi.Moshi
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

/** [RemoteRepository] backed by the TaskCal REST API. */
@Singleton
class ApiRemoteRepository @Inject constructor(
    private val api: TaskCalApi,
    private val tokens: DeviceTokenStore,
    moshi: Moshi,
) : RemoteRepository {

    private val problemAdapter = moshi.adapter(ProblemDto::class.java)

    override suspend fun health() = call { api.health() }

    override suspend fun registerDevice(): String =
        call { api.registerDevice(DeviceAuthRequestDto(tokens.token())).userId }

    override suspend fun currentUserId(): String = call { api.me().userId }

    override suspend fun deleteAccount() = call { api.deleteAccount() }

    override suspend fun profile(): UserProfile? = callOrNull { api.profile().toDomain() }

    override suspend fun saveProfile(profile: UserProfile): UserProfile =
        call { api.saveProfile(profile.toDto()).toDomain() }

    override suspend fun tasks(): List<Task> = call { api.tasks().map { it.toDomain() } }

    override suspend fun task(taskId: String): Task? = callOrNull { api.task(taskId).toDomain() }

    override suspend fun saveTask(task: Task): Task = call { api.saveTask(task.id, task.toDto()).toDomain() }

    override suspend fun deleteTask(taskId: String) = call { api.deleteTask(taskId) }

    override suspend fun completions(taskId: String): List<TaskCompletion> =
        call { api.completions(taskId).map { it.toDomain() } }

    override suspend fun saveCompletion(completion: TaskCompletion): TaskCompletion =
        call { api.saveCompletion(completion.taskId, completion.id, completion.toDto()).toDomain() }

    override suspend fun deleteCompletion(taskId: String, completionId: String) =
        call { api.deleteCompletion(taskId, completionId) }

    override suspend fun foodEntries(from: LocalDate?, to: LocalDate?): List<FoodEntry> =
        call { api.foodEntries(from, to).map { it.toDomain() } }

    override suspend fun foodEntry(entryId: String): FoodEntry? = callOrNull { api.foodEntry(entryId).toDomain() }

    override suspend fun saveFoodEntry(entry: FoodEntry): FoodEntry =
        call { api.saveFoodEntry(entry.id, entry.toDto()).toDomain() }

    override suspend fun deleteFoodEntry(entryId: String) = call { api.deleteFoodEntry(entryId) }

    /** Runs [block], turning what the network layer throws into a [RemoteException]. */
    private suspend fun <T> call(block: suspend () -> T): T = try {
        block()
    } catch (e: HttpException) {
        throw e.toRemoteException(problemAdapter)
    } catch (e: IOException) {
        throw RemoteException.Network(e)
    }

    /** Like [call], but a missing item is an expected answer, not a failure. */
    private suspend fun <T : Any> callOrNull(block: suspend () -> T): T? = try {
        call(block)
    } catch (e: RemoteException.NotFound) {
        null
    }
}