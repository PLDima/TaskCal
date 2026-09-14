package inc.dimasic.taskcal.ui.screens.statistic.nutrition

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.common.CommonViewModel
import inc.dimasic.taskcal.domain.model.MacroType
import inc.dimasic.taskcal.domain.model.NutritionPeriod
import inc.dimasic.taskcal.domain.usecase.CaloriesUseCase
import inc.dimasic.taskcal.ui.screens.statistic.statistic_views.toLabel
import inc.dimasic.taskcal.domain.usecase.model.ChartPoint
import inc.dimasic.taskcal.domain.usecase.model.PeriodCaloriesSummary
import inc.dimasic.taskcal.domain.usecase.model.PeriodStatisticsSummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val summaryDayFormatter = DateTimeFormatter.ofPattern("MMM d")

@HiltViewModel
class NutritionStatisticViewModel @Inject constructor(
    private val caloriesUseCase: CaloriesUseCase,
) : CommonViewModel<NutritionPeriodUi>() {

    private val _selectedPeriod = MutableStateFlow(NutritionPeriod.WEEK)
    val selectedPeriod: StateFlow<NutritionPeriod> = _selectedPeriod.asStateFlow()

    private var periodOffset = 0

    /**
     * The macro breakdown is a second, independent concern from the main content above: it's
     * Room-only with no realistic failure worth the full Loading/Success/Failure machinery, so it
     * gets its own small state instead of sharing `CommonViewModel`'s single request slot.
     */
    private val _expandedMacro = MutableStateFlow<MacroType?>(null)
    val expandedMacro: StateFlow<MacroType?> = _expandedMacro.asStateFlow()

    private val _isMacroChartLoading = MutableStateFlow(false)
    val isMacroChartLoading: StateFlow<Boolean> = _isMacroChartLoading.asStateFlow()

    private val _macroChart = MutableStateFlow<List<ChartPoint>>(emptyList())
    val macroChart: StateFlow<List<ChartPoint>> = _macroChart.asStateFlow()

    private var macroJob: Job? = null

    init {
        reload()
    }

    fun selectPeriod(period: NutritionPeriod) {
        _selectedPeriod.value = period
        periodOffset = 0
        reload()
        reloadExpandedMacro()
    }

    fun showPreviousPeriod() {
        periodOffset -= 1
        reload()
        reloadExpandedMacro()
    }

    fun showNextPeriod() {
        periodOffset += 1
        reload()
        reloadExpandedMacro()
    }

    /**
     * Collapses if [macro] is already expanded; otherwise expands it and loads its chart — on
     * demand, only now, never as part of the main load above.
     */
    fun toggleMacro(macro: MacroType) {
        if (_expandedMacro.value == macro) {
            macroJob?.cancel()
            _expandedMacro.value = null
            _isMacroChartLoading.value = false
            _macroChart.value = emptyList()
            return
        }
        _expandedMacro.value = macro
        loadMacroChart(macro)
    }

    private fun reload() {
        val period = _selectedPeriod.value
        val offset = periodOffset
        launchRequest { caloriesUseCase.periodSummary(period, offset).toUi() }
    }

    /** Keeps an already-expanded macro's chart live to period/offset changes. */
    private fun reloadExpandedMacro() {
        _expandedMacro.value?.let { loadMacroChart(it) }
    }

    private fun loadMacroChart(macro: MacroType) {
        macroJob?.cancel()
        _isMacroChartLoading.value = true
        val period = _selectedPeriod.value
        val offset = periodOffset
        macroJob = viewModelScope.launch {
            _macroChart.value = caloriesUseCase.macroChartForPeriod(macro, period, offset)
            _isMacroChartLoading.value = false
        }
    }
}

private fun PeriodStatisticsSummary.toUi() = NutritionPeriodUi(
    dateRangeLabel = dateRange.toLabel(),
    averages = averages,
    summary = summary.toUi(),
    caloriesChart = caloriesChart,
)

private fun PeriodCaloriesSummary.toUi(): NutritionSummaryUi = NutritionSummaryUi(
    totalConsumed = totalConsumed,
    totalBurned = totalBurned,
    avgBalance = avgBalance,
    bestDayLabel = bestDay?.toBalanceLabel(bestDayBalance) ?: "-",
    worstDayLabel = worstDay?.toBalanceLabel(worstDayBalance) ?: "-",
)

private fun LocalDate.toBalanceLabel(balance: Int): String =
    "${format(summaryDayFormatter).lowercase()} ($balance kcal)"
