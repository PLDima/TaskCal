package inc.dimasic.taskcal.ui.screens.statistic.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.model.ActivitiesPeriod
import inc.dimasic.taskcal.model.DateRange
import inc.dimasic.taskcal.repository.ActivitiesRepository
import inc.dimasic.taskcal.repository.ActivityTotals
import inc.dimasic.taskcal.repository.PeriodActivityTotals
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val rangeLabelFormatter = DateTimeFormatter.ofPattern("MMM d")
private val singleDayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

/**
 * One row of the activity characteristics list: value done this period, optionally with a
 * "% of daily goal" figure ([goalPercent]) and a progress bar ([fraction]). Either is null when
 * the characteristic has no goal to track against (e.g. average heart rate).
 */
data class ActivityCharacteristic(
    val title: String,
    val valueLabel: String,
    val goalPercent: Int?,
    val fraction: Float?,
)

@HiltViewModel
class ActivitiesStatisticViewModel @Inject constructor(
    private val activitiesRepository: ActivitiesRepository,
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ActivitiesPeriod.WEEK)
    val selectedPeriod: StateFlow<ActivitiesPeriod> = _selectedPeriod.asStateFlow()

    private val _periodOffset = MutableStateFlow(0)

    private val periodTotals: StateFlow<PeriodActivityTotals> =
        activitiesRepository.totalsForPeriod(_selectedPeriod, _periodOffset)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeriodActivityTotals.Empty)

    val dateRangeLabel: StateFlow<String> = periodTotals
        .map { it.dateRange.toLabel() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            PeriodActivityTotals.Empty.dateRange.toLabel(),
        )

    val totals: StateFlow<ActivityTotals> = periodTotals
        .map { it.totals }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivityTotals.Empty)

    val characteristics: StateFlow<List<ActivityCharacteristic>> = periodTotals
        .map { it.totals.toCharacteristics() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectPeriod(period: ActivitiesPeriod) {
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
    if (start == end) {
        start.format(singleDayFormatter)
    } else {
        "${start.format(rangeLabelFormatter)} - ${end.format(rangeLabelFormatter)}"
    }

private fun ActivityTotals.toCharacteristics(): List<ActivityCharacteristic> = listOf(
    goalCharacteristic("Steps", steps, stepsGoal) { "%,d".format(it) },
    goalCharacteristic("Calories burned", caloriesBurned, caloriesGoal) { "%,d kcal".format(it) },
    goalCharacteristic("Distance", distanceMeters, distanceGoalMeters) { "%.1f km".format(it / 1000.0) },
    ActivityCharacteristic(
        title = "Active minutes",
        valueLabel = "$activeMinutes min",
        goalPercent = null,
        fraction = null,
    ),
    ActivityCharacteristic(
        title = "Average heart rate",
        valueLabel = "$avgHeartRate bpm",
        goalPercent = null,
        fraction = null,
    ),
)

private fun goalCharacteristic(
    title: String,
    value: Int,
    goal: Int,
    valueFormat: (Int) -> String,
): ActivityCharacteristic = ActivityCharacteristic(
    title = title,
    valueLabel = valueFormat(value),
    goalPercent = if (goal > 0) value * 100 / goal else 0,
    fraction = if (goal > 0) (value.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f,
)
