package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.repository.FoodRepository
import inc.dimasic.taskcal.domain.model.FoodEntry
import inc.dimasic.taskcal.domain.model.MealType
import inc.dimasic.taskcal.domain.model.NutritionPeriod
import inc.dimasic.taskcal.domain.usecase.model.MealGroup
import inc.dimasic.taskcal.domain.usecase.model.PeriodFoodEntries
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** The order meals are eaten in, and so the order they're listed in. */
private val MEAL_ORDER = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)

class FoodUseCase @Inject constructor(
    private val foodRepository: FoodRepository,
) {

    /** One-shot: the day screens load this alongside their other reads and show the lot together. */
    suspend fun entriesForDate(date: LocalDate): List<FoodEntry> =
        foodRepository.entriesForDate(date)

    fun entriesForPeriod(period: Flow<NutritionPeriod>, offset: Flow<Int>): Flow<PeriodFoodEntries> =
        combine(foodRepository.foodEntries, period, offset) { entries, selectedPeriod, selectedOffset ->
            val range = selectedPeriod.dateRangeAt(selectedOffset)
            PeriodFoodEntries(dateRange = range, entries = entries.filter { range.contains(it.date) })
        }

    /** One-shot sibling of [entriesForPeriod], for callers that load once per period/offset change. */
    suspend fun entriesForPeriod(period: NutritionPeriod, offset: Int): PeriodFoodEntries {
        val range = period.dateRangeAt(offset)
        val entries = foodRepository.foodEntries.first()
        return PeriodFoodEntries(dateRange = range, entries = entries.filter { range.contains(it.date) })
    }

    /**
     * Logs one item against [date], which is the day being viewed rather than necessarily today.
     *
     * A null [id] is a new entry and gets a fresh one; passing an existing id replaces that entry
     * instead, which is how editing works — the two differ only in whether the caller already has
     * an id to reuse.
     */
    suspend fun saveEntry(
        id: String? = null,
        date: LocalDate,
        mealType: MealType,
        name: String,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
    ) {
        val entry = FoodEntry(
            date = date,
            mealType = mealType,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            name = name,
        )
        foodRepository.saveEntry(if (id == null) entry else entry.copy(id = id))
    }

    suspend fun deleteEntry(id: String) {
        foodRepository.deleteEntry(id)
    }

    /** A day's entries grouped by meal, in eating order. Meals with nothing logged are left out. */
    suspend fun mealsForDate(date: LocalDate): List<MealGroup> {
        val byMeal = entriesForDate(date).groupBy { it.mealType }
        return MEAL_ORDER.mapNotNull { mealType ->
            byMeal[mealType]?.let { MealGroup(mealType = mealType, entries = it) }
        }
    }
}
