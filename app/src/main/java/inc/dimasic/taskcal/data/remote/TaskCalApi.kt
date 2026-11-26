package inc.dimasic.taskcal.data.remote

import inc.dimasic.taskcal.data.remote.model.DeviceAuthRequestDto
import inc.dimasic.taskcal.data.remote.model.FoodEntryDto
import inc.dimasic.taskcal.data.remote.model.TaskCompletionDto
import inc.dimasic.taskcal.data.remote.model.TaskDto
import inc.dimasic.taskcal.data.remote.model.UserDto
import inc.dimasic.taskcal.data.remote.model.UserProfileDto
import java.time.LocalDate
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The TaskCal backend. Paths are relative to the `/api/v1/` base URL; every call except [health]
 * and [registerDevice] needs the device token as a bearer token.
 *
 * Saves are upserts: the app owns every id, and a `PUT` creates the item or replaces it. Errors
 * surface as [retrofit2.HttpException] carrying a `problem+json` body.
 */
interface TaskCalApi {

    /** Outside the `/api/v1` prefix, hence the absolute path. Needs no token. */
    @GET("/health")
    suspend fun health()

    /** Idempotent, so it is safe to call on every launch. Needs no token. */
    @POST("auth/device")
    suspend fun registerDevice(@Body request: DeviceAuthRequestDto): UserDto

    @GET("me")
    suspend fun me(): UserDto

    /** Deletes the account and, with it, everything stored for it. */
    @DELETE("me")
    suspend fun deleteAccount()

    /** 404 until a profile has been saved. */
    @GET("me/profile")
    suspend fun profile(): UserProfileDto

    @PUT("me/profile")
    suspend fun saveProfile(@Body profile: UserProfileDto): UserProfileDto

    /** Oldest first. */
    @GET("tasks")
    suspend fun tasks(): List<TaskDto>

    @GET("tasks/{taskId}")
    suspend fun task(@Path("taskId") taskId: String): TaskDto

    @PUT("tasks/{taskId}")
    suspend fun saveTask(@Path("taskId") taskId: String, @Body task: TaskDto): TaskDto

    /** Also removes the task's completions. */
    @DELETE("tasks/{taskId}")
    suspend fun deleteTask(@Path("taskId") taskId: String)

    @GET("tasks/{taskId}/completions")
    suspend fun completions(@Path("taskId") taskId: String): List<TaskCompletionDto>

    /** 409 when the task doesn't repeat, or the occurrence is already completed. */
    @PUT("tasks/{taskId}/completions/{completionId}")
    suspend fun saveCompletion(
        @Path("taskId") taskId: String,
        @Path("completionId") completionId: String,
        @Body completion: TaskCompletionDto,
    ): TaskCompletionDto

    @DELETE("tasks/{taskId}/completions/{completionId}")
    suspend fun deleteCompletion(
        @Path("taskId") taskId: String,
        @Path("completionId") completionId: String,
    )

    /** Ordered by date. Both bounds are inclusive and optional. */
    @GET("food-entries")
    suspend fun foodEntries(
        @Query("from") from: LocalDate? = null,
        @Query("to") to: LocalDate? = null,
    ): List<FoodEntryDto>

    @GET("food-entries/{entryId}")
    suspend fun foodEntry(@Path("entryId") entryId: String): FoodEntryDto

    @PUT("food-entries/{entryId}")
    suspend fun saveFoodEntry(@Path("entryId") entryId: String, @Body entry: FoodEntryDto): FoodEntryDto

    @DELETE("food-entries/{entryId}")
    suspend fun deleteFoodEntry(@Path("entryId") entryId: String)
}