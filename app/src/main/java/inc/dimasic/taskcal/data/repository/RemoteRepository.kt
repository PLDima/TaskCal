package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.domain.model.FoodEntry
import inc.dimasic.taskcal.domain.model.Task
import inc.dimasic.taskcal.domain.model.TaskCompletion
import inc.dimasic.taskcal.domain.model.UserProfile
import java.time.LocalDate

/**
 * The user's data as the backend stores it. Every failure is a [inc.dimasic.taskcal.data.remote.RemoteException].
 *
 * Saves are upserts: the app owns every id, so saving creates the item or replaces the one with
 * that id. Like the other repositories this stores and returns data and decides nothing; what to
 * do with it is for the use cases.
 */
interface RemoteRepository {

    /** Succeeds when the server is up; throws otherwise. */
    suspend fun health()

    /** Registers this install, or finds the account it already has, and returns the user id. */
    suspend fun registerDevice(): String

    suspend fun currentUserId(): String

    /** Deletes the account and everything stored for it. The device can register again afterwards. */
    suspend fun deleteAccount()

    /** Null until a profile has been saved. */
    suspend fun profile(): UserProfile?

    suspend fun saveProfile(profile: UserProfile): UserProfile

    /** Oldest first. */
    suspend fun tasks(): List<Task>

    /** Null when there is no such task. */
    suspend fun task(taskId: String): Task?

    suspend fun saveTask(task: Task): Task

    /** Also removes the task's completions. */
    suspend fun deleteTask(taskId: String)

    suspend fun completions(taskId: String): List<TaskCompletion>

    /** Only repeating tasks take completions, and each occurrence can be completed once. */
    suspend fun saveCompletion(completion: TaskCompletion): TaskCompletion

    suspend fun deleteCompletion(taskId: String, completionId: String)

    /** Ordered by date; both bounds are inclusive and optional. */
    suspend fun foodEntries(from: LocalDate? = null, to: LocalDate? = null): List<FoodEntry>

    /** Null when there is no such entry. */
    suspend fun foodEntry(entryId: String): FoodEntry?

    suspend fun saveFoodEntry(entry: FoodEntry): FoodEntry

    suspend fun deleteFoodEntry(entryId: String)
}