package inc.dimasic.taskcal.repository

import inc.dimasic.taskcal.model.ui.DateRange
import inc.dimasic.taskcal.model.domain.FoodEntry
import inc.dimasic.taskcal.model.domain.MealType
import inc.dimasic.taskcal.model.ui.NutritionPeriod
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

data class PeriodFoodEntries(
    val dateRange: DateRange,
    val entries: List<FoodEntry>,
) {
    companion object {
        val Empty = PeriodFoodEntries(DateRange(LocalDate.now(), LocalDate.now()), emptyList())
    }
}

@Singleton
class FoodRepository @Inject constructor() {

    private val foodEntries = MutableStateFlow(mockFoodEntries())

    val allFoodEntries: Flow<List<FoodEntry>> = foodEntries

    fun foodEntriesForDate(date: Flow<LocalDate>): Flow<List<FoodEntry>> =
        combine(foodEntries, date) { entries, selectedDate ->
            entries.filter { it.date == selectedDate }
        }

    fun foodEntriesForPeriod(period: Flow<NutritionPeriod>, offset: Flow<Int>): Flow<PeriodFoodEntries> =
        combine(foodEntries, period, offset) { entries, selectedPeriod, selectedOffset ->
            val range = dateRangeFor(selectedPeriod, selectedOffset)
            PeriodFoodEntries(dateRange = range, entries = entries.filter { range.contains(it.date) })
        }

    private fun dateRangeFor(period: NutritionPeriod, offset: Int): DateRange {
        val today = LocalDate.now()
        return when (period) {
            NutritionPeriod.WEEK -> {
                val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(offset.toLong())
                DateRange(start = monday, end = monday.plusDays(6))
            }
            NutritionPeriod.MONTH -> {
                val firstOfMonth = today.with(TemporalAdjusters.firstDayOfMonth()).plusMonths(offset.toLong())
                DateRange(start = firstOfMonth, end = firstOfMonth.with(TemporalAdjusters.lastDayOfMonth()))
            }
        }
    }

    private fun mockFoodEntries(): List<FoodEntry> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        return listOf(
            FoodEntry(
                date = today,
                mealType = MealType.BREAKFAST,
                calories = 420,
                protein = 28.0,
                carbs = 45.0,
                fat = 14.0,
                note = "Oatmeal with eggs",
            ),
            FoodEntry(
                date = today,
                mealType = MealType.BREAKFAST,
                calories = 150,
                protein = 15.0,
                carbs = 12.0,
                fat = 4.0,
                note = "Greek yogurt",
            ),
            FoodEntry(
                date = today,
                mealType = MealType.LUNCH,
                calories = 680,
                protein = 45.0,
                carbs = 70.0,
                fat = 20.0,
                note = "Chicken bowl",
            ),
            FoodEntry(
                date = today,
                mealType = MealType.DINNER,
                calories = 540,
                protein = 35.0,
                carbs = 50.0,
                fat = 18.0,
                note = "Salmon with rice",
            ),
            FoodEntry(
                date = today,
                mealType = MealType.SNACK,
                calories = 210,
                protein = 20.0,
                carbs = 20.0,
                fat = 8.0,
                note = "Protein bar",
            ),
            FoodEntry(
                date = yesterday,
                mealType = MealType.BREAKFAST,
                calories = 380,
                protein = 22.0,
                carbs = 40.0,
                fat = 10.0,
                note = "Pancakes",
            ),
            FoodEntry(
                date = yesterday,
                mealType = MealType.LUNCH,
                calories = 610,
                protein = 38.0,
                carbs = 60.0,
                fat = 22.0,
                note = "Turkey sandwich",
            ),
            FoodEntry(
                date = yesterday,
                mealType = MealType.DINNER,
                calories = 590,
                protein = 40.0,
                carbs = 45.0,
                fat = 24.0,
                note = "Beef stir fry",
            ),
            FoodEntry(
                date = yesterday,
                mealType = MealType.SNACK,
                calories = 180,
                protein = 5.0,
                carbs = 18.0,
                fat = 9.0,
                note = "Trail mix",
            ),
        )
    }
}
