package inc.dimasic.taskcal.data.remote

import inc.dimasic.taskcal.data.remote.model.DeviceAuthRequestDto
import inc.dimasic.taskcal.data.remote.model.FoodEntryDto
import inc.dimasic.taskcal.data.remote.model.TaskCompletionDto
import inc.dimasic.taskcal.data.remote.model.TaskDto
import inc.dimasic.taskcal.data.remote.model.UserDto
import inc.dimasic.taskcal.data.remote.model.UserProfileDto
import inc.dimasic.taskcal.domain.model.RecurrenceType
import java.io.IOException
import java.time.LocalDate
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/**
 * An in-memory backend that follows the rules in the API documentation: ids come from the client,
 * saves are upserts, another user's data is never reachable, and errors are `problem+json`.
 * Setting [failWith] or [unauthorized] makes every following call fail that way.
 */
class FakeTaskCalApi : TaskCalApi {

    var failWith: IOException? = null
    var unauthorized = false

    val registeredTokens = mutableListOf<String>()

    private val tasks = linkedMapOf<String, TaskDto>()
    private val completions = linkedMapOf<String, TaskCompletionDto>()
    private val food = linkedMapOf<String, FoodEntryDto>()
    private var profile: UserProfileDto? = null

    override suspend fun health() {
        failIfBroken()
    }

    override suspend fun registerDevice(request: DeviceAuthRequestDto): UserDto {
        failWith?.let { throw it }
        registeredTokens += request.deviceToken
        return UserDto(USER_ID)
    }

    override suspend fun me(): UserDto {
        failIfBroken()
        return UserDto(USER_ID)
    }

    override suspend fun deleteAccount() {
        failIfBroken()
        tasks.clear()
        completions.clear()
        food.clear()
        profile = null
    }

    override suspend fun profile(): UserProfileDto {
        failIfBroken()
        return profile ?: throw problem(404, "Not found")
    }

    override suspend fun saveProfile(profile: UserProfileDto): UserProfileDto {
        failIfBroken()
        this.profile = profile
        return profile
    }

    override suspend fun tasks(): List<TaskDto> {
        failIfBroken()
        return tasks.values.toList()
    }

    override suspend fun task(taskId: String): TaskDto {
        failIfBroken()
        return tasks[taskId] ?: throw problem(404, "Not found")
    }

    override suspend fun saveTask(taskId: String, task: TaskDto): TaskDto {
        failIfBroken()
        if (task.title.isBlank()) throw problem(400, "Validation failed", "title can't be blank")
        require(task.id == null || task.id == taskId) { "The body id must match the URL" }
        return task.copy(id = taskId).also { tasks[taskId] = it }
    }

    override suspend fun deleteTask(taskId: String) {
        failIfBroken()
        tasks.remove(taskId) ?: throw problem(404, "Not found")
        completions.values.removeAll { it.taskId == taskId }
    }

    override suspend fun completions(taskId: String): List<TaskCompletionDto> {
        failIfBroken()
        if (taskId !in tasks) throw problem(404, "Not found")
        return completions.values.filter { it.taskId == taskId }.sortedBy { it.occurrenceDate }
    }

    override suspend fun saveCompletion(
        taskId: String,
        completionId: String,
        completion: TaskCompletionDto,
    ): TaskCompletionDto {
        failIfBroken()
        val task = tasks[taskId] ?: throw problem(404, "Not found")
        if (task.recurrenceType == RecurrenceType.NONE) throw problem(409, "Task doesn't repeat")
        val duplicate = completions.values.any {
            it.taskId == taskId && it.id != completionId && it.occurrenceDate == completion.occurrenceDate
        }
        if (duplicate) throw problem(409, "Occurrence already completed")
        return completion.copy(id = completionId, taskId = taskId).also { completions[completionId] = it }
    }

    override suspend fun deleteCompletion(taskId: String, completionId: String) {
        failIfBroken()
        val existing = completions[completionId]
        if (existing == null || existing.taskId != taskId) throw problem(404, "Not found")
        completions.remove(completionId)
    }

    override suspend fun foodEntries(from: LocalDate?, to: LocalDate?): List<FoodEntryDto> {
        failIfBroken()
        if (from != null && to != null && from > to) throw problem(400, "Validation failed", "from is after to")
        return food.values
            .filter { (from == null || it.date >= from) && (to == null || it.date <= to) }
            .sortedBy { it.date }
    }

    override suspend fun foodEntry(entryId: String): FoodEntryDto {
        failIfBroken()
        return food[entryId] ?: throw problem(404, "Not found")
    }

    override suspend fun saveFoodEntry(entryId: String, entry: FoodEntryDto): FoodEntryDto {
        failIfBroken()
        return entry.copy(id = entryId).also { food[entryId] = it }
    }

    override suspend fun deleteFoodEntry(entryId: String) {
        failIfBroken()
        food.remove(entryId) ?: throw problem(404, "Not found")
    }

    private fun failIfBroken() {
        failWith?.let { throw it }
        if (unauthorized) throw problem(401, "Unauthorized")
    }

    private fun problem(status: Int, title: String, vararg errors: String): HttpException {
        val errorList = errors.joinToString(",") { "\"$it\"" }
        val body = "{\"type\":\"about:blank\",\"title\":\"$title\",\"status\":$status,\"errors\":[$errorList]}"
        return HttpException(
            Response.error<Any>(status, body.toResponseBody("application/problem+json".toMediaType())),
        )
    }

    companion object {
        const val USER_ID = "11111111-1111-1111-1111-111111111111"
    }
}
