package inc.dimasic.taskcal.ui.screens

import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.repository.HealthConnectStatus
import inc.dimasic.taskcal.usecase.HealthConnectUseCase
import inc.dimasic.taskcal.usecase.ProfileGoals
import inc.dimasic.taskcal.usecase.ProfileUseCase
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

    val goals: StateFlow<ProfileGoals> =
        profileUseCase.goals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileGoals.Empty)

    val healthConnectStatus: StateFlow<HealthConnectStatus> =
        healthConnectUseCase.status
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HealthConnectStatus.NOT_CONNECTED)

    /** Read permissions requested from Health Connect: steps, active minutes, weight, distance, heart rate. */
    val healthConnectPermissions: Set<String> = healthConnectUseCase.permissions

    /** One reusable contract instance for the Health Connect permission request launcher. */
    val healthConnectPermissionContract: ActivityResultContract<Set<String>, Set<String>> =
        healthConnectUseCase.permissionRequestContract()

    init {
        refreshHealthConnect()
    }

    /** Re-checks whether Health Connect is installed and all permissions are granted. */
    fun refreshHealthConnect() {
        viewModelScope.launch { healthConnectUseCase.refreshStatus() }
    }

    fun updateCaloriesGoal(calories: Int) {
        profileUseCase.updateCaloriesGoal(calories)
    }

    fun updateStepsGoal(steps: Int) {
        profileUseCase.updateStepsGoal(steps)
    }

    fun updateMacroGoals(protein: Double, carbs: Double, fat: Double) {
        profileUseCase.updateMacroGoals(protein, carbs, fat)
    }

    fun updateWeightGoal(weightGoal: Double) {
        profileUseCase.updateWeightGoal(weightGoal)
    }
}
