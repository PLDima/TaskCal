package inc.dimasic.taskcal.ui.screens.statistic.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.TasksPeriod
import inc.dimasic.taskcal.domain.usecase.TasksUseCase
import inc.dimasic.taskcal.domain.usecase.model.ChartPoint
import inc.dimasic.taskcal.domain.usecase.model.PeriodTaskOccurrences
import inc.dimasic.taskcal.domain.usecase.model.TaskStatusBreakdown
import inc.dimasic.taskcal.domain.usecase.model.TasksSummary
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

private val rangeLabelFormatter = DateTimeFormatter.ofPattern("MMM d")

@HiltViewModel
class TasksStatisticViewModel @Inject constructor(
    private val tasksUseCase: TasksUseCase,
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(TasksPeriod.WEEK)
    val selectedPeriod: StateFlow<TasksPeriod> = _selectedPeriod.asStateFlow()

    private val _periodOffset = MutableStateFlow(0)

    private val _isLoading = MutableStateFlow(true)

    /**
     * True only until the shared [periodOccurrences] subscription below has produced its first
     * value. After that this screen's data is always live, not "loading" again on every
     * period/offset change or background task edit.
     */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * The one query this screen needs from [TasksUseCase]. Every stat below maps off this same
     * [StateFlow] instead of asking the use case to recombine tasks/completions/period/offset
     * again for itself, which would otherwise re-run the same Room queries and recurrence
     * expansion once per stat.
     */
    private val periodOccurrences: StateFlow<PeriodTaskOccurrences> =
        tasksUseCase.occurrencesForPeriod(_selectedPeriod, _periodOffset)
            .onEach { _isLoading.value = false }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeriodTaskOccurrences.Empty)

    val dateRangeLabel: StateFlow<String> = periodOccurrences
        .map { it.dateRange?.toLabel() ?: "All time" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "All time")

    val summary: StateFlow<TasksSummary> = periodOccurrences
        .map { tasksUseCase.summaryOf(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksSummary.Empty)

    val onTimeRateChart: StateFlow<List<ChartPoint>> =
        combine(periodOccurrences, _selectedPeriod) { occurrences, period ->
            tasksUseCase.onTimeRateChartOf(occurrences, period)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val taskStatus: StateFlow<TaskStatusBreakdown> = periodOccurrences
        .map { tasksUseCase.taskStatusOf(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskStatusBreakdown.Empty)

    fun selectPeriod(period: TasksPeriod) {
        _selectedPeriod.value = period
        _periodOffset.value = 0
    }

    fun showPreviousPeriod() {
        _periodOffset.value -= 1
    }

    fun showNextPeriod() {
        _periodOffset.value += 1
    }
}

private fun DateRange.toLabel(): String =
    "${start.format(rangeLabelFormatter)} - ${end.format(rangeLabelFormatter)}"
