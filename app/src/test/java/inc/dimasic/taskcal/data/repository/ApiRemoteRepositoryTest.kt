package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.remote.FakeDeviceTokenStore
import inc.dimasic.taskcal.data.remote.FakeTaskCalApi
import inc.dimasic.taskcal.data.remote.JavaTimeAdapters
import inc.dimasic.taskcal.data.remote.RemoteException
import inc.dimasic.taskcal.domain.model.FoodEntry
import inc.dimasic.taskcal.domain.model.MealType
import inc.dimasic.taskcal.domain.model.NutritionGoal
import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.model.Task
import inc.dimasic.taskcal.domain.model.TaskCompletion
import inc.dimasic.taskcal.domain.model.UserProfile
import com.squareup.moshi.Moshi
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiRemoteRepositoryTest {

    private val api = FakeTaskCalApi()
    private val token = "k".repeat(40)
    private val repository = ApiRemoteRepository(
        api = api,
        tokens = FakeDeviceTokenStore(token),
        moshi = Moshi.Builder().add(JavaTimeAdapters()).build(),
    )

    private fun weeklyTask() = Task(
        title = "Gym",
        description = "",
        priority = Priority.MEDIUM,
        recurrenceType = RecurrenceType.WEEKLY,
        assignedDateTime = LocalDateTime.of(2026, 11, 9, 18, 0),
    )

    private fun oneOffTask() = Task(
        title = "Call",
        description = "",
        priority = Priority.LOW,
        recurrenceType = RecurrenceType.NONE,
    )

    private fun food(date: LocalDate, name: String = "Oatmeal") = FoodEntry(
        date = date,
        mealType = MealType.BREAKFAST,
        calories = 350,
        protein = 12.5,
        carbs = 60.0,
        fat = 6.0,
        name = name,
    )

    private fun profile() = UserProfile(
        name = "Dmytro",
        gender = null,
        dateOfBirth = null,
        heightCm = null,
        stepsGoal = 10_000,
        distanceGoalMeters = 5_000,
        caloriesBurnedGoal = 2_500,
        weightGoal = 68.0,
        nutritionGoal = NutritionGoal(calories = 3000, protein = 140.0, carbs = 250.0, fat = 70.0),
    )

    // Account

    @Test
    fun `registering sends this device's token and returns the user id`() = runBlocking {
        assertEquals(FakeTaskCalApi.USER_ID, repository.registerDevice())
        assertEquals(listOf(token), api.registeredTokens)
    }

    @Test
    fun `registering twice is safe`() = runBlocking {
        assertEquals(repository.registerDevice(), repository.registerDevice())
    }

    @Test
    fun `the health check and current user are reachable`() = runBlocking {
        repository.health()

        assertEquals(FakeTaskCalApi.USER_ID, repository.currentUserId())
    }

    @Test
    fun `deleting the account clears what was stored`() = runBlocking {
        repository.saveTask(oneOffTask())
        repository.saveProfile(profile())

        repository.deleteAccount()

        assertTrue(repository.tasks().isEmpty())
        assertNull(repository.profile())
    }

    // Profile

    @Test
    fun `there is no profile until one is saved`() = runBlocking {
        assertNull(repository.profile())

        repository.saveProfile(profile())

        assertEquals(profile(), repository.profile())
    }

    // Tasks

    @Test
    fun `a saved task is read back and listed`() = runBlocking {
        val task = weeklyTask()

        assertEquals(task, repository.saveTask(task))

        assertEquals(task, repository.task(task.id))
        assertEquals(listOf(task), repository.tasks())
    }

    @Test
    fun `saving a task again replaces it`() = runBlocking {
        val task = oneOffTask()
        repository.saveTask(task)

        repository.saveTask(task.copy(title = "Call back", isCompleted = true))

        assertEquals(listOf(task.copy(title = "Call back", isCompleted = true)), repository.tasks())
    }

    @Test
    fun `a task that doesn't exist reads as null`() = runBlocking {
        assertNull(repository.task("missing"))
    }

    @Test
    fun `deleting a task removes it`() = runBlocking {
        val task = oneOffTask()
        repository.saveTask(task)

        repository.deleteTask(task.id)

        assertNull(repository.task(task.id))
    }

    @Test
    fun `deleting a task that doesn't exist is not found`() {
        assertThrows(RemoteException.NotFound::class.java) {
            runBlocking { repository.deleteTask("missing") }
        }
    }

    @Test
    fun `rejected input carries every message the server gave`() {
        val error = assertThrows(RemoteException.Validation::class.java) {
            runBlocking { repository.saveTask(oneOffTask().copy(title = " ")) }
        }

        assertEquals(listOf("title can't be blank"), error.errors)
        assertEquals("title can't be blank", error.message)
    }

    // Completions

    @Test
    fun `completions of a repeating task are saved, listed and deleted`() = runBlocking {
        val task = repository.saveTask(weeklyTask())
        val completion = TaskCompletion(
            taskId = task.id,
            occurrenceDate = LocalDateTime.of(2026, 11, 9, 18, 0),
            completedAt = LocalDateTime.of(2026, 11, 9, 18, 45),
        )

        assertEquals(completion, repository.saveCompletion(completion))
        assertEquals(listOf(completion), repository.completions(task.id))

        repository.deleteCompletion(task.id, completion.id)

        assertTrue(repository.completions(task.id).isEmpty())
    }

    @Test
    fun `a one-off task can't be completed by occurrence`() {
        val task = oneOffTask()
        runBlocking { repository.saveTask(task) }

        assertThrows(RemoteException.Conflict::class.java) {
            runBlocking {
                repository.saveCompletion(
                    TaskCompletion(
                        taskId = task.id,
                        occurrenceDate = LocalDateTime.of(2026, 11, 9, 18, 0),
                        completedAt = LocalDateTime.of(2026, 11, 9, 18, 0),
                    ),
                )
            }
        }
    }

    @Test
    fun `the same occurrence can't be completed twice`() {
        val task = weeklyTask()
        val occurrence = LocalDateTime.of(2026, 11, 9, 18, 0)
        runBlocking {
            repository.saveTask(task)
            repository.saveCompletion(TaskCompletion(taskId = task.id, occurrenceDate = occurrence, completedAt = occurrence))
        }

        assertThrows(RemoteException.Conflict::class.java) {
            runBlocking {
                repository.saveCompletion(
                    TaskCompletion(taskId = task.id, occurrenceDate = occurrence, completedAt = occurrence),
                )
            }
        }
    }

    @Test
    fun `completions of an unknown task are not found`() {
        assertThrows(RemoteException.NotFound::class.java) {
            runBlocking { repository.completions("missing") }
        }
    }

    @Test
    fun `deleting a task takes its completions with it`() = runBlocking {
        val task = repository.saveTask(weeklyTask())
        val occurrence = LocalDateTime.of(2026, 11, 9, 18, 0)
        repository.saveCompletion(TaskCompletion(taskId = task.id, occurrenceDate = occurrence, completedAt = occurrence))

        repository.deleteTask(task.id)

        assertThrows(RemoteException.NotFound::class.java) {
            runBlocking { repository.completions(task.id) }
        }
        Unit
    }

    // Food

    @Test
    fun `food entries are filtered by inclusive bounds and ordered by date`() = runBlocking {
        val first = food(LocalDate.of(2026, 11, 8), "A")
        val second = food(LocalDate.of(2026, 11, 9), "B")
        val third = food(LocalDate.of(2026, 11, 10), "C")
        listOf(third, first, second).forEach { repository.saveFoodEntry(it) }

        assertEquals(listOf(first, second, third), repository.foodEntries())
        assertEquals(listOf(second, third), repository.foodEntries(from = second.date))
        assertEquals(listOf(first, second), repository.foodEntries(to = second.date))
        assertEquals(listOf(second), repository.foodEntries(from = second.date, to = second.date))
    }

    @Test
    fun `a range that ends before it starts is rejected`() {
        assertThrows(RemoteException.Validation::class.java) {
            runBlocking { repository.foodEntries(from = LocalDate.of(2026, 11, 10), to = LocalDate.of(2026, 11, 9)) }
        }
    }

    @Test
    fun `a food entry is saved, read, replaced and deleted`() = runBlocking {
        val entry = food(LocalDate.of(2026, 11, 9))

        assertEquals(entry, repository.saveFoodEntry(entry))
        assertEquals(entry, repository.foodEntry(entry.id))

        repository.saveFoodEntry(entry.copy(calories = 500))
        assertEquals(500, repository.foodEntry(entry.id)!!.calories)

        repository.deleteFoodEntry(entry.id)
        assertNull(repository.foodEntry(entry.id))
    }

    // Failures

    @Test
    fun `an unreachable server is a network failure`() {
        api.failWith = IOException("offline")

        val error = assertThrows(RemoteException.Network::class.java) { runBlocking { repository.tasks() } }

        assertEquals("offline", error.cause?.message)
    }

    @Test
    fun `an unknown token is unauthorized`() {
        api.unauthorized = true

        assertThrows(RemoteException.Unauthorized::class.java) { runBlocking { repository.tasks() } }
        assertThrows(RemoteException.Unauthorized::class.java) { runBlocking { repository.profile() } }
    }

    @Test
    fun `registration failing offline is a network failure too`() {
        api.failWith = IOException("offline")

        assertThrows(RemoteException.Network::class.java) { runBlocking { repository.registerDevice() } }
    }
}
