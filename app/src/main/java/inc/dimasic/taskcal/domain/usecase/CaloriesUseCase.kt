package inc.dimasic.taskcal.domain.usecase

import inc.dimasic.taskcal.domain.usecase.model.PeriodFoodEntries
import inc.dimasic.taskcal.data.repository.ActivitiesRepository
import inc.dimasic.taskcal.data.repository.SettingsRepository
import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.MacroType
import inc.dimasic.taskcal.domain.model.NutritionPeriod
import inc.dimasic.taskcal.domain.usecase.model.ChartPoint
import inc.dimasic.taskcal.domain.usecase.model.CaloriesSummary
import inc.dimasic.taskcal.domain.usecase.model.PeriodCaloriesAverages
import inc.dimasic.taskcal.domain.usecase.model.PeriodCaloriesSummary
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val WEEKDAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private const val MONTH_CHART_BUCKETS = 5

// Still mocked, and only for the period paths below. Those need burned calories *per day* — the
// charts plot a value per bucket and the summary picks a best and worst day — but Health Connect is
// now read as one total per range, with no per-day breakdown. Wiring these up needs that breakdown
// back, which is its own decision. The per-date path no longer uses any of this.
private val MOCK_CALORIES_BURNED_BY_WEEKDAY = listOf(2400, 2700, 2550, 2950, 2650, 3200, 2350)

private fun mockCaloriesBurnedForDate(date: LocalDate): Int =
    MOCK_CALORIES_BURNED_BY_WEEKDAY[date.dayOfWeek.value - 1]

private fun mockCaloriesBurnedForRange(range: DateRange): Int {
    if (range.dayCount <= 0) return 0
    val total = (0 until range.dayCount).sumOf { mockCaloriesBurnedForDate(range.start.plusDays(it.toLong())) }
    return total / range.dayCount
}

private data class NutritionChartBucket(
    val label: String,
    val consumedCalories: Int,
    val burnedCalories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
)

private fun MacroType.valueFrom(bucket: NutritionChartBucket): Double = when (this) {
    MacroType.PROTEIN -> bucket.protein
    MacroType.FAT -> bucket.fat
    MacroType.CARBS -> bucket.carbs
}

