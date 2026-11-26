package inc.dimasic.taskcal.domain.usecase.food

import inc.dimasic.taskcal.domain.model.FoodEntry
import inc.dimasic.taskcal.domain.model.MealType
import inc.dimasic.taskcal.domain.model.NutritionPeriod
import inc.dimasic.taskcal.domain.usecase.model.MealGroup
import inc.dimasic.taskcal.domain.usecase.model.PeriodFoodEntries
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface FoodUseCase {
    suspend fun entriesForDate(date: LocalDate): List<FoodEntry>

    fun entriesForPeriod(period: Flow<NutritionPeriod>, offset: Flow<Int>): Flow<PeriodFoodEntries>

    suspend fun entriesForPeriod(period: NutritionPeriod, offset: Int): PeriodFoodEntries

    suspend fun saveEntry(
        id: String? = null,
        date: LocalDate,
        mealType: MealType,
        name: String,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
    )

    suspend fun deleteEntry(id: String)

    suspend fun mealsForDate(date: LocalDate): List<MealGroup>
}
