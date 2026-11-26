package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.database.FakeFoodEntryDao
import inc.dimasic.taskcal.data.local.database.FoodEntryEntity
import inc.dimasic.taskcal.data.repository.FoodRepository
import inc.dimasic.taskcal.domain.model.MealType
import inc.dimasic.taskcal.domain.model.NutritionPeriod
import inc.dimasic.taskcal.domain.usecase.food.FoodUseCaseImpl
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The two domain rules this use case owns: a day's entries are grouped by meal in eating order,
 * with meals nothing was logged for left out; and a period's entries are exactly the ones whose
 * date falls inside it.
 */
class FoodUseCaseTest {

    private val today = LocalDate.now()

    private fun useCase(entries: List<FoodEntryEntity>) = FoodUseCaseImpl(FoodRepository(FakeFoodEntryDao(entries)))

    @Test
    fun `meals come back in eating order regardless of how they were stored`() {
        val entries = listOf(
            entry("snack", today, MealType.SNACK),
            entry("dinner", today, MealType.DINNER),
            entry("breakfast", today, MealType.BREAKFAST),
            entry("lunch", today, MealType.LUNCH),
        )

        val meals = runBlocking { useCase(entries).mealsForDate(today) }

        assertEquals(
            listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK),
            meals.map { it.mealType },
        )
    }

    @Test
    fun `meals with nothing logged are left out rather than listed empty`() {
        val entries = listOf(
            entry("toast", today, MealType.BREAKFAST),
            entry("apple", today, MealType.SNACK),
            entry("crisps", today, MealType.SNACK),
        )

        val meals = runBlocking { useCase(entries).mealsForDate(today) }

        assertEquals(listOf(MealType.BREAKFAST, MealType.SNACK), meals.map { it.mealType })
        assertEquals(listOf("apple", "crisps"), meals[1].entries.map { it.id })
    }

    @Test
    fun `only the requested day's entries are grouped`() {
        val entries = listOf(
            entry("today", today, MealType.LUNCH),
            entry("yesterday", today.minusDays(1), MealType.LUNCH),
        )

        val meals = runBlocking { useCase(entries).mealsForDate(today) }

        assertEquals(1, meals.size)
        assertEquals(listOf("today"), meals.single().entries.map { it.id })
    }

    @Test
    fun `a day with nothing logged has no meals`() {
        assertTrue(runBlocking { useCase(emptyList()).mealsForDate(today) }.isEmpty())
    }

    @Test
    fun `period entries are filtered to the period's dates`() {
        val thisWeek = weekRangeAt(0)
        val entries = listOf(
            entry("first", thisWeek.start, MealType.LUNCH),
            entry("last", thisWeek.end, MealType.LUNCH),
            entry("before", thisWeek.start.minusDays(1), MealType.LUNCH),
            entry("after", thisWeek.end.plusDays(1), MealType.LUNCH),
        )

        val period = runBlocking { useCase(entries).entriesForPeriod(NutritionPeriod.WEEK, 0) }

        assertEquals(thisWeek, period.dateRange)
        assertEquals(listOf("first", "last"), period.entries.map { it.id })
    }

    private fun entry(id: String, date: LocalDate, mealType: MealType) = FoodEntryEntity(
        id = id,
        date = date.toString(),
        mealType = mealType.name,
        calories = 100,
        protein = 0.0,
        carbs = 0.0,
        fat = 0.0,
        name = id,
    )
}
