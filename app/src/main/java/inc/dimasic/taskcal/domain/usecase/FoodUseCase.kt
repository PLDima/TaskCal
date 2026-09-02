package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.data.repository.FoodRepository
import inc.dimasic.taskcal.data.model.PeriodFoodEntries
import inc.dimasic.taskcal.domain.model.FoodEntry
import inc.dimasic.taskcal.domain.model.NutritionPeriod
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
