package inc.dimasic.taskcal.ui.screens.statistic.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.model.DateRange
import inc.dimasic.taskcal.model.TasksPeriod
import inc.dimasic.taskcal.repository.PeriodTaskOccurrences
import inc.dimasic.taskcal.usecase.TaskStatusBreakdown
import inc.dimasic.taskcal.usecase.TasksChartPoint
import inc.dimasic.taskcal.usecase.TasksSummary
import inc.dimasic.taskcal.usecase.TasksUseCase
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val rangeLabelFormatter = DateTimeFormatter.ofPattern("MMM d")

@HiltViewModel
class TasksStatisticViewModel @Inject constructor(
    private val tasksUseCase: TasksUseCase,
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(TasksPeriod.WEEK)
    val selectedPeriod: StateFlow<TasksPeriod> = _selectedPeriod.asStateFlow()

    private val _periodOffset = MutableStateFlow(0)

    private val periodOccurrences: StateFlow<PeriodTaskOccurrences> =
        tasksUseCase.occurrencesForPeriod(_selectedPeriod, _periodOffset)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeriodTaskOccurrences.Empty)

    val dateRangeLabel: StateFlow<String> = periodOccurrences
        .map { it.dateRange?.toLabel() ?: "All time" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "All time")

    val summary: StateFlow<TasksSummary> =
        tasksUseCase.summaryForPeriod(_selectedPeriod, _periodOffset)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksSummary.Empty)

    val onTimeRateChart: StateFlow<List<TasksChartPoint>> =
        tasksUseCase.onTimeRateChartForPeriod(_selectedPeriod, _periodOffset)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val taskStatus: StateFlow<TaskStatusBreakdown> =
        tasksUseCase.taskStatusForPeriod(_selectedPeriod, _periodOffset)
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
