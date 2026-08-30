package inc.dimasic.taskcal.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.usecase.ProfileGoals
import inc.dimasic.taskcal.usecase.ProfileUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileUseCase: ProfileUseCase,
) : ViewModel() {

    val goals: StateFlow<ProfileGoals> =
        profileUseCase.goals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileGoals.Empty)

    fun updateCaloriesGoal(calories: Int) {
        profileUseCase.updateCaloriesGoal(calories)
    }

    fun updateMacroGoals(protein: Double, carbs: Double, fat: Double) {
        profileUseCase.updateMacroGoals(protein, carbs, fat)
    }

    fun updateWeightGoal(weightGoal: Double) {
        profileUseCase.updateWeightGoal(weightGoal)
    }
}
