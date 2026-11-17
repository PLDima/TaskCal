package inc.dimasic.taskcal.domain.usecase.calories

import inc.dimasic.taskcal.domain.model.MacroType
import inc.dimasic.taskcal.domain.model.NutritionPeriod
import inc.dimasic.taskcal.domain.usecase.model.CaloriesSummary
import inc.dimasic.taskcal.domain.usecase.model.ChartPoint
import inc.dimasic.taskcal.domain.usecase.model.PeriodStatisticsSummary
import java.time.LocalDate

interface CaloriesUseCase {
    suspend fun caloriesForDate(date: LocalDate): CaloriesSummary

    suspend fun periodSummary(period: NutritionPeriod, offset: Int): PeriodStatisticsSummary

    suspend fun macroChartForPeriod(macro: MacroType, period: NutritionPeriod, offset: Int): List<ChartPoint>
}