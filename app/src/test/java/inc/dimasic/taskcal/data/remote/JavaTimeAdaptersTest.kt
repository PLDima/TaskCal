package inc.dimasic.taskcal.data.remote

import inc.dimasic.taskcal.data.remote.model.FoodEntryDto
import inc.dimasic.taskcal.data.remote.model.ProblemDto
import inc.dimasic.taskcal.data.remote.model.TaskDto
import inc.dimasic.taskcal.domain.model.MealType
import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import com.squareup.moshi.Moshi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JavaTimeAdaptersTest {

    private val moshi = Moshi.Builder().add(JavaTimeAdapters()).build()
    private val taskAdapter = moshi.adapter(TaskDto::class.java)

    @Test
    fun `date-times are written with seconds even when they are zero`() {
        val task = TaskDto(
            title = "Gym",
            priority = Priority.MEDIUM,
            assignedDateTime = LocalDateTime.of(2026, 11, 9, 18, 0),
        )

        assertTrue(taskAdapter.toJson(task).contains("\"assignedDateTime\":\"2026-11-09T18:00:00\""))
    }

    @Test
    fun `date-times are read with or without seconds`() {
        val withSeconds = taskAdapter.fromJson(
            """{"title":"A","priority":"LOW","assignedDateTime":"2026-11-09T07:30:15"}""",
        )!!
        val withoutSeconds = taskAdapter.fromJson(
            """{"title":"A","priority":"LOW","assignedDateTime":"2026-11-09T07:30"}""",
        )!!

        assertEquals(LocalDateTime.of(2026, 11, 9, 7, 30, 15), withSeconds.assignedDateTime)
        assertEquals(LocalDateTime.of(2026, 11, 9, 7, 30), withoutSeconds.assignedDateTime)
    }

    @Test
    fun `weekdays and enums travel by name`() {
        val json = taskAdapter.toJson(
            TaskDto(
                title = "Gym",
                priority = Priority.HIGH,
                recurrenceType = RecurrenceType.WEEKLY,
                repeatDayOfWeek = DayOfWeek.FRIDAY,
            ),
        )

        assertTrue(json.contains("\"priority\":\"HIGH\""))
        assertTrue(json.contains("\"recurrenceType\":\"WEEKLY\""))
        assertTrue(json.contains("\"repeatDayOfWeek\":\"FRIDAY\""))
    }

    @Test
    fun `a task with only the required fields gets the server's defaults`() {
        val task = taskAdapter.fromJson("""{"title":"Gym","priority":"MEDIUM"}""")!!

        assertNull(task.id)
        assertEquals("", task.description)
        assertEquals(RecurrenceType.NONE, task.recurrenceType)
        assertNull(task.assignedDateTime)
        assertFalse(task.hasDueTime)
        assertFalse(task.isCompleted)
    }

    @Test
    fun `a food entry date is a plain date`() {
        val adapter = moshi.adapter(FoodEntryDto::class.java)
        val entry = FoodEntryDto(
            date = LocalDate.of(2026, 11, 9),
            mealType = MealType.BREAKFAST,
            calories = 350,
            protein = 12.5,
            carbs = 60.0,
            fat = 6.0,
            name = "Oatmeal",
        )

        val json = adapter.toJson(entry)

        assertTrue(json.contains("\"date\":\"2026-11-09\""))
        assertEquals(entry, adapter.fromJson(json))
    }

    @Test
    fun `an error body lists every broken rule`() {
        val problem = moshi.adapter(ProblemDto::class.java).fromJson(
            """{"type":"about:blank","title":"Validation failed","status":400,"detail":null,"errors":["a","b"]}""",
        )!!

        assertEquals(400, problem.status)
        assertEquals(listOf("a", "b"), problem.errors)
    }
}
