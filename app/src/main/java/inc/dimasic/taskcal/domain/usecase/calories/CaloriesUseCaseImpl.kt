package inc.dimasic.taskcal.domain.usecase.calories

import inc.dimasic.taskcal.data.repository.ActivitiesRepository
import inc.dimasic.taskcal.data.repository.SettingsRepository
import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.MacroType
import inc.dimasic.taskcal.domain.model.NutritionPeriod
import inc.dimasic.taskcal.domain.usecase.dateRangeAt
import inc.dimasic.taskcal.domain.usecase.food.FoodUseCase
import inc.dimasic.taskcal.domain.usecase.model.CaloriesSummary
import inc.dimasic.taskcal.domain.usecase.model.ChartPoint
import inc.dimasic.taskcal.domain.usecase.model.PeriodCaloriesAverages
import inc.dimasic.taskcal.domain.usecase.model.PeriodCaloriesSummary
import inc.dimasic.taskcal.domain.usecase.model.PeriodFoodEntries
import inc.dimasic.taskcal.domain.usecase.model.PeriodStatisticsSummary
import inc.dimasic.taskcal.domain.usecase.monthChartBuckets
import inc.dimasic.taskcal.domain.usecase.weekChartBuckets
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first


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

class CaloriesUseCaseImpl @Inject constructor(
    private val foodUseCase: FoodUseCase,
    private val activitiesRepository: ActivitiesRepository,
    settingsRepository: SettingsRepository,
) : CaloriesUseCase {

    private val userProfile = settingsRepository.userProfile

    /**
     * One-shot, so a screen can load this together with everything else it shows. The Health
     * Connect read runs alongside the local ones rather than behind them — it is the slow one.
     */
    override suspend fun caloriesForDate(date: LocalDate): CaloriesSummary = coroutineScope {
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

    /**
     * Everything the Nutrition statistics screen's main content needs for one period, in one call.
     *
     * The three reads run in parallel: the food entries are local, [ActivitiesRepository.activityTotals]
     * gives the period's true total burned (used for the top-line total/averages), and
     * [ActivitiesRepository.dailyCaloriesBurned] gives the day-by-day breakdown that best/worst-day
     * and the chart bars need — those can't be answered from a single range total.
     */
    override suspend fun periodSummary(period: NutritionPeriod, offset: Int): PeriodStatisticsSummary = coroutineScope {
        val range = period.dateRangeAt(offset)
        val periodEntriesDeferred = async { foodUseCase.entriesForPeriod(period, offset) }
        val totalBurnedDeferred = async { activitiesRepository.activityTotals(range).caloriesBurned }
        val dailyBurnedDeferred = async { activitiesRepository.dailyCaloriesBurned(range) }

        val periodEntries = periodEntriesDeferred.await()
        val totalBurned = totalBurnedDeferred.await()
        val dailyBurned = dailyBurnedDeferred.await()

        val buckets = period.buckets(periodEntries, dailyBurned)

        PeriodStatisticsSummary(
            dateRange = range,
            averages = periodEntries.toAverages(dailyBurned),
            summary = periodEntries.toSummary(totalBurned, dailyBurned, range),
            caloriesChart = buckets.map {
                ChartPoint(label = it.label, values = listOf(it.consumedCalories, it.burnedCalories))
            },
        )
    }

    /**
     * On demand, only once a macro's chart is actually asked for. Room-only — no Health Connect
     * read, since protein/fat/carbs are entirely consumed-side figures.
     */
    override suspend fun macroChartForPeriod(macro: MacroType, period: NutritionPeriod, offset: Int): List<ChartPoint> {
        val periodEntries = foodUseCase.entriesForPeriod(period, offset)
        return period.buckets(periodEntries, dailyBurned = emptyMap()).map {
            ChartPoint(label = it.label, values = listOf(macro.valueFrom(it).roundToInt()))
        }
    }

    /**
     * Every day/bucket keeps its slot, so the chart's weekday/bucket positions stay fixed. A day
     * nothing was logged for computes to a zero bucket — the chart renderer already skips drawing
     * any bar whose height comes out to zero, so that reads as an empty line, not a dropped one.
     */
    private fun NutritionPeriod.buckets(
        periodEntries: PeriodFoodEntries,
        dailyBurned: Map<LocalDate, Int>,
    ): List<NutritionChartBucket> = when (this) {
        NutritionPeriod.WEEK -> weekBuckets(periodEntries, dailyBurned)
        NutritionPeriod.MONTH -> monthBuckets(periodEntries, dailyBurned)
    }

    /**
     * Averaged only over days at least one item was logged for — a day nothing was logged for
     * would otherwise dilute the average down, reading as if less was eaten than it actually was
     * on the days that were tracked. Burned figures for the same untracked days are left out of
     * [avgBurned] for the same reason, so the two averages stay comparable.
     */
    private fun PeriodFoodEntries.toAverages(dailyBurned: Map<LocalDate, Int>): PeriodCaloriesAverages {
        val consumedByDate = entries.groupBy { it.date }
            .mapValues { (_, dayEntries) -> dayEntries.sumOf { it.calories } }
            .filterValues { it > 0 }
        val trackedDays = consumedByDate.size
        if (trackedDays <= 0) return PeriodCaloriesAverages.Empty

        val avgConsumed = consumedByDate.values.sum() / trackedDays
        val avgBurned = consumedByDate.keys.sumOf { dailyBurned[it] ?: 0 } / trackedDays
        return PeriodCaloriesAverages(
            avgConsumed = avgConsumed,
            avgBurned = avgBurned,
            avgBalance = avgConsumed - avgBurned,
        )
    }

    /**
     * Best/worst day only compares days Health Connect actually reported a figure for — a day
     * with nothing recorded is left out of the comparison rather than assumed to have burned
     * nothing, which would otherwise make an un-synced day look like the worst one.
     */
    private fun PeriodFoodEntries.toSummary(
        totalBurned: Int,
        dailyBurned: Map<LocalDate, Int>,
        range: DateRange,
    ): PeriodCaloriesSummary {
        val days = range.dayCount
        if (days <= 0) return PeriodCaloriesSummary.Empty

        val consumedByDate = entries.groupBy { it.date }.mapValues { (_, dayEntries) -> dayEntries.sumOf { it.calories } }
        val dailyBalances = dailyBurned.map { (date, burned) -> date to ((consumedByDate[date] ?: 0) - burned) }
        val bestDayEntry = dailyBalances.minByOrNull { (_, balance) -> balance }
        val worstDayEntry = dailyBalances.maxByOrNull { (_, balance) -> balance }

        val totalConsumed = entries.sumOf { it.calories }
        return PeriodCaloriesSummary(
            totalConsumed = totalConsumed,
            totalBurned = totalBurned,
            avgBalance = (totalConsumed - totalBurned) / days,
            bestDay = bestDayEntry?.first,
            bestDayBalance = bestDayEntry?.second ?: 0,
            worstDay = worstDayEntry?.first,
            worstDayBalance = worstDayEntry?.second ?: 0,
        )
    }

    private fun weekBuckets(
        periodEntries: PeriodFoodEntries,
        dailyBurned: Map<LocalDate, Int>,
    ): List<NutritionChartBucket> {
        val entriesByDate = periodEntries.entries.groupBy { it.date }
        return periodEntries.dateRange.weekChartBuckets().map { bucket ->
            val date = bucket.range.start
            val dayEntries = entriesByDate[date].orEmpty()
            NutritionChartBucket(
                label = bucket.label,
                consumedCalories = dayEntries.sumOf { it.calories },
                burnedCalories = dailyBurned[date] ?: 0,
                protein = dayEntries.sumOf { it.protein },
                fat = dayEntries.sumOf { it.fat },
                carbs = dayEntries.sumOf { it.carbs },
            )
        }
    }

    private fun monthBuckets(
        periodEntries: PeriodFoodEntries,
        dailyBurned: Map<LocalDate, Int>,
    ): List<NutritionChartBucket> {
        val trackedDatesInPeriod = periodEntries.entries.groupBy { it.date }
            .filterValues { dayEntries -> dayEntries.sumOf { it.calories } > 0 }
            .keys

        return periodEntries.dateRange.monthChartBuckets().map { bucket ->
            // Only the days within this bucket something was logged for count toward its average
            // — the same rule the period-level averages follow, applied per bucket.
            val trackedDates = bucket.range.dates().filter { it in trackedDatesInPeriod }
            val trackedCount = trackedDates.size
            val bucketEntries = periodEntries.entries.filter { it.date in trackedDates }

            NutritionChartBucket(
                label = bucket.label,
                consumedCalories = if (trackedCount > 0) bucketEntries.sumOf { it.calories } / trackedCount else 0,
                burnedCalories = if (trackedCount > 0) trackedDates.sumOf { dailyBurned[it] ?: 0 } / trackedCount else 0,
                protein = if (trackedCount > 0) bucketEntries.sumOf { it.protein } / trackedCount else 0.0,
                fat = if (trackedCount > 0) bucketEntries.sumOf { it.fat } / trackedCount else 0.0,
                carbs = if (trackedCount > 0) bucketEntries.sumOf { it.carbs } / trackedCount else 0.0,
            )
        }
    }
}