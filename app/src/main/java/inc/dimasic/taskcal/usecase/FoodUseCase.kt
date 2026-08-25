package inc.dimasic.taskcal.usecase

import inc.dimasic.taskcal.model.FoodEntry
import inc.dimasic.taskcal.model.NutritionPeriod
import inc.dimasic.taskcal.repository.FoodRepository
import inc.dimasic.taskcal.repository.PeriodFoodEntries
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class FoodUseCase @Inject constructor(
    private val foodRepository: FoodRepository,
) {

    fun entriesForDate(date: Flow<LocalDate>): Flow<List<FoodEntry>> =
        foodRepository.foodEntriesForDate(date)

    fun entriesForPeriod(period: Flow<NutritionPeriod>, offset: Flow<Int>): Flow<PeriodFoodEntries> =
        foodRepository.foodEntriesForPeriod(period, offset)
}