class CaloriesUseCase @Inject constructor(
    private val foodUseCase: FoodUseCase,
    private val activitiesRepository: ActivitiesRepository,
    settingsRepository: SettingsRepository,
) {

    private val userProfile = settingsRepository.userProfile

    /**
     * One-shot, so a screen can load this together with everything else it shows. The Health
     * Connect read runs alongside the local ones rather than behind them — it is the slow one.
     */
    suspend fun caloriesForDate(date: LocalDate): CaloriesSummary = coroutineScope {
        val burned = async { activitiesRepository.activityTotals(DateRange(date, date)).caloriesBurned }
        val entries = async { foodUseCase.entriesForDate(date) }
        val goal = userProfile.first().nutritionGoal

        val entriesForDate = entries.await()
        CaloriesSummary(
            caloriesUsed = entriesForDate.sumOf { it.calories },
            caloriesGoal = goal.calories,
            caloriesBurned = burned.await(),
            proteinUsed = entriesForDate.sumOf { it.protein },
            proteinGoal = goal.protein,
            carbsUsed = entriesForDate.sumOf { it.carbs },
            carbsGoal = goal.carbs,
            fatUsed = entriesForDate.sumOf { it.fat },
            fatGoal = goal.fat,
        )
    }

    fun averagesForPeriod(period: Flow<NutritionPeriod>, offset: Flow<Int>): Flow<PeriodCaloriesAverages> =
        foodUseCase.entriesForPeriod(period, offset).map { periodEntries ->
            val days = periodEntries.dateRange.dayCount
            val avgConsumed = if (days > 0) periodEntries.entries.sumOf { it.calories } / days else 0
            val avgBurned = mockCaloriesBurnedForRange(periodEntries.dateRange)
            PeriodCaloriesAverages(
                avgConsumed = avgConsumed,
                avgBurned = avgBurned,
                avgBalance = avgConsumed - avgBurned,
            )
        }

    fun summaryForPeriod(period: Flow<NutritionPeriod>, offset: Flow<Int>): Flow<PeriodCaloriesSummary> =
        foodUseCase.entriesForPeriod(period, offset).map { periodEntries ->
            val range = periodEntries.dateRange
            val days = range.dayCount
            if (days <= 0) return@map PeriodCaloriesSummary.Empty

            val caloriesByDate = periodEntries.entries.groupBy { it.date }
                .mapValues { (_, entries) -> entries.sumOf { it.calories } }
            val dailyBalances = (0 until days).map {
                val date = range.start.plusDays(it.toLong())
                val consumed = caloriesByDate[date] ?: 0
                val burned = mockCaloriesBurnedForDate(date)
                date to (consumed - burned)
            }

            val totalConsumed = periodEntries.entries.sumOf { it.calories }
            val totalBurned = dailyBalances.sumOf { (date, _) -> mockCaloriesBurnedForDate(date) }
            val bestDayEntry = dailyBalances.minByOrNull { (_, balance) -> balance }
            val worstDayEntry = dailyBalances.maxByOrNull { (_, balance) -> balance }
            PeriodCaloriesSummary(
                totalConsumed = totalConsumed,
                totalBurned = totalBurned,
                avgBalance = (totalConsumed - totalBurned) / days,
                bestDay = bestDayEntry?.first,
                bestDayBalance = bestDayEntry?.second ?: 0,
                worstDay = worstDayEntry?.first,
                worstDayBalance = worstDayEntry?.second ?: 0,
            )
        }

    /** Each point carries consumed first, then burned. */
    fun caloriesChartForPeriod(period: Flow<NutritionPeriod>, offset: Flow<Int>): Flow<List<ChartPoint>> =
        nutritionChartForPeriod(period, offset).map { buckets ->
            buckets.map { ChartPoint(label = it.label, values = listOf(it.consumedCalories, it.burnedCalories)) }
        }

    fun macroChartForPeriod(
        macro: MacroType,
        period: Flow<NutritionPeriod>,
        offset: Flow<Int>,
    ): Flow<List<ChartPoint>> =
        nutritionChartForPeriod(period, offset).map { buckets ->
            buckets.map { ChartPoint(label = it.label, values = listOf(macro.valueFrom(it).roundToInt())) }
        }

    private fun nutritionChartForPeriod(
        period: Flow<NutritionPeriod>,
        offset: Flow<Int>,
    ): Flow<List<NutritionChartBucket>> =
        combine(foodUseCase.entriesForPeriod(period, offset), period) { periodEntries, selectedPeriod ->
            when (selectedPeriod) {
                NutritionPeriod.WEEK -> weekBuckets(periodEntries)
                NutritionPeriod.MONTH -> monthBuckets(periodEntries)
            }
        }

    private fun weekBuckets(periodEntries: PeriodFoodEntries): List<NutritionChartBucket> {
        val entriesByDate = periodEntries.entries.groupBy { it.date }
        return WEEKDAY_LABELS.mapIndexed { index, label ->
            val date = periodEntries.dateRange.start.plusDays(index.toLong())
            val dayEntries = entriesByDate[date].orEmpty()
            NutritionChartBucket(
                label = label,
                consumedCalories = dayEntries.sumOf { it.calories },
                burnedCalories = mockCaloriesBurnedForDate(date),
                protein = dayEntries.sumOf { it.protein },
                fat = dayEntries.sumOf { it.fat },
                carbs = dayEntries.sumOf { it.carbs },
            )
        }
    }

    private fun monthBuckets(periodEntries: PeriodFoodEntries): List<NutritionChartBucket> {
        val totalDays = periodEntries.dateRange.dayCount
        val bucketSize = ceil(totalDays / MONTH_CHART_BUCKETS.toDouble()).toInt().coerceAtLeast(1)
        return (0 until MONTH_CHART_BUCKETS).map { bucketIndex ->
            val bucketStart = periodEntries.dateRange.start.plusDays((bucketIndex * bucketSize).toLong())
            val bucketEnd = minOf(bucketStart.plusDays((bucketSize - 1).toLong()), periodEntries.dateRange.end)
            val bucketDays = ChronoUnit.DAYS.between(bucketStart, bucketEnd).toInt() + 1
            val bucketEntries = periodEntries.entries
                .filter { !it.date.isBefore(bucketStart) && !it.date.isAfter(bucketEnd) }
            NutritionChartBucket(
                label = "W${bucketIndex + 1}",
                consumedCalories = if (bucketDays > 0) bucketEntries.sumOf { it.calories } / bucketDays else 0,
                burnedCalories = mockCaloriesBurnedForRange(DateRange(bucketStart, bucketEnd)),
                protein = if (bucketDays > 0) bucketEntries.sumOf { it.protein } / bucketDays else 0.0,
                fat = if (bucketDays > 0) bucketEntries.sumOf { it.fat } / bucketDays else 0.0,
                carbs = if (bucketDays > 0) bucketEntries.sumOf { it.carbs } / bucketDays else 0.0,
            )
        }
    }
}
