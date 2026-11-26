package inc.dimasic.taskcal.data.database

import inc.dimasic.taskcal.data.local.database.toDomain
import inc.dimasic.taskcal.data.local.database.toEntity
import inc.dimasic.taskcal.domain.model.FoodEntry
import inc.dimasic.taskcal.domain.model.MealType
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The entity <-> domain mappers are the only new logic that can be wrong, so they get a round-trip
 * test each. Room itself is not exercised here.
 */
class FoodEntryMappingTest {

    private val date = LocalDate.of(2026, 3, 14)

    @Test
    fun `entry round-trips`() {
        val entry = FoodEntry(
            date = date,
            mealType = MealType.LUNCH,
            calories = 680,
            protein = 45.5,
            carbs = 70.25,
            fat = 20.75,
            name = "Chicken bowl",
        )

        assertEquals(entry, entry.toEntity().toDomain())
    }

    @Test
    fun `every meal type round-trips`() {
        for (mealType in MealType.entries) {
            val entry = FoodEntry(
                date = date,
                mealType = mealType,
                calories = 100,
                protein = 1.0,
                carbs = 2.0,
                fat = 3.0,
                name = mealType.label,
            )
            assertEquals(entry, entry.toEntity().toDomain())
        }
    }

    @Test
    fun `an entry with no name round-trips`() {
        val entry = FoodEntry(
            date = date,
            mealType = MealType.SNACK,
            calories = 0,
            protein = 0.0,
            carbs = 0.0,
            fat = 0.0,
            name = "",
        )

        assertEquals(entry, entry.toEntity().toDomain())
    }

    @Test
    fun `entry id is preserved, not regenerated`() {
        val entry = FoodEntry(
            date = date,
            mealType = MealType.BREAKFAST,
            calories = 420,
            protein = 28.0,
            carbs = 45.0,
            fat = 14.0,
            name = "Oatmeal",
        )

        assertEquals(entry.id, entry.toEntity().toDomain().id)
    }

    @Test
    fun `the date survives as an ISO-8601 string`() {
        val entry = FoodEntry(
            date = date,
            mealType = MealType.DINNER,
            calories = 540,
            protein = 35.0,
            carbs = 50.0,
            fat = 18.0,
            name = "Salmon",
        )

        assertEquals("2026-03-14", entry.toEntity().date)
        assertEquals(date, entry.toEntity().toDomain().date)
    }
}
