package inc.dimasic.taskcal.ui.screens.statistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.model.DateRange
import inc.dimasic.taskcal.model.MacroType
import inc.dimasic.taskcal.model.NutritionPeriod
import inc.dimasic.taskcal.repository.PeriodFoodEntries
import inc.dimasic.taskcal.usecase.CaloriesChartPoint
import inc.dimasic.taskcal.usecase.CaloriesUseCase
import inc.dimasic.taskcal.usecase.FoodUseCase
import inc.dimasic.taskcal.usecase.MacroChartPoint
import inc.dimasic.taskcal.usecase.PeriodCaloriesAverages
import inc.dimasic.taskcal.usecase.PeriodCaloriesSummary
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val rangeLabelFormatter = DateTimeFormatter.ofPattern("MMM d")
private val summaryDayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

data class NutritionSummaryUi(
    val totalConsumed: Int,
    val totalBurned: Int,
    val avgBalance: Int,
    val bestDayLabel: String,
    val worstDayLabel: String,
) {
    companion object {
        val Empty = NutritionSummaryUi(0, 0, 0, "-", "-")
    }
}

@HiltViewModel
class NutritionStatisticViewModel @Inject constructor(
    private val foodUseCase: FoodUseCase,
    private val caloriesUseCase: CaloriesUseCase,
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(NutritionPeriod.WEEK)
    val selectedPeriod: StateFlow<NutritionPeriod> = _selectedPeriod.asStateFlow()

    private val _periodOffset = MutableStateFlow(0)

    val periodFoodEntries: StateFlow<PeriodFoodEntries> =
        foodUseCase.entriesForPeriod(_selectedPeriod, _periodOffset)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeriodFoodEntries.Empty)

    val dateRangeLabel: StateFlow<String> = periodFoodEntries
        .map { it.dateRange.toLabel() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeriodFoodEntries.Empty.dateRange.toLabel())

    val periodCaloriesAverages: StateFlow<PeriodCaloriesAverages> =
        caloriesUseCase.averagesForPeriod(_selectedPeriod, _periodOffset)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeriodCaloriesAverages.Empty)

    val caloriesChart: StateFlow<List<CaloriesChartPoint>> =
        caloriesUseCase.caloriesChartForPeriod(_selectedPeriod, _periodOffset)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val caloriesSummary: StateFlow<NutritionSummaryUi> =
        caloriesUseCase.summaryForPeriod(_selectedPeriod, _periodOffset)
            .map { it.toUi() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NutritionSummaryUi.Empty)

    private val _expandedMacro = MutableStateFlow<MacroType?>(null)
    val expandedMacro: StateFlow<MacroType?> = _expandedMacro.asStateFlow()

    val macroChart: StateFlow<List<MacroChartPoint>> = _expandedMacro
        .flatMapLatest { macro ->
            if (macro == null) {
                flowOf(emptyList())
            } else {
                caloriesUseCase.macroChartForPeriod(macro, _selectedPeriod, _periodOffset)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectPeriod(period: NutritionPeriod) {
        _selectedPeriod.value = period
        _periodOffset.value = 0
    }

    fun showPreviousPeriod() {
        _periodOffset.value -= 1
    }

    fun showNextPeriod() {
        _periodOffset.value += 1
    }

    fun toggleMacro(macro: MacroType) {
        _expandedMacro.value = if (_expandedMacro.value == macro) null else macro
    }
}

private fun DateRange.toLabel(): String =
    "${start.format(rangeLabelFormatter)} - ${end.format(rangeLabelFormatter)}"

private fun PeriodCaloriesSummary.toUi(): NutritionSummaryUi = NutritionSummaryUi(
    totalConsumed = totalConsumed,
    totalBurned = totalBurned,
    avgBalance = avgBalance,
    bestDayLabel = bestDay?.format(summaryDayFormatter) ?: "-",
    worstDayLabel = worstDay?.format(summaryDayFormatter) ?: "-",
)
