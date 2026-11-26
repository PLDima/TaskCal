package inc.dimasic.taskcal.data.remote

import inc.dimasic.taskcal.data.remote.model.TaskCompletionDto
import inc.dimasic.taskcal.data.remote.model.TaskDto
import inc.dimasic.taskcal.data.remote.model.toDomain
import inc.dimasic.taskcal.data.remote.model.toDto
import inc.dimasic.taskcal.domain.model.FoodEntry
import inc.dimasic.taskcal.domain.model.Gender
import inc.dimasic.taskcal.domain.model.MealType
import inc.dimasic.taskcal.domain.model.NutritionGoal
import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.model.Task
import inc.dimasic.taskcal.domain.model.TaskCompletion
import inc.dimasic.taskcal.domain.model.UserProfile
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RemoteMappingTest {

    @Test
    fun `a repeating task survives the round trip`() {
        val task = Task(
            title = "Gym",
            description = "Legs",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.WEEKLY,
            repeatDayOfWeek = DayOfWeek.MONDAY,
            assignedDateTime = LocalDateTime.of(2026, 11, 9, 18, 0),
            hasDueTime = true,
        )

        assertEquals(task, task.toDto().toDomain())
    }

    @Test
    fun `a monthly task keeps its day of month`() {
        val task = Task(
            title = "Rent",
            description = "",
            priority = Priority.LOW,
            recurrenceType = RecurrenceType.MONTHLY,
            repeatDayOfMonth = 31,
            assignedDateTime = LocalDateTime.of(2026, 1, 31, 9, 0),
        )

        assertEquals(task, task.toDto().toDomain())
    }

    @Test
    fun `a done one-off task keeps its state`() {
        val task = Task(
            title = "Call",
            description = "",
            priority = Priority.LOW,
            recurrenceType = RecurrenceType.NONE,
            isCompleted = true,
        )

        assertEquals(task, task.toDto().toDomain())
    }

    @Test
    fun `a task from the server must carry an id`() {
        val dto = TaskDto(title = "Gym", priority = Priority.LOW)

        assertThrows(IllegalArgumentException::class.java) { dto.toDomain() }
    }

    @Test
    fun `a completion survives the round trip`() {
        val completion = TaskCompletion(
            taskId = "task-1",
            occurrenceDate = LocalDateTime.of(2026, 11, 9, 18, 0),
            completedAt = LocalDateTime.of(2026, 11, 9, 18, 45),
        )

        assertEquals(completion, completion.toDto().toDomain())
    }

    @Test
    fun `a completion from the server must carry both ids`() {
        val noTask = TaskCompletionDto(
            id = "c",
            occurrenceDate = LocalDateTime.of(2026, 11, 9, 18, 0),
            completedAt = LocalDateTime.of(2026, 11, 9, 18, 0),
        )

        assertThrows(IllegalArgumentException::class.java) { noTask.toDomain() }
        assertThrows(IllegalArgumentException::class.java) { noTask.copy(id = null, taskId = "t").toDomain() }
    }

    @Test
    fun `a food entry survives the round trip`() {
        val entry = FoodEntry(
            date = LocalDate.of(2026, 11, 9),
            mealType = MealType.SNACK,
            calories = 120,
            protein = 3.5,
            carbs = 20.0,
            fat = 2.25,
            name = "Bar",
        )

        assertEquals(entry, entry.toDto().toDomain())
    }

    @Test
    fun `a profile survives the round trip, with and without its optional details`() {
        val full = UserProfile(
            name = "Dmytro",
            gender = Gender.MALE,
            dateOfBirth = LocalDate.of(1995, 4, 12),
            heightCm = 180,
            stepsGoal = 10_000,
            distanceGoalMeters = 5_000,
            caloriesBurnedGoal = 2_500,
            weightGoal = 68.0,
            nutritionGoal = NutritionGoal(calories = 3000, protein = 140.0, carbs = 250.0, fat = 70.0),
        )
        val bare = full.copy(gender = null, dateOfBirth = null, heightCm = null)

        assertEquals(full, full.toDto().toDomain())
        assertEquals(bare, bare.toDto().toDomain())
    }
}
