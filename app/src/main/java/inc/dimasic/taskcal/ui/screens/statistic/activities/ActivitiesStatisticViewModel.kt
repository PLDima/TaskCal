package inc.dimasic.taskcal.ui.screens.statistic.activities

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.common.CommonViewModel
import inc.dimasic.taskcal.domain.model.ActivitiesPeriod
import inc.dimasic.taskcal.domain.model.HealthConnectStatus
import inc.dimasic.taskcal.domain.usecase.ActivitiesUseCase
import inc.dimasic.taskcal.domain.model.UnitSystem
import inc.dimasic.taskcal.domain.usecase.HealthConnectUseCase
import inc.dimasic.taskcal.domain.usecase.ProfileUseCase
import inc.dimasic.taskcal.domain.usecase.WeightUseCase
import inc.dimasic.taskcal.domain.usecase.model.WeightSummary
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import inc.dimasic.taskcal.domain.usecase.goalFraction
import inc.dimasic.taskcal.domain.usecase.goalPercent
import inc.dimasic.taskcal.domain.usecase.model.ActivityTotals
import inc.dimasic.taskcal.domain.usecase.model.PeriodActivityTotals
import inc.dimasic.taskcal.ui.screens.statistic.statistic_views.toLabel
import inc.dimasic.taskcal.ui.screens.uiViews.distanceLabel
import inc.dimasic.taskcal.ui.screens.uiViews.goalProgressUi
import kotlinx.coroutines.flow.first
import inc.dimasic.taskcal.ui.screens.uiViews.toUi
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ActivitiesStatisticViewModel @Inject constructor(
    private val activitiesUseCase: ActivitiesUseCase,
    private val weightUseCase: WeightUseCase,
    private val profileUseCase: ProfileUseCase,
    private val healthConnectUseCase: HealthConnectUseCase,
) : CommonViewModel<ActivitiesPeriodUi>() {

    private val _selectedPeriod = MutableStateFlow(ActivitiesPeriod.DAY)
    val selectedPeriod: StateFlow<ActivitiesPeriod> = _selectedPeriod.asStateFlow()

    private var periodOffset = 0

    val healthConnectStatus: StateFlow<HealthConnectStatus> =
        healthConnectUseCase.status
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HealthConnectStatus.NOT_CONNECTED)

    val healthConnectPermissions: Set<String> = healthConnectUseCase.permissions

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
        // Both reads go to Health Connect, so they run side by side rather than one after the other.
        launchRequest {
            coroutineScope {
                val totals = async { activitiesUseCase.totalsForPeriod(period, offset) }
                val periodTotals = totals.await()
                val weight = async { weightUseCase.summary(changeOver = periodTotals.dateRange) }
                periodTotals.toUi(weight.await(), profileUseCase.unitSystem.first())
            }
        }
    }
}

private fun PeriodActivityTotals.toUi(weight: WeightSummary, units: UnitSystem) = ActivitiesPeriodUi(
    dateRangeLabel = dateRange.toLabel(),
    caloriesBurned = goalProgressUi(totals.caloriesBurned, totals.caloriesGoal, remainingLabel = "left", overLabel = "over"),
    steps = goalProgressUi(totals.steps, totals.stepsGoal, remainingLabel = "left", overLabel = "over"),
    characteristics = totals.toCharacteristics(units),
    weight = weight.toUi(units, changeWording = if (dateRange.dayCount == 1) "today" else "this period"),
)

private fun ActivityTotals.toCharacteristics(units: UnitSystem): List<ActivityCharacteristic> = listOfNotNull(
    goalCharacteristic("Steps", steps, stepsGoal) { "%,d".format(it) },
    goalCharacteristic("Calories burned", caloriesBurned, caloriesGoal) { "%,d kcal".format(it) },
    goalCharacteristic("Distance", distanceMeters, distanceGoalMeters) { units.distanceLabel(it) },
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
