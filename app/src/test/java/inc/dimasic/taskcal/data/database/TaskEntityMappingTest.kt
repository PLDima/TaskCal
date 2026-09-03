package inc.dimasic.taskcal.data.database

import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.model.Task
import inc.dimasic.taskcal.domain.model.TaskCompletion
import java.time.DayOfWeek
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The entity <-> domain mappers are the only new logic that can be wrong, so they get a
 * round-trip test each. Room itself is not exercised here.
 */
class TaskEntityMappingTest {

    private val anchor = LocalDateTime.of(2026, 3, 14, 9, 30)

    @Test
    fun `one-off task round-trips`() {
        val task = Task(
            title = "Renew passport",
            description = "Before the slot closes",
            priority = Priority.HIGH,
            recurrenceType = RecurrenceType.NONE,
            assignedDateTime = anchor,
            isCompleted = true,
        )

        assertEquals(task, task.toEntity().toDomain())
    }

    @Test
    fun `undated backlog task round-trips`() {
        val task = Task(
            title = "Backup laptop",
            description = "",
            priority = Priority.LOW,
            recurrenceType = RecurrenceType.NONE,
            assignedDateTime = null,
            isCompleted = false,
        )

        assertEquals(task, task.toEntity().toDomain())
    }

    @Test
    fun `weekly task keeps its repeat weekday`() {
        val task = Task(
            title = "Submit timesheet",
            description = "Log hours",
            priority = Priority.MEDIUM,
            recurrenceType = RecurrenceType.WEEKLY,
            repeatDayOfWeek = DayOfWeek.FRIDAY,
            assignedDateTime = anchor,
        )

        val restored = task.toEntity().toDomain()

        assertEquals(task, restored)
        assertEquals(DayOfWeek.FRIDAY, restored.repeatDayOfWeek)
    }

    @Test
    fun `monthly task keeps its repeat day-of-month`() {
        val task = Task(
            title = "Pay utility bill",
            description = "Electricity",
            priority = Priority.MEDIUM,
            recurrenceType = RecurrenceType.MONTHLY,
            repeatDayOfMonth = 28,
            assignedDateTime = anchor,
        )

        assertEquals(task, task.toEntity().toDomain())
    }

    @Test
    fun `every priority and recurrence type round-trips`() {
        for (priority in Priority.entries) {
            for (recurrence in RecurrenceType.entries) {
                val task = Task(
                    title = "$priority / $recurrence",
                    description = "",
                    priority = priority,
                    recurrenceType = recurrence,
                    assignedDateTime = anchor,
                )
                assertEquals(task, task.toEntity().toDomain())
            }
        }
    }

    @Test
    fun `task id is preserved, not regenerated`() {
        val task = Task(
            title = "x",
            description = "",
            priority = Priority.LOW,
            recurrenceType = RecurrenceType.NONE,
        )

        assertEquals(task.id, task.toEntity().toDomain().id)
    }

    @Test
    fun `completion round-trips`() {
        val completion = TaskCompletion(
            taskId = "task-42",
            occurrenceDate = anchor,
            completedAt = anchor.plusHours(2),
        )

        assertEquals(completion, completion.toEntity().toDomain())
    }
}
