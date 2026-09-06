package inc.dimasic.taskcal.ui.screens.statistic.activities

import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.common.CommonViewModel
import inc.dimasic.taskcal.domain.model.ActivitiesPeriod
import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.model.HealthConnectStatus
import inc.dimasic.taskcal.domain.usecase.ActivitiesUseCase
import inc.dimasic.taskcal.domain.usecase.HealthConnectUseCase
import inc.dimasic.taskcal.domain.usecase.goalFraction
import inc.dimasic.taskcal.domain.usecase.goalPercent
import inc.dimasic.taskcal.domain.usecase.model.ActivityTotals
import inc.dimasic.taskcal.domain.usecase.model.PeriodActivityTotals
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val rangeLabelFormatter = DateTimeFormatter.ofPattern("MMM d")
private val singleDayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

@HiltViewModel
class ActivitiesStatisticViewModel @Inject constructor(
    private val activitiesUseCase: ActivitiesUseCase,
    private val healthConnectUseCase: HealthConnectUseCase,
) : CommonViewModel<PeriodActivityTotals>() {

    private val _selectedPeriod = MutableStateFlow(ActivitiesPeriod.DAY)
    val selectedPeriod: StateFlow<ActivitiesPeriod> = _selectedPeriod.asStateFlow()

    private var periodOffset = 0

    val healthConnectStatus: StateFlow<HealthConnectStatus> =
        healthConnectUseCase.status
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HealthConnectStatus.NOT_CONNECTED)

    val healthConnectPermissions: Set<String> = healthConnectUseCase.permissions

    val healthConnectPermissionContract: ActivityResultContract<Set<String>, Set<String>> =
        healthConnectUseCase.permissionRequestContract()

    init {
        refreshHealthConnect()
        reload()
    }

    fun selectPeriod(period: ActivitiesPeriod) {
        _selectedPeriod.value = period
        periodOffset = 0
        reload()
    }

    fun showPreviousPeriod() {
        periodOffset -= 1
        reload()
    }

    fun showNextPeriod() {
        periodOffset += 1
        reload()
    }

    /** Re-checks whether Health Connect is installed and all permissions are granted. */
    fun refreshHealthConnect() {
        viewModelScope.launch { healthConnectUseCase.refreshStatus() }
    }

    private fun reload() {
        val period = _selectedPeriod.value
        val offset = periodOffset
        launchRequest { activitiesUseCase.totalsForPeriod(period, offset) }
    }
}

fun DateRange.toLabel(): String =
    if (start == end) {
        start.format(singleDayFormatter)
    } else {
        "${start.format(rangeLabelFormatter)} - ${end.format(rangeLabelFormatter)}"
    }

fun ActivityTotals.toCharacteristics(): List<ActivityCharacteristic> = listOfNotNull(
    goalCharacteristic("Steps", steps, stepsGoal) { "%,d".format(it) },
    goalCharacteristic("Calories burned", caloriesBurned, caloriesGoal) { "%,d kcal".format(it) },
    goalCharacteristic("Distance", distanceMeters, distanceGoalMeters) { "%.1f km".format(it / 1000.0) },
    ActivityCharacteristic(
        title = "Active minutes",
        valueLabel = "$activeMinutes min",
        goalPercent = null,
        fraction = null,
    ),
    // A period nothing measured a heart rate over drops the row entirely, rather than claiming 0 bpm.
    avgHeartRate?.let { bpm ->
        ActivityCharacteristic(
            title = "Average heart rate",
            valueLabel = "$bpm bpm",
            goalPercent = null,
            fraction = null,
        )
    },
)

/** Formats one metric for display; how far along the goal it is comes from the domain layer. */
private fun goalCharacteristic(
    title: String,
    value: Int,
    goal: Int,
    valueFormat: (Int) -> String,
): ActivityCharacteristic = ActivityCharacteristic(
    title = title,
    valueLabel = valueFormat(value),
    goalPercent = goalPercent(value, goal),
    fraction = goalFraction(value, goal),
)
