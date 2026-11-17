package inc.dimasic.taskcal.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.domain.model.HealthConnectStatus
import inc.dimasic.taskcal.domain.usecase.healthconnect.HealthConnectUseCase
import inc.dimasic.taskcal.domain.usecase.profile.ProfileUseCase
import inc.dimasic.taskcal.domain.model.Gender
import inc.dimasic.taskcal.domain.model.UnitSystem
import inc.dimasic.taskcal.domain.usecase.model.ProfileDetails
import inc.dimasic.taskcal.domain.usecase.model.ProfileGoals
import java.time.LocalDate
import inc.dimasic.taskcal.ui.screens.uiViews.fromCentimeters
import inc.dimasic.taskcal.ui.screens.uiViews.fromKilograms
import inc.dimasic.taskcal.ui.screens.uiViews.heightLabel
import inc.dimasic.taskcal.ui.screens.uiViews.heightUnit
import inc.dimasic.taskcal.ui.screens.uiViews.toCentimeters
import inc.dimasic.taskcal.ui.screens.uiViews.toKilograms
import inc.dimasic.taskcal.ui.screens.uiViews.weightLabel
import inc.dimasic.taskcal.ui.screens.uiViews.weightUnit
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileUseCase: ProfileUseCase,
    private val healthConnectUseCase: HealthConnectUseCase,
) : ViewModel() {

    val unitSystem: StateFlow<UnitSystem> =
        profileUseCase.unitSystem.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UnitSystem.METRIC)

    val card: StateFlow<ProfileCardUi> =
        combine(profileUseCase.details, profileUseCase.unitSystem) { details, units -> details.toCardUi(units) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileDetails.Empty.toCardUi(UnitSystem.METRIC))

    val detailsForm: StateFlow<ProfileDetailsFormUi> =
        combine(profileUseCase.details, profileUseCase.unitSystem) { details, units -> details.toFormUi(units) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileDetails.Empty.toFormUi(UnitSystem.METRIC))

    val goals: StateFlow<ProfileGoalsUi> =
        combine(profileUseCase.goals, profileUseCase.unitSystem) { goals, units -> goals.toUi(units) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileGoals.Empty.toUi(UnitSystem.METRIC))

    val healthConnectStatus: StateFlow<HealthConnectStatus> =
        healthConnectUseCase.status
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HealthConnectStatus.NOT_CONNECTED)

    /** Read permissions requested from Health Connect: steps, active minutes, weight, distance, heart rate. */
    val healthConnectPermissions: Set<String> = healthConnectUseCase.permissions

    init {
        refreshHealthConnect()
    }

    /** Re-checks whether Health Connect is installed and all permissions are granted. */
    fun refreshHealthConnect() {
        viewModelScope.launch { healthConnectUseCase.refreshStatus() }
    }

    fun updateUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch { profileUseCase.updateUnitSystem(unitSystem) }
    }

    /**
     * Saves the details, [height] being in the chosen unit, then [onSaved] so the screen leaves
     * only once they're written.
     */
    fun updateDetails(name: String, gender: Gender?, dateOfBirth: LocalDate?, height: Int?, onSaved: () -> Unit) {
        val heightCm = height?.let { unitSystem.value.toCentimeters(it) }
        viewModelScope.launch {
            profileUseCase.updateDetails(name, gender, dateOfBirth, heightCm)
            onSaved()
        }
    }

    fun updateCaloriesGoal(calories: Int) {
        viewModelScope.launch { profileUseCase.updateCaloriesGoal(calories) }
    }

    fun updateStepsGoal(steps: Int) {
        viewModelScope.launch { profileUseCase.updateStepsGoal(steps) }
    }

    fun updateMacroGoals(protein: Double, carbs: Double, fat: Double) {
        viewModelScope.launch { profileUseCase.updateMacroGoals(protein, carbs, fat) }
    }

    /** [weightGoal] is in the chosen unit; the goal itself is kept in kilograms. */
    fun updateWeightGoal(weightGoal: Double) {
        val kilograms = unitSystem.value.toKilograms(weightGoal)
        viewModelScope.launch { profileUseCase.updateWeightGoal(kilograms) }
    }
}

private fun ProfileDetails.toCardUi(units: UnitSystem) = ProfileCardUi(
    name = name.ifBlank { "User" },
    subtitle = listOfNotNull(age?.let { "$it years" }, heightCm?.let { units.heightLabel(it) })
        .joinToString(" · ")
        .ifEmpty { "Tap to add your details." },
)

private fun ProfileDetails.toFormUi(units: UnitSystem) = ProfileDetailsFormUi(
    name = name,
    gender = gender,
    dateOfBirth = dateOfBirth,
    height = heightCm?.let { units.fromCentimeters(it) },
    heightUnit = units.heightUnit,
)

private fun ProfileGoals.toUi(units: UnitSystem) = ProfileGoalsUi(
    caloriesGoal = caloriesGoal,
    stepsGoal = stepsGoal,
    proteinGoal = proteinGoal,
    carbsGoal = carbsGoal,
    fatGoal = fatGoal,
    weightGoalLabel = units.weightLabel(weightGoalKg),
    weightGoalInput = units.fromKilograms(weightGoalKg),
    weightUnit = units.weightUnit,
)
