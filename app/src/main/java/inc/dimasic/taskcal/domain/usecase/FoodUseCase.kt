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
import kotlinx.coroutines.flow.map

/** The order meals are eaten in, and so the order they're listed in. */
private val MEAL_ORDER = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)

class FoodUseCase @Inject constructor(
    private val foodRepository: FoodRepository,
) {

    fun entriesForDate(date: Flow<LocalDate>): Flow<List<FoodEntry>> =
        combine(foodRepository.foodEntries, date) { entries, selectedDate ->
            entries.filter { it.date == selectedDate }
        }

    fun entriesForPeriod(period: Flow<NutritionPeriod>, offset: Flow<Int>): Flow<PeriodFoodEntries> =
        combine(foodRepository.foodEntries, period, offset) { entries, selectedPeriod, selectedOffset ->
            val range = selectedPeriod.dateRangeAt(selectedOffset)
            PeriodFoodEntries(dateRange = range, entries = entries.filter { range.contains(it.date) })
        }

    /** A day's entries grouped by meal, in eating order. Meals with nothing logged are left out. */
    fun mealsForDate(date: Flow<LocalDate>): Flow<List<MealGroup>> =
        entriesForDate(date).map { entries ->
            val byMeal = entries.groupBy { it.mealType }
            MEAL_ORDER.mapNotNull { mealType ->
                byMeal[mealType]?.let { MealGroup(mealType = mealType, entries = it) }
            }
        }
}
